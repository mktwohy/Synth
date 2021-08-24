package com.example.synth

import com.example.synth.Note.Companion.toList

class Oscillator(){
    val harmonicSeries = HarmonicSeries()
    var amplitude = 0f
        set(value) {
            field = value
            onAmpChangedCallbacks.forEach { it.invoke(value) }
        }
    var bend = 1f
        set(value) {
            field = value
            onBendChangedCallbacks.forEach { it.invoke(value) }
        }
    private var output = SumSignal()
    private val noteToSignal = mutableMapOf<Note, Signal>()
    var waveShape: WaveShape = WaveShape.SINE

    private val onOutputUpdatedCallbacks = mutableSetOf<(SumSignal) -> Unit>()
    private val onAmpChangedCallbacks = mutableSetOf<(Float) -> Unit>()
    private val onBendChangedCallbacks = mutableSetOf<(Float) -> Unit>()

    fun registerOnAmpChangedCallback(callback: (Float) -> Unit){
        onAmpChangedCallbacks.add(callback)
    }

    fun registerOnBendChangedCallback(callback: (Float) -> Unit){
        onBendChangedCallbacks.add(callback)
    }

    fun registerOnActiveSignalUpdatedCallback(callback: (SumSignal) -> Unit){
        onOutputUpdatedCallbacks.add(callback)
    }

    fun updateSignals(){
        noteToSignal.clear()
        AppModel.noteRange.toList().forEach {
            noteToSignal[it] = HarmonicSignal(it, harmonicSeries, 1/7f)
        }
    }
}