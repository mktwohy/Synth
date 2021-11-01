package com.example.synth

import HarmonicSignal
import com.example.signallib.HarmonicSeries
import com.example.signallib.Note
import com.example.signallib.Note.Companion.plus
import com.example.signallib.Note.Companion.toList
import com.example.signallib.WaveShape

class SignalHandler {
    val harmonicSeries = HarmonicSeries()
    var waveShape: WaveShape = WaveShape.SINE
        set(value){
            for((_, signal) in noteToSignal){
                signal.waveShape = value
            }
            onWaveShapeChangedCallbacks.forEach { it.invoke(value) }
            field = value
        }

    private val noteToSignal = mutableMapOf<Note, HarmonicSignal>()
    private val onWaveShapeChangedCallbacks = mutableSetOf<(WaveShape) -> Unit>()
    private val onAmpChangedCallbacks = mutableSetOf<(Float) -> Unit>()
    private val onBendChangedCallbacks = mutableSetOf<(Float) -> Unit>()

    init {
        assignSignalsToNotes()
    }

    fun getSignals(
        notes: Set<Note>,
        bend: Float = 0f,
        amplitude: Float = 0f
    ): Set<HarmonicSignal> = notes.map { noteToSignal[it]!! }.toSet()

    fun registerOnWaveShapeChangedCallback(callback: (WaveShape) -> Unit){
        onWaveShapeChangedCallbacks.add(callback)
    }

    fun registerOnAmpChangedCallback(callback: (Float) -> Unit){
        onAmpChangedCallbacks.add(callback)
    }

    fun registerOnBendChangedCallback(callback: (Float) -> Unit){
        onBendChangedCallbacks.add(callback)
    }

    private fun assignSignalsToNotes(){
        noteToSignal.clear()
        AppModel.noteRange.toList().forEach {
            noteToSignal[it] = HarmonicSignal(it, harmonicSeries, waveShape, 1/7f)
        }
        val lastNote = AppModel.noteRange.endInclusive
        noteToSignal[lastNote+1] = HarmonicSignal(lastNote+1, harmonicSeries, waveShape,1/7f)
    }
}