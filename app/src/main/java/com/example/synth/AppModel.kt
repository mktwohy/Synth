package com.example.synth

import androidx.compose.runtime.*
import com.example.signallib.Note
import com.example.signallib.SignalEngine

object AppModel{
    val signalEngine = SignalEngine()
    val audioEngine  = AudioEngine(signalEngine)
    var noteRange = Note.C_3..Note.C_5
    var pitchBend = 0f
    val bendRange = -1f..1f
    var currentAudio by mutableStateOf<List<Float>>(listOf())

    val pianoViewModel          = PianoViewModel()
    val harmonicSeriesViewModel = HarmonicSeriesViewModel(signalEngine.harmonicSeries)
    val SignalPlotViewModel     = SignalPlotViewModel(signalEngine.harmonicSeries)
    val waveFormChangeViewModel = WaveFormChangeViewModel(signalEngine)
    val volumeSliderViewModel   = VolumeSliderViewModel(signalEngine)
    val pitchBendViewModel      = PitchBendViewModel(signalEngine)
}