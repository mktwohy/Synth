package com.example.synth

import HarmonicSignal
import Signal
import SumSignal
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.example.signallib.Constants.BUFFER_SIZE
import com.example.signallib.Constants.SAMPLE_RATE
import com.example.signallib.HarmonicSeries
import com.example.signallib.WaveShape
import com.example.signallib.Note
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
    var amplitude = 0f
        set(value) {
            field = value
            onAmpChangedCallbacks.forEach { it.invoke(value) }
        }
    val harmonicSeries = HarmonicSeries()
    var waveShape = WaveShape.SINE
        set(value){
            for((_, signal) in noteToSignal){
                signal.waveShape = value
            }
            onWaveShapeChangedCallbacks.forEach { it.invoke(value) }
            field = value
        }


    init {
        assignSignalsToNotes()
    }

    val signalBufferQueue: Queue<Set<Signal>> = LinkedList()
    private val noteToSignal = mutableMapOf<Note, HarmonicSignal>()
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


    private val onWaveShapeChangedCallbacks = mutableSetOf<(WaveShape) -> Unit>()
    private val onAmpChangedCallbacks = mutableSetOf<(Float) -> Unit>()

    fun registerOnWaveShapeChangedCallback(callback: (WaveShape) -> Unit){
        onWaveShapeChangedCallbacks.add(callback)
    }
    fun registerOnAmpChangedCallback(callback: (Float) -> Unit){
        onAmpChangedCallbacks.add(callback)
    }

    private fun assignSignalsToNotes(){
        Log.d("m_tag","$noteToSignal")
        noteToSignal.clear()
        AppModel.noteRange.toList().forEach {
            noteToSignal[it] = HarmonicSignal(it, harmonicSeries, waveShape, 1/7f)
        }
        val lastNote = AppModel.noteRange.endInclusive
        noteToSignal[lastNote+1] = HarmonicSignal(lastNote+1, harmonicSeries, waveShape,1/7f)
    }

    fun updateNotes(notes: Set<Note>, bend: Float){

    }

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
                if(signalBufferQueue.isNotEmpty()){
                    masterSignal.clear()
                    masterSignal.addAll(signalBufferQueue.poll()!!)
                }
                masterSignal.evaluateToBuffer(audioBuffer)
                audioTrack.write(audioBuffer, 0, BUFFER_SIZE, AudioTrack.WRITE_BLOCKING)
            }
            audioTrack.stop()
            audioTrack.flush()
        }.start()
    }
}
