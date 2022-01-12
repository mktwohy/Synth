package com.example.synth

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.signallib.*
import com.example.signallib.enums.Note
import com.example.signallib.enums.Note.Companion.nextWhiteNote
import com.example.signallib.enums.Note.Companion.prevWhiteNote
import com.example.signallib.enums.WaveShape
import com.example.signallib.signalCollections.HarmonicSignal
import com.example.synth.ui.composables.toFloat
import java.util.*
import kotlin.math.abs

class SignalPlotViewModel(
    val signalSettings: SignalSettings,
    numPeriods: Int = 4
) : ViewModel(){
    private val plotSignal = HarmonicSignal(
        fundamental = Note.A_4,
        signalSettings = signalSettings
    )
    private val plotBuffer = FloatArray(plotSignal.period.toInt()*numPeriods)
    var plotData  by mutableStateOf(plotBuffer.toList())

    init {
        signalSettings.registerHarmonicSeriesListener { updatePlot() }
        signalSettings.registerWaveShapeListener { updatePlot() }
        updatePlot()
    }

    private fun updatePlot(){
        plotSignal.reset()
        plotSignal.evaluateToBuffer(plotBuffer)
//        plotData.clear()
//        for(i in plotBuffer.indices){
//            plotData.add(plotBuffer[i])
//        }
        plotData = plotBuffer.toList()
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
    val sliderState = mutableStateListOf<Float>()

    /** Queue of Pairs of sliderIndex, sliderValue */
    val harmonicSeriesUpdateQueue: Queue<Pair<Int, Float>> = LinkedList()

    init {
        with(signalSettings.harmonicSeries){
            for (i in 1..numHarmonics){
                sliderState.add(this[i]) }
        }
        AppModel.signalEngine.registerAfterBufferWriteCallback {
            if (harmonicSeriesUpdateQueue.isNotEmpty()){
                val (sliderIndex, sliderValue) = harmonicSeriesUpdateQueue.poll()!!
                signalSettings.harmonicSeries[sliderIndex+1] = volumeToAmplitude(sliderValue)
            }
        }
    }

    fun reset(){
        signalSettings.harmonicSeries.reset()
        sliderState.mapInPlace { 0f }
    }

    fun random(){
        AppModel.signalEngine.registerAfterBufferWriteOneTimeCallback {
            signalSettings.harmonicSeries.generateRandom()
            sliderState.mapInPlaceIndexed { i, _ -> signalSettings.harmonicSeries[i+1] }
        }

    }
}