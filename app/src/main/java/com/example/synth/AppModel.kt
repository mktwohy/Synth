package com.example.synth

import androidx.compose.runtime.*
import com.example.signallib.*

object AppModel{
    val signalSettings = SignalSettings(
        harmonicSeries = HarmonicSeries(15),
        waveShape = WaveShape.SINE,
        sampleRate = 44800,
        bufferSize = 192
    )
    val signalManager = SignalManager(signalSettings)
    val signalEngine  = SignalEngine(signalSettings, signalManager)

    var noteRange = Note.C_3..Note.C_5
    var currentAudio by mutableStateOf<List<Float>>(listOf())

    val pianoViewModel          = PianoViewModel()
    val harmonicSeriesViewModel = HarmonicSeriesViewModel(signalSettings)
    val SignalPlotViewModel     = SignalPlotViewModel(signalSettings)
    val waveFormChangeViewModel = WaveShapeSelectorViewModel(signalSettings)
}