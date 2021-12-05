package com.example.synth

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.signallib.*

class SignalPlotViewModel(
    signalSettings: SignalSettings,
    numPeriods: Int = 4
) : ViewModel(){
    private val harmonicSignal = HarmonicSignal(
        fundamental = Note.A_4,
        signalSettings = signalSettings
    )
    val plotBuffer = FloatArray(harmonicSignal.period.toInt()*numPeriods)
    val plotData   = mutableStateListOf<Float>()

    init {
        updatePlot()
        harmonicSignal.signalSettings.registerHarmonicSeriesListener { updatePlot() }
        harmonicSignal.signalSettings.registerWaveShapeListener { updatePlot() }
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

class WaveFormChangeViewModel(val signalSettings: SignalSettings): ViewModel() {
    var waveShape by mutableStateOf(WaveShape.SINE)

    private var index = 1
    private val waveShapes = WaveShape.values()
    fun nextWaveShape(){
        signalSettings.waveShape = waveShapes[index]
        index = (index + 1) % waveShapes.size
    }

    init {
        signalSettings.registerWaveShapeListener {
            waveShape = it
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