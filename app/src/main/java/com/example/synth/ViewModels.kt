package com.example.synth

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel

class PianoViewModel(
    notes: List<Note>
): ViewModel(){
    val notes = notes.toMutableStateList()
    val pressedNotes = mutableStateListOf<Note>()
    var width = mutableStateOf(0.dp)
    var height = mutableStateOf(0.dp)
    val pianoGrid = PianoGrid(width, height, notes)
}


class HarmonicSignalViewModel(
    signal: HarmonicSignal,
    plotBuffer: FloatArray
) : ViewModel(){
    val signal: MutableState<HarmonicSignal> = mutableStateOf(signal)
    var plotBuffer: MutableState<FloatArray> = mutableStateOf(plotBuffer)
    var bendAmount: MutableState<Float> = mutableStateOf(1f)
    var volume: MutableState<Float> = mutableStateOf(1f)
    var harmonicSliders = mutableStateListOf<Float>().apply {
        repeat(Constants.NUM_HARMONICS){ this.add(0f) }
    }
}