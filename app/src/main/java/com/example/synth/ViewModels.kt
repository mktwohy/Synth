package com.example.synth

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.signallib.*

class SignalPlotViewModel(
    val signalSettings: SignalSettings,
    numPeriods: Int = 4
) : ViewModel(){
    private val harmonicSignal = HarmonicSignal(
        fundamental = Note.A_4,
        signalSettings = signalSettings
    )
    val plotBuffer = FloatArray(harmonicSignal.period.toInt()*numPeriods)
    val plotData   = mutableStateListOf<Float>()

    init {
        signalSettings.registerHarmonicSeriesListener { updatePlot() }
        signalSettings.registerWaveShapeListener { updatePlot() }
        updatePlot()
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

class WaveShapeSelectorViewModel(val signalSettings: SignalSettings): ViewModel() {
    var waveShapeName by mutableStateOf(signalSettings.waveShape.abbreviation)
    private var index = 0
    private val waveShapes = WaveShape.values()
    fun nextWaveShape(){
        index = (index + 1) % waveShapes.size
        signalSettings.waveShape = waveShapes[index]
    }

    init {
        signalSettings.registerWaveShapeListener {
            waveShapeName = it.abbreviation
        }
    }
}

class VolumeSliderViewModel(val signalSettings: SignalSettings) : ViewModel(){
    var sliderState by mutableStateOf(1f)
    init {
//        oscillator.registerOnAmpChangedCallback {
//            sliderState = amplitudeToVolume(it)
//        }
    }
}

class PitchBendViewModel(val signalSettings: SignalSettings) : ViewModel(){
    var sliderState by mutableStateOf(0f)
}

class HarmonicSeriesViewModel(
    val signalSettings: SignalSettings
): ViewModel(){
    var sliderState = mutableStateListOf<Float>()
    init {
        repeat(signalSettings.harmonicSeries.numHarmonics){
            sliderState.add(0f)
        }
        signalSettings.registerHarmonicSeriesListener {
            it.forEach { (harmonic, amplitude) ->
                sliderState[harmonic-1] = amplitudeToVolume(amplitude)
            }
        }
    }
}