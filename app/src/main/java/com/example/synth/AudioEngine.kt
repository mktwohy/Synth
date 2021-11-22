package com.example.synth

import Signal
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.signallib.*
import com.example.signallib.Constants.BUFFER_SIZE
import com.example.signallib.Constants.SAMPLE_RATE

import java.util.*


/**
 * A wrapper class for [AudioTrack] and [SignalEngine] that
 *
 */
class AudioEngine(
    val signalEngine: SignalEngine = SignalEngine(),
){
    private val noteQueue: Queue<Set<Note>> = LinkedList()
    private val audioBuffer = FloatArray(BUFFER_SIZE)
    private var runMainLoop = false
    private var audioTrack = createAudioTrack()
    private val onBufferUpdateListeners = mutableSetOf< (FloatArray) -> Unit >()

    fun registerOnBufferUpdateCallback(callback: (FloatArray) -> Unit){
        onBufferUpdateListeners.add(callback)
    }

    fun updateNotes(notes: Set<Note>){
        noteQueue.offer(notes)
    }

    /** Begins playing *currentAudio* on a loop */
    fun start(){
        if (audioTrack.playState != AudioTrack.PLAYSTATE_PLAYING){
            runMainLoop = true
            mainLoop()
        }
    }

    fun stop(){ runMainLoop = false }

    /**
     * Stops AudioTrack, releases it from memory, creates a new AudioTrack, and starts it.
     * If sample rate changes, AudioEngine should be reset.
     * */
    fun reset(){
        stop()
        audioTrack.flush()
        audioTrack.release()
        this.audioTrack = createAudioTrack()
        start()
    }

    private fun mainLoop(){
        val prevNotes       = mutableSetOf<Note>()
        val currentNotes    = mutableSetOf<Note>()

        Thread {
            audioTrack.play()

            while (runMainLoop) {
                // poll from note queue
                if(noteQueue.isNotEmpty()) {
                    currentNotes.clear()
                    currentNotes.addAll(noteQueue.poll()!!)
                }

                // check if audio buffer needs to be updated
                // (ensures silent audio doesn't get rendered)
                if( !(prevNotes.isEmpty() && currentNotes.isEmpty()) ){
                    // render notes to audio buffer
                    signalEngine.renderToBuffer(
                        buffer = audioBuffer,
                        notes = currentNotes,
                        pitchBend = AppModel.pitchBend,
                        amp = 1 / 7f
                    )

                    onBufferUpdateListeners.forEach { it.invoke(audioBuffer) }
                    prevNotes.replaceAll(currentNotes)
                }

                // write buffer to AudioTrack
                audioTrack.write(
                    audioBuffer,
                    0,
                    BUFFER_SIZE,
                    AudioTrack.WRITE_BLOCKING
                )
            }
            audioTrack.stop()
            audioTrack.flush()
        }.start()
    }

    private fun createAudioTrack() = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build())
        .setTransferMode(AudioTrack.MODE_STREAM)
        .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
        .build()
}
