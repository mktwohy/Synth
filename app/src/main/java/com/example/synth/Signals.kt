package com.example.synth


import com.example.synth.Constants.TWO_PI
import com.example.synth.Constants.SAMPLE_RATE
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

    fun changeVolume(perceivedVolume: Float) { amp = perceivedVolume.pow(4) }

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
    var autoNormalize: Boolean = true

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
    val clock: Clock = Clock(440f),
    amp: Float = 1f,
    var func: (Float) -> Float = { angle -> sin(angle*Constants.PI) },
): Signal() {
    override var amp: Float = amp
        set(value) {
            when{
                value >= 0f -> field = value
                value.isNaN() -> field = 0f
            }
        }
    override val period get() = clock.period

    var pitchBend: Float = 1f
        set(value) {
            field = value
            clock.frequencyBend = pitchBend
        }

    override fun reset() { this.clock.reset() }

    override fun evaluateNext() =  func(clock.angle) * amp
        .also { clock.tick() }

    override fun toString(): String {
        val funcName = when(func){
            sine    -> "sine"
            cosine  -> "cosine"
            silence -> "silence"
            else    -> "custom function"
        }
        return "FuncSignal:\n\tnote = ${clock.frequency} \n\tamp  = $amp \n\tfunc = $funcName"
    }
}

class HarmonicSignal(
    fundamental: Note,
    val harmonicSeries: HarmonicSeries = HarmonicSeries(),
    amp: Float = 1f,
    autoNormalize: Boolean = true
): SignalCollection() {
    override val period = SAMPLE_RATE / fundamental.freq
    override val signals = List(Constants.NUM_HARMONICS){ i ->
        PeriodicSignal(Clock(fundamental.freq*(i+1)), 0f)
    }

    var fundamental: Note = Note.A_4
        set(value){
            field = value
            for(i in signals.indices) {
                signals[i].clock.frequency = fundamental.freq*(i+1)
            }
        }

    fun bend(multiplier: Float){
        signals.forEach { it.pitchBend = multiplier }
    }

    init {
        this.fundamental = fundamental
        this.amp = amp
        this.autoNormalize = autoNormalize
        harmonicSeries.registerCallback {
            for((overtone, amplitude) in harmonicSeries){
                signals[overtone-1].amp = amplitude
            }
        }
    }
}

/** Combines two or more Signals into one Signal. */
class SumSignal(
    signals: Collection<Signal>,
    amp: Float = 1f,
    autoNormalize: Boolean = true
) : SignalCollection() {

    constructor(vararg signal: Signal, amp: Float = 1f, autoNormalize: Boolean = true)
            : this(signal.toSet(), amp, autoNormalize)

    override val signals = mutableSetOf<Signal>()
    override val period
        get() = signals.map{ it.period.toInt() }.lcm().toFloat()

    init {
        this.signals.addAll(signals)
        this.amp = amp
        this.autoNormalize = autoNormalize
    }

    operator fun plusAssign(that: Signal){
        when(that){
            is SignalCollection -> this.signals.addAll(that.signals)
            else                -> this.signals.add(that)
        }
    }
}

fun main(){
    val s1 = PeriodicSignal(Clock(Note.A_4.freq))
    val s2 = PeriodicSignal(Clock(Note.A_3.freq))
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