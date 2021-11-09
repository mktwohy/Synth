package com.example.synth

import com.example.signallib.Note
import com.example.signallib.SignalEngine
import com.example.signallib.WaveShape

object AppModel{
    var noteRange = Note.C_3..Note.C_5
    val audioEngine = AudioEngine()
    var pitchBend = 0f
    val bendRange = -1f..1f
    val signalEngine = SignalEngine()


    val pianoViewModel          = PianoViewModel()
    val harmonicSeriesViewModel = HarmonicSeriesViewModel(signalEngine.harmonicSeries)
    val SignalPlotViewModel     = SignalPlotViewModel(signalEngine.harmonicSeries)
    val waveFormChangeViewModel = WaveFormChangeViewModel(signalEngine)
    val volumeSliderViewModel   = VolumeSliderViewModel(signalEngine)
    val pitchBendViewModel      = PitchBendViewModel(signalEngine)
}