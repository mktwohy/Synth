package com.example.signallib

class SignalSettings(
    val harmonicSeries: HarmonicSeries,
    waveShape: WaveShape,
    sampleRate: Int,
    bufferSize: Int,
){
    var waveShape = waveShape
        set(value){
            field = value
            waveShapeListeners.forEach { it.invoke(value) }
        }
    var sampleRate = sampleRate
        set(value){
            field = value
            sampleRateListeners.forEach { it.invoke(value) }
        }
    var bufferSize = bufferSize
        set(value){
            field = value
            bufferSizeListeners.forEach { it.invoke(value) }
        }

    fun registerHarmonicSeriesListener(callback: (HarmonicSeries) -> Unit){
        harmonicSeries.registerOnUpdatedCallback(callback)
    }

    fun registerWaveShapeListener(callback: (WaveShape) -> Unit){
        waveShapeListeners.add(callback)
    }

    fun registerSampleRateListener(callback: (Int) -> Unit){
        sampleRateListeners.add(callback)
    }

    fun registerBufferSizeListener(callback: (Int) -> Unit){
        bufferSizeListeners.add(callback)
    }

    private val waveShapeListeners = mutableSetOf<(WaveShape) -> Unit>()
    private val sampleRateListeners = mutableSetOf<(Int) -> Unit>()
    private val bufferSizeListeners = mutableSetOf<(Int) -> Unit>()

}