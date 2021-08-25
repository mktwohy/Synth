package com.example.synth

import com.example.synth.Note.Companion.plus
import com.example.synth.Note.Companion.toList

class Oscillator(input: Set<Note>){
    val harmonicSeries = HarmonicSeries()
    var amplitude = 0f
        set(value) {
            field = value
            onAmpChangedCallbacks.forEach { it.invoke(value) }
        }
    var bend = 1f
        set(value) {
            field = 1/value
            onBendChangedCallbacks.forEach { it.invoke(value) }
        }

    val output = SumSignal()
    var waveShape: WaveShape = WaveShape.SINE
    private val noteToSignal = mutableMapOf<Note, HarmonicSignal>()
    private val onAmpChangedCallbacks = mutableSetOf<(Float) -> Unit>()
    private val onBendChangedCallbacks = mutableSetOf<(Float) -> Unit>()

    init {
        assignSignalsToNotes()
    }

    fun bundleSignals() = mutableSetOf<Signal>()
        .apply{
            AppModel.pianoViewModel.pressedNotes.forEach{
                this += noteToSignal[it]
                    ?.apply {
                        this.bend(1/bend)
                    } ?: SilentSignal
        }
    }.toSet()

    fun registerOnActiveNotesChangedCallbacksCallback(callback: (Float) -> Unit){
        onAmpChangedCallbacks.add(callback)
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
            noteToSignal[it] = HarmonicSignal(it, harmonicSeries, 1/7f)
        }
        val lastNote = AppModel.noteRange.endInclusive
        noteToSignal[lastNote+1] = HarmonicSignal(lastNote+1, harmonicSeries, 1/7f)

    }
}