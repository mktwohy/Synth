package com.example.synth

import androidx.compose.runtime.*
import com.example.signallib.*
import com.example.signallib.enums.WaveShape
import com.example.synth.viewModels.HarmonicEditorViewModel
import com.example.synth.viewModels.PianoViewModel
import com.example.synth.viewModels.SignalPlot
import com.example.synth.viewModels.WaveShapeSelectorViewModel

object AppModel{
    var startup = true

    val signalSettings = SignalSettings(
        harmonicSeries = HarmonicSeries(20),
        waveShape = WaveShape.SINE,
        sampleRate = 44100,
        bufferSize = 512
    )
    val signalManager = SignalManager(signalSettings)
    val signalEngine  = SignalEngine(signalSettings, signalManager)

    var currentAudio by mutableStateOf(listOf<Float>())

    val pianoViewModel          = PianoViewModel()
    val harmonicSeriesViewModel = HarmonicEditorViewModel(signalSettings)
    val SignalPlotViewModel     = SignalPlot(signalSettings)
    val waveFormChangeViewModel = WaveShapeSelectorViewModel(signalSettings)

}