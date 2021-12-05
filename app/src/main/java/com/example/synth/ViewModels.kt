package com.example.synth

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.signallib.*

class SignalPlotViewModel(
    val harmonicSignal: HarmonicSignal,
    numPeriods: Int = 4
) : ViewModel(){
    val plotBuffer = FloatArray(harmonicSignal.period.toInt()*numPeriods)
    val plotData   = mutableStateListOf<Float>()

    init {
        updatePlot()
        harmonicSignal.harmonicSeries.registerOnUpdatedCallback { updatePlot() }
//        AppModel.oscillator.registerOnAmpChangedCallback {
//            plotSignal.amp = it
//            updatePlot()
//        }
//        AppModel.registerOnBendChangedCallback {
//            plotSignal.bendAmount = it
//            updatePlot()
//        }
        AppModel.signalSettings.registerWaveShapeListener {
            harmonicSignal.waveShape = it
            updatePlot()
        }

    }

    private fun updatePlot(){
        harmonicSignal.reset()
        harmonicSignal.evaluateToBuffer(plotBuffer)
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

class WaveFormChangeViewModel(val signalManager: SignalManager): ViewModel() {
    var waveShape by mutableStateOf(WaveShape.SINE)

    private var index = 1
    private val waveShapes = WaveShape.values()
    fun nextWaveShape(){
        AppModel.signalSettings.waveShape = waveShapes[index]
        index = (index + 1) % waveShapes.size
    }

    init {
        AppModel.signalSettings.registerWaveShapeListener {
            waveShape = it
        }
    }
}

class VolumeSliderViewModel(val signalManager: SignalManager) : ViewModel(){
    var sliderState by mutableStateOf(1f)
    init {
//        oscillator.registerOnAmpChangedCallback {
//            sliderState = amplitudeToVolume(it)
//        }
    }
}

class PitchBendViewModel(val signalManager: SignalManager) : ViewModel(){
    var sliderState by mutableStateOf(0f)
}

class HarmonicSeriesViewModel(
    val harmonicSeries: HarmonicSeries
): ViewModel(){
    var sliderState = mutableStateListOf<Float>()
    init {
        repeat(AppModel.NUM_HARMONICS){
            sliderState.add(0f)
        }
        harmonicSeries.registerOnUpdatedCallback {
            harmonicSeries.forEach { (harmonic, amplitude) ->
                sliderState[harmonic-1] = amplitudeToVolume(amplitude)
            }
        }
    }
}