package com.example.synth

import Signal
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.example.signallib.*
import com.example.signallib.Constants.BUFFER_SIZE
import com.example.signallib.Constants.SAMPLE_RATE
import com.example.signallib.Note.Companion.plus
import com.example.signallib.Note.Companion.toList

import java.util.*


/**
 * A wrapper class for [AudioTrack] that plays audio [Signal]s on a loop.
 *
 *
 * Example Usage:
 * ```
 * val c = AudioGenerator.sinusoid(Note.C_4.freq, sine)
 * val dg = AudioGenerator.sinusoid(setOf(Note.D_4.freq, Note.G_4.freq)), sine)
 * val audioEngine = AudioEngine()
 *
 * audioEngine.currentAudio = setOf(c)
 * audioEngine.start()
 * ...
 * audioEngine.mute()
 * ...
 * audioEngine.signalForPlayback = setOf(c, dg)
 * ...
 * audioEngine.stop()
 * ```
 * .0
 * @property masterSignal the audio to be played on a loop by the [AudioEngine]
 */
class AudioEngine{
    val signalEngine = SignalEngine()

    val noteQueue: Queue<Set<Note>> = LinkedList()
    private val currentNotes = mutableSetOf<Note>()
    private val audioBuffer = FloatArray(BUFFER_SIZE)
    private var runMainLoop = false
    private val masterSignal = SumSignal(autoNormalize = false)
    private val audioTrack = AudioTrack.Builder()
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


    /** Begins playing *currentAudio* on a loop */
    fun start(){
        if (audioTrack.playState != AudioTrack.PLAYSTATE_PLAYING){
            runMainLoop = true
            mainLoop()
        }
    }

    fun stop(){ runMainLoop = false }

    fun destroyAudioTrack(){
        audioTrack.flush()
        audioTrack.release()
    }

    private fun mainLoop(){
        Thread {
            audioTrack.play()
            while (runMainLoop) {
                // update notes
                if(noteQueue.isNotEmpty()){
                    currentNotes.clear()
                    currentNotes.addAll(noteQueue.poll()!!)
                }
                // play notes
                if(currentNotes.isNotEmpty()){
                    signalEngine.renderPcmToBuffer(
                        audioBuffer,
                        currentNotes,
                        AppModel.pitchBend,
                        1/7f
                    )
                    audioTrack.write(audioBuffer, 0, BUFFER_SIZE, AudioTrack.WRITE_BLOCKING)
                }

            }
            audioTrack.stop()
            audioTrack.flush()
        }.start()
    }
}
