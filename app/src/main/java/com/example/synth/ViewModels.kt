package com.example.synth

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel


class SignalPlotViewModel(
    harmonicSeries: HarmonicSeries,
    numPeriods: Int = 4
) : ViewModel(){
    val plotSignal = HarmonicSignal(Note.C_2, harmonicSeries, WaveShape.SINE)
    val plotBuffer = FloatArray(plotSignal.period.toInt()*numPeriods)
    val plotData   = mutableStateListOf<Float>()

    init {
        updatePlot()
        harmonicSeries.registerOnUpdatedCallback { updatePlot() }
        AppModel.oscillator.registerOnAmpChangedCallback {
            plotSignal.amp = it
            updatePlot()
        }
        AppModel.oscillator.registerOnBendChangedCallback {
            plotSignal.bendAmount = it
            updatePlot()
        }
        AppModel.oscillator.registerOnWaveShapeChangedCallback {
            plotSignal.waveShape = it
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
}

class WaveFormChangeViewModel(val oscillator: Oscillator): ViewModel() {
    var waveShape by mutableStateOf(WaveShape.SINE)

    private var index = 1
    private val waveShapes = WaveShape.values()
    fun nextWaveShape(){
        oscillator.waveShape = waveShapes[index]
        index = (index + 1) % waveShapes.size
    }

    init {
        oscillator.registerOnWaveShapeChangedCallback {
            waveShape = it
        }
    }
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
    val bendRange = AppModel.bendRange
    var sliderState by mutableStateOf(0f)
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