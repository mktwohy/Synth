package com.example.synth


import com.example.synth.Constants.TWO_PI
import com.example.synth.Constants.SAMPLE_RATE
import com.example.synth.Note.Companion.bend
import kotlin.math.*

/** Represents a time-varying signal.
 * Inspired by Allen Downey's ThinkDSP Python module */
abstract class Signal{
    companion object{
        val sine = { i: Int, freq: Float ->
            sin(TWO_PI * i / (SAMPLE_RATE/freq)).toFloat()
        }
        val cosine = { i: Int, freq: Float ->
            cos(TWO_PI * i / (SAMPLE_RATE/freq)).toFloat()
        }
        val silence = { _: Int, _: Int ->
            0f
        }
    }

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
        destination.indices.forEach { i -> destination[i] = evaluateNext() }
    }

    fun plus(that: Signal) = SumSignal(mutableSetOf(this, that))
}

abstract class SignalCollection: Signal(){
    abstract val signals: Collection<Signal>
    abstract var autoNormalize: Boolean

    override var amp: Float = 1f
        set(value){
            if(value >= 0f) field = value
            if (autoNormalize) normalize()
        }

    open fun normalize() {
        if (signals.isEmpty()) return
        val ampSum = signals.map { it.amp }.sum()
        signals.forEach { it.amp = (it.amp / ampSum) * this.amp  }
    }

    override fun reset() { signals.forEach { it.reset() } }

    override fun evaluateNext(): Float{
        if (autoNormalize) normalize()
        return signals.fold(0f){ sum, signal ->
            sum + signal.evaluateNext() * amp
        }
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
    val angularClock: AngularClock = AngularClock(440f),
    amp: Float = 1f,
    var waveShape: WaveShape = WaveShape.SINE
): Signal() {
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
        return "FuncSignal:\n\tnote = ${angularClock.frequency} \n\tamp  = $amp \n\twaveShape = $waveShape"
    }
}

class HarmonicSignal(
    fundamental: Note,
    val harmonicSeries: HarmonicSeries = HarmonicSeries(),
    override var amp: Float = 1f,
    override var autoNormalize: Boolean = true
): SignalCollection() {
    override val period = SAMPLE_RATE / fundamental.freq
    override val signals = List(Constants.NUM_HARMONICS){ i ->
        PeriodicSignal(AngularClock(fundamental.freq*(i+1)), 0f)
    }
    var bendAmount: Float = 1f
        set(value){
            val bentFundFreq = fundamental.bend(value)
            for(i in signals.indices) {
                signals[i].angularClock.frequency = bentFundFreq * (i+1)
            }
            logd(value)
            field = value
        }

    var fundamental: Note = fundamental
        set(value){
            field = value
            for(i in signals.indices) {
                signals[i].angularClock.frequency = fundamental.freq*(i+1)
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

fun main(){
    val s1 = PeriodicSignal(AngularClock(Note.A_4.freq))
    val s2 = PeriodicSignal(AngularClock(Note.A_3.freq))
    val sum = SumSignal(s1, s2)

    s1.plotInConsole()
    sum.plotInConsole()

    val angle = 0.5f
    println(angle * s1.period / sum.period)
//    val sum3 = SumSignal(sum, harm)

//    println(sum3)
//    println(harm)
//    harm.amp = 0.5f
//    println(harm)


//    sum3.plotInConsole()
//
//    println(s1.evaluate(1, true).contentToString())
//
//    s1.plotInConsole()
//    s2.plotInConsole()
//    sum.plotInConsole(false)
//    sum.plotInConsole()
//
//    println(Signal.sine(0, Note.A_4.freq))

}