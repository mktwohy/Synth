package com.example.synth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.synth.Note.Companion.toList

class Oscillator(){
    val harmonicSeries = HarmonicSeries()
    private var output = SumSignal()
    private val onOutputUpdated = mutableSetOf<(SumSignal) -> Unit>()
    private val noteToSignal = mutableMapOf<Note, Signal>()
    var waveShape: WaveShape = WaveShape.SINE

    fun registerOnActiveSignalUpdatedCallback(callback: (SumSignal) -> Unit){
        onOutputUpdated.add(callback)
    }

    fun updateSignals(){
        noteToSignal.clear()
        AppModel.noteRange.toList().forEach {
            noteToSignal[it] = HarmonicSignal(it, harmonicSeries, 1/7f)
        }
    }
}