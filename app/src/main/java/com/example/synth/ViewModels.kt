package com.example.synth

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlin.math.log
import kotlin.math.pow

object AppModel{
    val noteRange = Note.C_3..Note.C_4
    val oscillator = Oscillator()
    val harmonicSeriesViewModel = HarmonicSeriesViewModel(oscillator.harmonicSeries)
    val pianoViewModel = PianoViewModel(noteRange)
}

class PianoViewModel(
    noteRange: ClosedRange<Note>
): ViewModel(){
    val pressedNotes = mutableStateListOf<Note>()
    var width = mutableStateOf(0.dp)
    var height = mutableStateOf(0.dp)
    val pianoGrid = PianoGrid(width, height, AppModel.noteRange)
}

class HarmonicSeriesViewModel(
    val harmonicSeries: HarmonicSeries
): ViewModel(){
    var sliderState = mutableStateListOf<Float>().apply {
        repeat(Constants.NUM_HARMONICS){ this.add(0f) }
    }
    init {
        harmonicSeries.registerOnUpdatedCallback {
            harmonicSeries.forEach { (harmonic, amplitude) ->
                sliderState[harmonic-1] = amplitudeToVolume(amplitude)
            }
        }
    }
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