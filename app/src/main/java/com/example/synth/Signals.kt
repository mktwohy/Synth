package com.example.synth


import com.example.synth.Constants.TWO_PI
import kotlin.math.*

object Constants{
    const val TWO_PI              = 2.0 * PI.toFloat()
    const val MIN_16BIT_VALUE     = -32_768
    const val MAX_16BIT_VALUE     = 32_767
}

/** Represents a time-varying signal.
 * Inspired by Allen Downey's ThinkDSP Python module */
abstract class Signal{
    companion object Functions{
        val sine = { i: Int, p: Int ->
            sin(TWO_PI * i / p).toFloat()
        }
        val cosine = { i: Int, p: Int ->
            cos(TWO_PI * i / p).toFloat()
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
            filter: (Int) -> Boolean
        ): Map<Int, Float> {
            val harmonics = (start..end).filter{ harmonic -> filter(harmonic)}
            return harmonics
                .mapIndexed{ i, harmonic ->
                    harmonic to  ((1f-floor) * (1f-decayRate).pow(i) + floor)
                }
                .toMap()
        }

        fun signalsFromHarmonicSeries(
            harmonicSeries: Map<Int, Float>,
            fundamentalFreq: Int,
            func: (Int, Int) -> Float
        ): MutableSet<Signal> =
            harmonicSeries
                .map { (overtone, amplitude) ->
                    FuncSignal(func, fundamentalFreq*overtone, amplitude)
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


class FuncSignal(var func: (Int, Int) -> Float,
                 var freq: Int = 440,
                 override var amp: Float = 1f,
): Signal() {
    override var period: Int = 0
    private val index: CircularIndex
    init{
        period = AudioEngine.SAMPLE_RATE / freq
        index = CircularIndex(period)
    }

    override fun reset() { index.reset() }

    override fun evaluateNext() =
        func(index.getIndexAndIterate(), period) * amp


    override fun toString() = "FuncSignal(p: $period, a: $amp, f: $freq)"

}

/** Combines two or more Signals into one Signal. */
class SumSignal(
    private val signals: MutableSet<Signal> = mutableSetOf(),
    override var amp: Float = 1f
) : Signal() {
    private val index = CircularIndex(1)
    override var period: Int = 0
        set(value){
            index.maxValue = value
            field = value
        }

    init { calculatePeriod() }

    constructor(vararg signal: Signal, amp: Float = 1f)
            : this(signal.toMutableSet(), amp)


    fun clear(){ signals.clear() }

    fun normalize(upperBound: Float) {
        if (signals.isEmpty()) return

        val ampSum = signals.map { it.amp }.sum()
        signals.forEach { it.amp = (it.amp / ampSum) * upperBound  }
    }

    private fun calculatePeriod(){
        period = signals.map{ it.period }.lcm()
    }

    private fun addSignal(newSignal: Signal){
        when(newSignal){
            is FuncSignal -> signals.add(newSignal)
            is SumSignal  -> signals.addAll(newSignal.signals)
        }
        calculatePeriod()
    }

    fun addSignals(newSignals: Collection<Signal>){
        for(signal in newSignals){
            when(signal){
                is FuncSignal -> signals.add(signal)
                is SumSignal  -> signals.addAll(signal.signals)
            }
        }
        calculatePeriod()
    }

    override fun reset() { signals.forEach { it.reset() } }

    override fun evaluateNext() =
        signals.fold(0f){ sum, signal -> sum + signal.evaluateNext() * amp }


    operator fun plusAssign(that: Signal){ addSignal(that) }
}

fun main(){
    val s1 = FuncSignal(Signal.sine, 440, 1f)
    val s2 = FuncSignal(Signal.sine, 880,1f)
    val sum = SumSignal(s1, s2)

//    s1.plotInConsole()
//    s2.plotInConsole()
//    sum.plotInConsole(false)
//    sum.normalize(1f)
//    sum.plotInConsole()

    val sums = listOf(
        SumSignal(s1, s2),
        SumSignal(s1, s2, FuncSignal(Signal.sine, Note.B_4.freq)),
//        SumSignal(
//            Signal.signalsFromHarmonicSeries(
//                Signal.harmonicSeries(1, 20, 0.8f, 0.1f, Signal.odd),
//                Note.C_4.freq,
//                Signal.sine
//            )
//        )
    )
    for(s in sums){
        s.plotInConsole(false)
        s.normalize(1f)
        s.plotInConsole()

    }

    val hs = Signal.harmonicSeries(1, 15, 0.5f, 0.01f, Signal.odd)
    println(hs)
}