package com.example.synth

import HarmonicSignal
import com.example.signallib.Note.Companion.plus
import com.example.signallib.Note.Companion.toList

class SignalHandler {
    val harmonicSeries = com.example.signallib.HarmonicSeries()
    var waveShape: com.example.signallib.WaveShape = com.example.signallib.WaveShape.SINE
        set(value){
            for((_, signal) in noteToSignal){
                signal.waveShape = value
            }
            onWaveShapeChangedCallbacks.forEach { it.invoke(value) }
            field = value
        }

    private val noteToSignal = mutableMapOf<com.example.signallib.Note, HarmonicSignal>()
    private val onWaveShapeChangedCallbacks = mutableSetOf<(com.example.signallib.WaveShape) -> Unit>()
    private val onAmpChangedCallbacks = mutableSetOf<(Float) -> Unit>()
    private val onBendChangedCallbacks = mutableSetOf<(Float) -> Unit>()

    init {
        assignSignalsToNotes()
    }

    fun getSignals(
        notes: Set<com.example.signallib.Note>,
        bend: Float = 0f,
        amplitude: Float = 0f
    ): Set<HarmonicSignal> = notes.map { noteToSignal[it]!! }.toSet()

    fun registerOnWaveShapeChangedCallback(callback: (com.example.signallib.WaveShape) -> Unit){
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