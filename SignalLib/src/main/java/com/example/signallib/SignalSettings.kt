package com.example.signallib

class SignalSettings(
    val harmonicSeries: HarmonicSeries,
    waveShape: WaveShape,
    sampleRate: Int,
    bufferSize: Int,
){
    var waveShape = waveShape
        set(value){
            waveShapeListeners.forEach { it.invoke(value) }
            field = value
        }
    var sampleRate = sampleRate
        set(value){
            sampleRateListeners.forEach { it.invoke(value) }
            field = value
        }
    var bufferSize = bufferSize
        set(value){
            bufferSizeListeners.forEach { it.invoke(value) }
            field = value
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

    fun registerBuffSizeListener(callback: (Int) -> Unit){
        bufferSizeListeners.add(callback)
    }

    private val waveShapeListeners = mutableSetOf<(WaveShape) -> Unit>()
    private val sampleRateListeners = mutableSetOf<(Int) -> Unit>()
    private val bufferSizeListeners = mutableSetOf<(Int) -> Unit>()

}