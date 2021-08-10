package com.example.synth

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
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
 * @property signal the audio to be played on a loop by the [AudioEngine]
 */
class AudioEngine(private val main: MainActivity){
    companion object{
        const val SAMPLE_RATE = 44100
        const val BUFFER_SIZE = 256
    }

    private val signal = SumSignal()
    val signalBuffer: Queue<Set<Signal>> = LinkedList()

    private var runMainLoop = false
    private val floatBuffer = FloatArray(BUFFER_SIZE)
    private val shortBuffer = ShortArray(BUFFER_SIZE)
    private val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build())
        .setTransferMode(AudioTrack.MODE_STREAM)
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
                signal.apply {
                    if(signalBuffer.isNotEmpty()){
                        clear()
                        addSignals(signalBuffer.poll()!!)
                    }
                    evaluateTo(floatBuffer, false)
                }
                floatBuffer.toShortArray(shortBuffer, Constants.MAX_16BIT_VALUE)
                main.updatePlot(floatBuffer)
                audioTrack.write(shortBuffer, 0, BUFFER_SIZE)
            }
            audioTrack.stop()
            audioTrack.flush()
        }.start()
    }
}