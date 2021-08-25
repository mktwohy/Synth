package com.example.synth

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel


class SignalPlotViewModel(
    harmonicSeries: HarmonicSeries,
    numPeriods: Int = 4
) : ViewModel(){
    val plotSignal = HarmonicSignal(Note.C_2, harmonicSeries)
    val plotBuffer = FloatArray(plotSignal.period.toInt()*numPeriods)
    val plotData   = mutableStateListOf<Float>()

    init {
        harmonicSeries.registerOnUpdatedCallback { updatePlot() }
        AppModel.oscillator.registerOnAmpChangedCallback {
            plotSignal.amp = it
            updatePlot()
        }
        AppModel.oscillator.registerOnBendChangedCallback {
            plotSignal.bend(it)
            updatePlot()
        }

    }

    private fun updatePlot(){
        plotSignal.reset()
        plotSignal.evaluateToBuffer(plotBuffer)
        plotData.clear()
        for(i in plotBuffer.indices){
            plotData.add(plotBuffer[i])
        }
    }
}

class PianoViewModel : ViewModel(){
    var pressedNotes by mutableStateOf(setOf<Note>())
    var width = mutableStateOf(0.dp)
    var height = mutableStateOf(0.dp)
    val pianoGrid = PianoGrid(width, height, AppModel.noteRange)
//    private val onPressedNotesChangedCallbacks = mutableSetOf< (Set<Note>) -> Unit >()
//
//    fun registerOnPressedNotesChangedCallback(callback: (Set<Note>) -> Unit){
//        onPressedNotesChangedCallbacks.add(callback)
//    }
}

class VolumeSliderViewModel(val oscillator: Oscillator) : ViewModel(){
    var sliderState by mutableStateOf(1f)
    init {
        oscillator.registerOnAmpChangedCallback {
            sliderState = amplitudeToVolume(it)
        }
    }
}

class PitchBendViewModel(val oscillator: Oscillator) : ViewModel(){
    var sliderState by mutableStateOf(1f)
    init {
        oscillator.registerOnBendChangedCallback {
            sliderState = it
        }
    }
}

class HarmonicSeriesViewModel(
    val harmonicSeries: HarmonicSeries
): ViewModel(){
    var sliderState = mutableStateListOf<Float>()
    init {
        repeat(Constants.NUM_HARMONICS){
            sliderState.add(0f)
        }
        AppModel.oscillator.harmonicSeries.registerOnUpdatedCallback {
            AppModel.oscillator.harmonicSeries.forEach { (harmonic, amplitude) ->
                sliderState[harmonic-1] = amplitudeToVolume(amplitude)
            }
        }
    }
}