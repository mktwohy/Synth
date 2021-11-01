package com.example.synth

import HarmonicSignal
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.signallib.amplitudeToVolume


class SignalPlotViewModel(
    harmonicSeries: com.example.signallib.HarmonicSeries,
    numPeriods: Int = 4
) : ViewModel(){
    val plotSignal = HarmonicSignal(com.example.signallib.Note.C_2, harmonicSeries, com.example.signallib.WaveShape.SINE)
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
    var pressedNotes by mutableStateOf(setOf<com.example.signallib.Note>())
    var width = mutableStateOf(0.dp)
    var height = mutableStateOf(0.dp)
    val pianoGrid = PianoGrid(width, height, AppModel.noteRange)
}

class WaveFormChangeViewModel(val oscillator: Oscillator): ViewModel() {
    var waveShape by mutableStateOf(com.example.signallib.WaveShape.SINE)

    private var index = 1
    private val waveShapes = com.example.signallib.WaveShape.values()
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
    val harmonicSeries: com.example.signallib.HarmonicSeries
): ViewModel(){
    var sliderState = mutableStateListOf<Float>()
    init {
        repeat(com.example.signallib.Constants.NUM_HARMONICS){
            sliderState.add(0f)
        }
        AppModel.oscillator.harmonicSeries.registerOnUpdatedCallback {
            AppModel.oscillator.harmonicSeries.forEach { (harmonic, amplitude) ->
                sliderState[harmonic-1] = amplitudeToVolume(amplitude)
            }
        }
    }
}