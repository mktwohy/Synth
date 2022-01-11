package com.example.signallib.signals

import com.example.signallib.SignalSettings

class PeriodicSignal(
    frequency: Float,
    amp: Float = 1f,
    signalSettings: SignalSettings
): Signal(signalSettings) {
    var frequency: Float = frequency
        set(value) {
            angularClock.frequency = value
            field = value
        }

    init {
        this.amp = amp
    }

    private val angularClock = AngularClock(frequency, signalSettings)

    override val period get() = signalSettings.sampleRate / angularClock.frequency

    override fun reset() { this.angularClock.reset() }

    override fun evaluateNext(): Float =
        signalSettings.waveShape.lookupTable[angularClock.angle.toInt()] * amp
            .also { angularClock.tick() }

    override fun toString(): String {
        return "FuncSignal:" +
                "\n\tnote = ${angularClock.frequency} " +
                "\n\tamp  = $amp " +
                "\n\twaveShape = ${signalSettings.waveShape}"
    }
}


/** Used by [Signal] to keep track of its current angle with respect to some frequency
 *
 * It assumes that [tick] is being called every single audio sample.
 * ie: if the sample rate is 44100, the [angle] is being updated 44100 times per second,
 * regardless of its [frequency]
 */
class AngularClock(
    frequency: Float,
    val signalSettings: SignalSettings,
    initAngle: Float = 0f
){
    var frequency: Float = 0f
        set(value){
            tickAngle = 360 / (signalSettings.sampleRate / value)
            field = value
        }

    private var tickAngle: Float = 0f

    var angle = 0f

    init {
        this.frequency = frequency
        this.angle = initAngle
        signalSettings.registerSampleRateListener { sampleRate ->
            tickAngle = 360 / (sampleRate / frequency)
        }
    }

    /** It increments the current [angle] according to the [frequency] */
    fun tick(){
        angle = (angle + tickAngle) % 360
    }

    fun reset(){
        this.angle = 0f
    }
}
