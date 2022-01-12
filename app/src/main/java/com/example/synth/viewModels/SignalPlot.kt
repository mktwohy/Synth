package com.example.synth.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.signallib.SignalSettings
import com.example.signallib.enums.Note
import com.example.signallib.signalCollections.HarmonicSignal

class SignalPlot(
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