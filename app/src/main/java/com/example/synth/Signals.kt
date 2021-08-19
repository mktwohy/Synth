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

    /** Resets the internal index,
     * which guarantees that [evaluate] and [evaluateToBuffer] start at the beginning */
    abstract fun reset()

    /** Returns the value at a given index */
    abstract fun evaluateAt(i: Int): Float

    /** Uses the Signal's internal index to evaluate the next value in the Signal's sequence */
    abstract fun evaluateNext(): Float

    /** Evaluates the next n periods of the signal as a new array */
    fun evaluate(
        periods: Int,
        useInternalIndex: Boolean = false
    ) =
        if(useInternalIndex)
            FloatArray(period.toInt() * periods){ i -> evaluateNext() }
        else
            FloatArray(period.toInt() * periods){ i -> evaluateAt(i) }

    /** Evaluates the signal fill an existing array */
    fun evaluateToBuffer(
        destination: FloatArray,
        useInternalIndex: Boolean = false
    ) {
        if(useInternalIndex)
            destination.indices.forEach { i -> destination[i] = evaluateNext() }
        else
            destination.indices.forEach { i -> destination[i] = evaluateAt(i) }
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

    override fun evaluateAt(i: Int): Float {
        if (autoNormalize) normalize()
        return signals.fold(0f){ sum, signal ->
            sum + signal.evaluateAt(i) * amp
        }
    }

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
    override fun evaluateAt(i: Int) = 0f
    override fun evaluateNext() = 0f

    override fun toString() = "SilentSignal"
}


class PeriodicSignal(
    var freq: Float = 440f,
    amp: Float = 1f,
    var func: (Int, Float) -> Float = sine
): Signal() {
    override var amp: Float = 1f
        set(value) {
            when{
                value >= 0f -> field = value
                value.isNaN() -> field = 0f
            }
        }
    private var internalIndex: Int = 0
    override val period
        get() = SAMPLE_RATE / freq

    init{ this.amp = amp }

    override fun reset() { internalIndex = 0 }

    override fun evaluateAt(i: Int) = func(i, freq) * amp

    override fun evaluateNext() = evaluateAt(internalIndex++)

    override fun toString(): String {
        val funcName = when(func){
            sine    -> "sine"
            cosine  -> "cosine"
            silence -> "silence"
            else    -> "custom function"
        }
        return "FuncSignal:\n\tfreq = $freq \n\tamp  = $amp \n\tfunc = $funcName"
    }
}


class HarmonicSignal(
    fundamental: Note,
    val harmonicSeries: HarmonicSeries = HarmonicSeries(),
    amp: Float = 1f,
    autoNormalize: Boolean = true
): SignalCollection() {
    override val signals = List(Constants.NUM_HARMONICS){ i ->
        PeriodicSignal(fundamental.freq*(i+1), 0f)
    }

    override val period: Float
        get() = signals.maxOfOrNull { it.period } ?: 1f

    var fundamental = fundamental
        set(value){
            for(i in signals.indices) {
                signals[i].freq = fundamental.freq*(i+1)
            }
            field = value
        }

    init {
        this.fundamental = fundamental
        this.amp = amp
        this.autoNormalize = autoNormalize
        harmonicSeries.registerCallback {
            for((overtone, amplitude) in harmonicSeries){
                signals[overtone-1].amp = amplitude
            }
            log("harmonicSeries:\n$harmonicSeries")
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
    val s1 = PeriodicSignal(Note.A_4.freq, 1f)
    val s2 = PeriodicSignal(Note.A_5.freq,1f)
    val sum = SumSignal(s1, s2)

    sum.plotInConsole()
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