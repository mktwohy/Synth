package com.example.synth

import com.example.signallib.Note

object AppModel{
    var noteRange = Note.C_3..Note.C_5
    val audioEngine = AudioEngine()
    val bendRange = -1f..1f
    val oscillator = Oscillator()


    val pianoViewModel          = PianoViewModel()
    val harmonicSeriesViewModel = HarmonicSeriesViewModel(oscillator.harmonicSeries)
    val SignalPlotViewModel     = SignalPlotViewModel(oscillator.harmonicSeries)
    val waveFormChangeViewModel = WaveFormChangeViewModel(oscillator)
    val volumeSliderViewModel   = VolumeSliderViewModel(oscillator)
    val pitchBendViewModel      = PitchBendViewModel(oscillator)
}