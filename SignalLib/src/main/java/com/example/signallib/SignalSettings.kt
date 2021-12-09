package com.example.signallib

class SignalSettings(
    val harmonicSeries: HarmonicSeries,
    waveShape: WaveShape,
    sampleRate: Int,
    bufferSize: Int,
){
    val waveShapeBroadcaster = Broadcaster<WaveShape>()
    val sampleRateBroadcaster = Broadcaster<Int>()
    val bufferSizeBroadcaster = Broadcaster<Int>()

    var waveShape = waveShape
        set(value){
            field = value
            waveShapeBroadcaster.broadcast(value)
        }
    var sampleRate = sampleRate
        set(value){
            field = value
            sampleRateBroadcaster.broadcast(value)
        }
    var bufferSize = bufferSize
        set(value){
            field = value
            bufferSizeBroadcaster.broadcast(value)
        }

    fun registerHarmonicSeriesListener(callback: (HarmonicSeries) -> Unit){
        harmonicSeries.registerOnUpdatedCallback(callback)
    }

    fun registerWaveShapeListener(callback: (WaveShape) -> Unit){
        waveShapeBroadcaster.registerListener(callback)
    }

    fun registerSampleRateListener(callback: (Int) -> Unit){
        sampleRateBroadcaster.registerListener(callback)
    }

    fun registerBufferSizeListener(callback: (Int) -> Unit){
        bufferSizeBroadcaster.registerListener(callback)
    }
}
