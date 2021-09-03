package com.example.synth

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.synth.Constants.BUFFER_SIZE
import com.example.synth.Constants.SAMPLE_RATE

import java.util.*
import kotlin.system.measureTimeMillis


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
    private val callbacks = mutableSetOf<(FloatArray) -> Unit>()
    fun registerListener(onBufferUpdate: (FloatArray) -> Unit){
        callbacks.add(onBufferUpdate)
    }

    private val masterSignal = SumSignal(autoNormalize = false)
    val signalBuffer: Queue<Set<Signal>> = LinkedList()

    private var runMainLoop = false
    val floatBuffer = FloatArray(BUFFER_SIZE)
    private val shortBuffer = ShortArray(BUFFER_SIZE)
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
                if(signalBuffer.isNotEmpty()){
                    masterSignal.clear()
                    masterSignal.addAll(signalBuffer.poll()!!)
                }
                masterSignal.evaluateToBuffer(floatBuffer)
                audioTrack.write(floatBuffer, 0, BUFFER_SIZE, AudioTrack.WRITE_BLOCKING)
            }
            audioTrack.stop()
            audioTrack.flush()
        }.start()
    }
}