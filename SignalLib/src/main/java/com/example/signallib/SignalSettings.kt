package com.example.signallib

class SignalSettings(
    val harmonicSeries: HarmonicSeries,
    waveShape: WaveShape,
    sampleRate: Int,
    bufferSize: Int,
){
    var waveShape   = StateBroadcaster(waveShape)
    var sampleRate  = StateBroadcaster(sampleRate)
    var bufferSize  = StateBroadcaster(bufferSize)
}
