package com.example.synth

import com.example.synth.Note.Companion.plus
import com.example.synth.Note.Companion.toList

class Oscillator{
    val harmonicSeries = HarmonicSeries()
    var amplitude = 0f
        set(value) {
            field = value
            onAmpChangedCallbacks.forEach { it.invoke(value) }
        }
    var bend = 0f
        set(value) {
            field = value
            for((_, signal) in noteToSignal){
                signal.bendAmount = this.bend
            }
            onBendChangedCallbacks.forEach { it.invoke(value) }
        }
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


    fun bundleSignals() = mutableSetOf<Signal>()
        .apply{
            AppModel.pianoViewModel.pressedNotes.forEach{
                this += noteToSignal[it] ?: SilentSignal
        }
    }.toSet()


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