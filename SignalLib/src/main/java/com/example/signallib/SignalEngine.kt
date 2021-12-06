package com.example.signallib

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

import java.util.*


/**
 * A threaded wrapper class that uses [SignalManager] to continually feed data to [AudioTrack].
 *
 * How to use:
 * 1. set [signalManager] parameters waveShape and harmonicSeries
 * 2. start()
 * 3. update realtime input with [updateNotes], [updateAmp], and [updatePitchBend]
 * 4. stop()
 */
class SignalEngine(
    val signalSettings: SignalSettings,
    val signalManager: SignalManager,
){
    private val noteQueue: Queue<Set<Note>>     = LinkedList()
    private val pitchBendQueue: Queue<Float>    = LinkedList()
    // todo [HarmonicSignal] amp is bugged.
    private val ampQueue: Queue<Float>          = LinkedList()

    private var audioBuffer = FloatArray(signalSettings.bufferSize)
    private var runMainLoop = false
    private var audioTrack = buildAudioTrack()
    private val afterBufferWriteOneTimeListeners = mutableSetOf< (FloatArray) -> Unit >()
    private val afterBufferWriteListeners = mutableSetOf< (FloatArray) -> Unit >()

    fun registerAfterBufferWriteOneTimeCallback(callback: (FloatArray) -> Unit){
        afterBufferWriteOneTimeListeners.add(callback)
    }

    fun registerAfterBufferWriteCallback(callback: (FloatArray) -> Unit){
        afterBufferWriteListeners.add(callback)
    }

    @Deprecated("Amp does not properly update")
    fun updateAmp(amp: Float)               { ampQueue.offer(amp)               }
    fun updateNotes(notes: Set<Note>)       { noteQueue.offer(notes)            }
    fun updatePitchBend(semitones: Float)   { pitchBendQueue.offer(semitones)   }


    fun play(){
        if (audioTrack.playState != AudioTrack.PLAYSTATE_PLAYING){
            runMainLoop = true
            mainLoop()
        }
    }

    fun pause(){
        audioTrack.pause()
        audioTrack.flush()
        runMainLoop = false
    }

    /**
     * Stops AudioTrack, releases it from memory, creates a new AudioTrack, and starts it.
     * Note: If sample rate or buffer size changes, AudioEngine should be reset.
     * */
    fun reset(){
        pause()
        audioTrack.flush()
        audioTrack.release()
        audioBuffer = FloatArray(signalSettings.bufferSize)
        this.audioTrack = buildAudioTrack()
    }

    private fun mainLoop(){
        Thread {
            val prevNotes       = mutableSetOf<Note>()
            val currentNotes    = mutableSetOf<Note>()
            var pitchBend       = 0f
            var amp             = 1/7f

            audioTrack.play()

            while (runMainLoop) {
                // poll from realtime input queues
                if(noteQueue.isNotEmpty())
                    currentNotes.replaceAll(noteQueue.poll()!!)

                if(pitchBendQueue.isNotEmpty())
                    pitchBend = pitchBendQueue.poll()!!

                if(ampQueue.isNotEmpty())
                    amp = ampQueue.poll()!!


                // check if audio buffer needs to be updated
                // (ensures silent audio doesn't get rendered)
                if( !(prevNotes.isEmpty() && currentNotes.isEmpty()) ){
                    // render notes to audio buffer
                    signalManager.renderToBuffer(
                        buffer = audioBuffer,
                        notes = currentNotes,
                        pitchBend = pitchBend,
                        amp = amp
                    )

                    afterBufferWriteOneTimeListeners.forEach { it.invoke(audioBuffer) }
                    prevNotes.replaceAll(currentNotes)
                }

                // write buffer to AudioTrack
                audioTrack.write(
                    audioBuffer,
                    0,
                    signalSettings.bufferSize,
                    AudioTrack.WRITE_BLOCKING
                )

                afterBufferWriteListeners.forEach{ it.invoke(audioBuffer) }
                afterBufferWriteOneTimeListeners.forEach{ it.invoke(audioBuffer) }
                afterBufferWriteOneTimeListeners.clear()
            }
        }.start()
    }

    private fun buildAudioTrack() = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                .setSampleRate(signalSettings.sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build())
        .setTransferMode(AudioTrack.MODE_STREAM)
        .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
        .build()
}
