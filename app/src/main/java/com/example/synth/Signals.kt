package com.example.synth


import androidx.compose.runtime.toMutableStateMap
import com.example.synth.AudioEngine.Companion.SAMPLE_RATE
import com.example.synth.Constants.TWO_PI
import kotlin.math.*

object Constants{
    const val TWO_PI              = 2.0 * PI.toFloat()
    const val MIN_16BIT_VALUE     = -32_768
    const val MAX_16BIT_VALUE     = 32_767
    const val NUM_HARMONICS       = 25
}

interface SignalUpdatedListener{ fun onSignalUpdated(signal: Signal) }


/** Represents a time-varying signal.
 * Inspired by Allen Downey's ThinkDSP Python module */
abstract class Signal{
    companion object Functions{
        val sine = { i: Int, freq: Float ->
            sin(TWO_PI * i * freq / SAMPLE_RATE).toFloat()
        }
        val cosine = { i: Int, freq: Float ->
            cos(TWO_PI * i * freq / SAMPLE_RATE).toFloat()
        }
        val silence = { _: Int, _: Int ->
            0f
        }

        val fundamental = { i: Int -> i == 1 }
        val odd         = { i: Int -> i % 2 != 0 }
        val even        = { i: Int -> i % 2 == 0 }
        val all         = { _: Int -> true }
        val none        = { _: Int -> false }


        /** produces a harmonic series with exponential decay
         * (represented as a map of overtones to amplitude) */
        fun harmonicSeries(
            start: Int,
            end: Int,
            decayRate: Float,
            floor: Float,
            filter: (Int) -> Boolean = all
        ): MutableMap<Int, Float> {
            val harmonics = (start..end).filter{ harmonic -> filter(harmonic)}
            return harmonics
                .mapIndexed{ i, harmonic ->
                    harmonic to  ((1f-floor) * (1f-decayRate).pow(i) + floor)
                }
                .toMutableStateMap()
        }

        fun sumSignalFromHarmonicSeries(
            harmonicSeries: Map<Int, Float>,
            fundamental: Note,
            func: (Int, Float) -> Float = sine
        ) = SumSignal(signalsFromHarmonicSeries(harmonicSeries, fundamental, func))
            .also{log("sumsignal: $it")}

        fun signalsFromHarmonicSeries(
            harmonicSeries: Map<Int, Float>,
            fundamental: Note,
            func: (Int, Float) -> Float = sine
        ): MutableSet<Signal> =
            harmonicSeries
                .map { (overtone, amplitude) ->
                    PeriodicSignal(fundamental.freq*overtone, amplitude, func)
                }
                .toMutableSet()
    }

    abstract var period: Int
    abstract var amp: Float

    /** Guarantees that [evaluate] and [evaluateTo] start at the beginning*/
    abstract fun reset()
    /** Returns the next value in the Signal's sequence */
    abstract fun evaluateNext(): Float

    /** Evaluates the next n periods of the signal as a new array */
    fun evaluate(periods: Int, startFromBeginning: Boolean): FloatArray{
        if (startFromBeginning) reset()
        return FloatArray(period * periods){ evaluateNext() }
    }

    /** Evaluates the next n periods of the signal to an existing array */
    fun evaluateTo(destination: FloatArray, startFromBeginning: Boolean) {
        if (startFromBeginning) reset()
        destination.indices.forEach { i -> destination[i] = evaluateNext() }
    }

    fun plus(that: Signal) = SumSignal(mutableSetOf(this, that))
}

object SilentSignal: Signal() {
    override var period: Int = AudioEngine.BUFFER_SIZE
    override var amp: Float = 0f

    override fun reset() { }
    override fun evaluateNext() = 0f
    override fun toString() = "SilentSignal"
}


class PeriodicSignal(
    var freq: Float = 440f,
    override var amp: Float = 1f,
    var func: (Int, Float) -> Float = sine
): Signal() {
    override var period: Int = 0
    private val index: CircularIndex
    init{
        period = (SAMPLE_RATE / freq).toInt()
        index = CircularIndex(period)
    }

    override fun reset() { index.reset() }

    override fun evaluateNext() =
        func(index.getIndexAndIterate(), freq) * amp

    override fun toString() = "FuncSignal(p: $period, a: $amp, f: $freq)"
}

class HarmonicSignal(
    fundamental: Note,
    harmonicSeries: Map<Int, Float> = mutableMapOf(),
    private val autoNormalize: Boolean = true
): Signal(){
    override var amp: Float = 1f
        set(value){
            if(value >= 0) field = value
            if (autoNormalize) normalize()
        }

    override var period: Int = 0

    var fundamental: Note = Note.A_4
        set(value){
            for(i in signals.indices) {
                signals[i].freq = fundamental.freq*(i+1)
            }
            field = value
            calculatePeriod()
        }

    private val signals = List(Constants.NUM_HARMONICS){ i ->
        PeriodicSignal(fundamental.freq*(i+1), 0f)
    }

    init {
        this.fundamental = fundamental
        calculatePeriod()
        updateHarmonicSeries(harmonicSeries)
    }

    override fun reset() { signals.forEach { it.reset() } }

    fun updateHarmonicSeries(harmonicSeries: Map<Int, Float>){
        for((overtone, amplitude) in harmonicSeries) {
            signals[overtone-1].amp = amplitude
        }
        calculatePeriod()
    }

    fun calculatePeriod() = signals.minOfOrNull { it.period } ?: 1

    override fun evaluateNext() =
        signals.fold(0f){ sum, signal -> sum + signal.evaluateNext() * amp }

    private fun normalize() {
        if (signals.isEmpty()) return

        val ampSum = signals.map { it.amp }.sum()
        signals.forEach { it.amp = (it.amp / ampSum) * this.amp  }
    }
}

/** Combines two or more Signals into one Signal. */
class SumSignal(
    private val signals: MutableSet<Signal> = mutableSetOf(),
    amp: Float = 1f,
    private val autoNormalize: Boolean = true
) : Signal() {

    override var amp: Float = 1f
        set(value){
            if(value >= 0) field = value
            if (autoNormalize) normalize()
        }
    override var period: Int = 1

    init {
        this.amp = amp
        calculatePeriod()
    }

    constructor(vararg signal: Signal, amp: Float = 1f, autoNormalize: Boolean = true)
            : this(signal.toMutableSet(), amp, autoNormalize)


    fun clear(){
        if (autoNormalize) normalize()
        calculatePeriod()
        signals.clear()
    }

    private fun normalize() {
        if (signals.isEmpty()) return

        val ampSum = signals.map { it.amp }.sum()
        signals.forEach { it.amp = (it.amp / ampSum) * this.amp  }
    }

    private fun calculatePeriod(){
        period = signals.map{ it.period }.lcm()
    }

    private fun addSignal(newSignal: Signal){
        when(newSignal){
            is PeriodicSignal -> signals.add(newSignal)
            is SumSignal  -> signals.addAll(newSignal.signals)
        }
        if (autoNormalize) normalize()
        calculatePeriod()
    }

    fun addSignals(newSignals: Collection<Signal>){
        for(signal in newSignals){
            addSignal(signal)
        }
        if (autoNormalize) normalize()
        calculatePeriod()
    }

    override fun reset() { signals.forEach { it.reset() } }

    override fun evaluateNext() =
        signals.fold(0f){ sum, signal -> sum + signal.evaluateNext() * amp }

    override fun toString() = "SumSignal(a: $amp s: \n\t$signals)"
    operator fun plusAssign(that: Signal){ addSignal(that) }
}

fun main(){
    val s1 = PeriodicSignal(Note.A_4.freq, 1f)
    val s2 = PeriodicSignal(Note.A_5.freq,1f)
    val sum = SumSignal(s1, s2)
    val s = SumSignal(
        mutableSetOf(s1,s2)
    )
    print(s.evaluate(1,true).contentToString())
    s.plotInConsole()
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