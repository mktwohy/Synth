package com.example.synth

import androidx.compose.runtime.*
import com.example.signallib.Note
import com.example.signallib.SignalEngine
import com.example.signallib.SignalManager

object AppModel{
    val signalManager = SignalManager()
    val signalEngine  = SignalEngine(signalManager)
    var noteRange = Note.C_3..Note.C_5
    var pitchBend = 0f
        set(value){
            signalEngine.updatePitchBend(value)
            field = value
        }
    val bendRange = -1f..1f
    var currentAudio by mutableStateOf<List<Float>>(listOf())

    val pianoViewModel          = PianoViewModel()
    val harmonicSeriesViewModel = HarmonicSeriesViewModel(signalManager.harmonicSeries)
    val SignalPlotViewModel     = SignalPlotViewModel(signalManager.harmonicSeries)
    val waveFormChangeViewModel = WaveFormChangeViewModel(signalManager)
    val volumeSliderViewModel   = VolumeSliderViewModel(signalManager)
    val pitchBendViewModel      = PitchBendViewModel(signalManager)
}