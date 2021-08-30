package com.example.synth


import com.example.synth.Constants.TWO_PI
import com.example.synth.Constants.SAMPLE_RATE
import com.example.synth.Note.Companion.bend
import kotlin.math.*

/** Represents a time-varying signal.
 * Inspired by Allen Downey's ThinkDSP Python module */
abstract class Signal{
    abstract val period: Float
    abstract var amp: Float

    /** Resets the internal index,
     * which guarantees that [evaluateNext] starts at the beginning */
    abstract fun reset()

    /** Uses the Signal's Clock to evaluate the next value in the Signal's sequence */
    abstract fun evaluateNext(): Float

    /** Evaluates the next n periods of the signal as a new array */
    fun evaluate(periods: Int): FloatArray{
        val ret = FloatArray(period.toInt() * periods)
        evaluateToBuffer(ret)
        return ret
    }

    /** Evaluates the signal fill an existing array */
    fun evaluateToBuffer(destination: FloatArray) {
        destination.indices.forEach { destination[it] = evaluateNext() }
    }

    fun plus(that: Signal) = SumSignal(mutableSetOf(this, that))
}

abstract class SignalCollection: Signal(){
    abstract val signals: Collection<Signal>
    abstract var autoNormalize: Boolean

    override var amp: Float = 1f
        set(value){
            if(value >= 0f) field = value
            if (autoNormalize)
            {
                normalize()
            }
        }

    open fun normalize() {
        if (signals.isEmpty()) return
        val ampSum = signals.map { it.amp }.sum()
        signals.forEach { it.amp = (it.amp / ampSum) * this.amp  }
    }

    override fun reset() { signals.forEach { it.reset() } }

//    override fun evaluateNext(): Float{
//        if (autoNormalize) normalize()
//        return signals.fold(0f){ sum, signal ->
//            sum + signal.evaluateNext() * amp
//        }
//    }
    override fun evaluateNext(): Float{
        if (autoNormalize) normalize()
        var sum = 0f
        signals.forEach {
            if(it.amp != 0f){
                sum += it.evaluateNext()
            }
        }
        return sum
    }

    override fun toString(): String{
        val s = StringBuilder()
        s.append(
            when(this){
                is SumSignal        -> ">SumSignal"
                is HarmonicSignal   -> ">HarmonicSignal"
                else                -> ">SignalCollection"
            }
        )
        s.append("(total amp = $amp):")
        signals.forEach{
            s.append("\n\t")
            s.append("$it".replace("\n\t", "\n\t\t"))
        }
        return s.toString()
    }
}

object SilentSignal: Signal() {
    override var period: Float = 1f
    override var amp: Float = 0f

    override fun reset() { }
    override fun evaluateNext() = 0f

    override fun toString() = "SilentSignal"
}


class PeriodicSignal(
    frequency: Float,
    amp: Float = 1f,
    var waveShape: WaveShape = WaveShape.SINE
): Signal() {
    var frequency: Float = frequency
        set(value) {
            angularClock.frequency = value
            field = value
        }

    private val angularClock = AngularClock(frequency)

    override var amp: Float = amp
        set(value) {
            when{
                value >= 0f -> field = value
                value.isNaN() -> field = 0f
            }
        }
    override val period get() = SAMPLE_RATE / angularClock.frequency

    override fun reset() { this.angularClock.reset() }

    override fun evaluateNext() = waveShape.values[angularClock.angle.toInt()] * amp
        .also { angularClock.tick() }

    override fun toString(): String {
        return "FuncSignal:" +
                "\n\tnote = ${angularClock.frequency} " +
                "\n\tamp  = $amp " +
                "\n\twaveShape = $waveShape"
    }
}

class HarmonicSignal(
    fundamental: Note,
    val harmonicSeries: HarmonicSeries = HarmonicSeries(),
    waveShape: WaveShape = WaveShape.SINE,
    override var amp: Float = 1f,
    override var autoNormalize: Boolean = true
): SignalCollection() {
    override val period = SAMPLE_RATE / fundamental.freq
    override val signals = List(Constants.NUM_HARMONICS){ i ->
        PeriodicSignal(
            fundamental.freq*(i+1),
            0f,
            waveShape
        )
    }
    var waveShape: WaveShape = waveShape
        set(value){
            signals.forEach{it.waveShape = value}
            field = value
        }
    var bendAmount: Float = 1f
        set(value){
            val bentFundFreq = fundamental.bend(value)
            for(i in signals.indices) {
                signals[i].frequency = bentFundFreq * (i+1)
            }
            field = value
        }

    var fundamental: Note = fundamental
        set(value){
            field = value
            for(i in signals.indices) {
                signals[i].frequency = fundamental.freq*(i+1)
            }
        }

    init {
        harmonicSeries.registerOnUpdatedCallback {
            for((overtone, amplitude) in harmonicSeries){
                signals[overtone-1].amp = amplitude
            }
        }
    }
}

/** Combines two or more Signals into one Signal. */
class SumSignal(
    signals: Collection<Signal>,
    override var amp: Float = 1f,
    override var autoNormalize: Boolean = true
) : SignalCollection() {
    constructor(vararg signal: Signal, amp: Float = 1f, autoNormalize: Boolean = true)
            : this(signal.toSet(), amp, autoNormalize)

    override val signals = mutableSetOf<Signal>()
    override val period
        get() = signals.map{ it.period.toInt() }.lcm().toFloat()


    init {
        this.signals.addAll(signals)
    }

    operator fun plusAssign(that: Signal){
        when(that){
            is SignalCollection -> this.signals.addAll(that.signals)
            else                -> this.signals.add(that)
        }
    }
}

fun main() {
    val b = FloatArray(Constants.BUFFER_SIZE)
    val s = HarmonicSignal(Note.A_4, HarmonicSeries())
    printAvgTimeMillis(repeat = 50000){ s.evaluateToBuffer(b) }

}