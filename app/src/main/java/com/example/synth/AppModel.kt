package com.example.synth

import androidx.compose.runtime.*
import com.example.signallib.*

object AppModel{
    var startup = true

    val signalSettings = SignalSettings(
        harmonicSeries = HarmonicSeries(15),
        waveShape = WaveShape.SINE,
        sampleRate = 44100,
        bufferSize = 512
    )
    val signalManager = SignalManager(signalSettings)
    val signalEngine  = SignalEngine(signalSettings, signalManager)

    var currentAudio by mutableStateOf(listOf<Float>())

    val pianoViewModel          = PianoViewModel()
    val harmonicSeriesViewModel = HarmonicSeriesViewModel(signalSettings)
    val SignalPlotViewModel     = SignalPlotViewModel(signalSettings)
    val waveFormChangeViewModel = WaveShapeSelectorViewModel(signalSettings)

}