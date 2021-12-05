package com.example.synth

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.signallib.*
import com.example.signallib.Note.Companion.minus
import com.example.signallib.Note.Companion.nextWhiteNote
import com.example.signallib.Note.Companion.plus
import com.example.signallib.Note.Companion.prevWhiteNote
import kotlin.math.abs

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
    var width by mutableStateOf(0.dp)
    var height by mutableStateOf(0.dp)
    var noteRange by mutableStateOf(Note.C_4..Note.C_5)
    val pianoGrid = PianoGrid(this)
    var rangeSliderState by mutableStateOf(
        noteRange.start.toFloat()..noteRange.endInclusive.toFloat()
    )

    fun shiftNoteRange(startOffset: Int, endOffset: Int){
        var start = noteRange.start
        var end = noteRange.endInclusive

        if(startOffset < 0)
            repeat(abs(startOffset)){ start = start.prevWhiteNote() }
        else if(startOffset > 0)
            repeat(abs(startOffset)){ start = start.nextWhiteNote() }

        if(endOffset < 0)
            repeat(abs(endOffset)){ end = end.prevWhiteNote() }
        else if(endOffset > 0)
            repeat(abs(endOffset)){ end = end.nextWhiteNote() }

        noteRange = start..end
        logd("Shift: $noteRange")
        pianoGrid.recalculateWidths()
    }

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