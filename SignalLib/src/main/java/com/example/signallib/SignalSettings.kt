package com.example.signallib

import com.example.signallib.enums.WaveShape

/** Various parameters that each signal has.
 *
 * They are stored within a class so that multiple signals can have the a reference to the same
 * SignalSettings, which ensures that, when you adjust the shared SignalSettings instance, you
 * apply changes to all signals.
 */
class SignalSettings(
    harmonicSeries: HarmonicSeries,
    waveShape: WaveShape,
    sampleRate: Int,
    bufferSize: Int,
){
    private val harmonicSeriesBroadcaster = Broadcaster<HarmonicSeries>()
    private val waveShapeBroadcaster = Broadcaster<WaveShape>()
    private val sampleRateBroadcaster = Broadcaster<Int>()
    private val bufferSizeBroadcaster = Broadcaster<Int>()

    /**
     * For performance reasons, prefer mutating harmonicSeries rather than reassigning it
     */
    var harmonicSeries = harmonicSeries
        set(value){
            field = value
            harmonicSeriesBroadcaster.broadcast(value)
        }

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
        harmonicSeriesBroadcaster.registerListener(callback)
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
