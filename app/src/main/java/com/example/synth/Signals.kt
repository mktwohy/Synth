package com.example.synth


import android.util.Log
import com.example.synth.Constants.TWO_PI
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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

    fun calculatePeriod(){
        period = signals.map{ it.period }.lcm()
    }

    fun addSignal(newSignal: Signal){
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

    println(sum.evaluate(2, true).contentToString())
    s1.plotInConsole()
    s2.plotInConsole()
    sum.plotInConsole()

}