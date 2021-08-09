package com.example.synth


import android.util.Log
import com.example.synth.Constants.MAX_16BIT_VALUE
import com.example.synth.Constants.TWO_PI
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin

object Constants{
    const val TWO_PI              = 2.0 * PI.toFloat()
    const val MIN_16BIT_VALUE     = -32_768
    const val MAX_16BIT_VALUE     = 32_767
}

/** Represents a time-varying signal.
 * Inspired by Allen Downey's ThinkDSP Python module */
interface Signal{
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

    var period: Int
    var amp: Float

    /** Guarantees that [evaluate] and [evaluateTo] start at the beginning*/
    fun reset()
    /** Returns the next value in the Signal's sequence */
    fun evaluateNext(): Float
    /** Evaluates the next n periods of the signal as a new array */
    fun evaluate(periods: Int, startFromBeginning: Boolean = false): FloatArray
    /** Evaluates the next n periods of the signal to an existing array */
    fun evaluateTo(destination: FloatArray, startFromBeginning: Boolean = false)
    fun plus(that: Signal) = SumSignal(this, that)
}

object SilentSignal: Signal {
    override var period: Int = AudioEngine.BUFFER_SIZE
    override var amp: Float = 0f

    override fun reset() { }
    override fun evaluateNext() = 0f
    override fun evaluate(size: Int, startFromBeginning: Boolean): FloatArray {
        if(startFromBeginning) reset()
        return FloatArray(size){ 0f }
    }
    override fun evaluateTo(destination: FloatArray, startFromBeginning: Boolean) {
        if(startFromBeginning) reset()
        for(i in destination.indices){
            destination[i] = 0f
        }
    }
    override fun toString() = "SilentSignal"
}


class FuncSignal(var func: (Int, Int) -> Float,
                 var freq: Int = 440,
                 override var amp: Float = 1f,
                 var offset: Int = 0
): Signal {
    override var period: Int = 0
    private val index: CircularIndex
    init{
        period = AudioEngine.SAMPLE_RATE / freq
        index = CircularIndex(period)
    }

    override fun reset() { index.reset() }

    override fun evaluateNext(): Float {
        return func(index.getIndexAndIterate()+offset, period) * amp
    }



    override fun evaluate(periods: Int, startFromBeginning: Boolean): FloatArray{
        if (startFromBeginning) reset()
        return FloatArray(period * periods){ evaluateNext() }
    }

    override fun evaluateTo(destination: FloatArray, startFromBeginning: Boolean) {
        if (startFromBeginning) reset()
        for(i in destination.indices){
            destination[i] = evaluateNext()
        }
    }

    override fun toString() = "FuncSignal(p: $period, a: $amp, f: $freq, o: $offset)"

}

/** Combines two or more Signals into one Signal. */
open class SumSignal(vararg signal: Signal, override var amp: Float = 1f) : Signal {
    private val signals = mutableSetOf<Signal>()
    private val index: CircularIndex
    override var period: Int = 0

    init{
        this.amp = amp
        signals.addAll(signal)
        period = signals.map{ it.period }.lcm()
        index = CircularIndex(period)
    }



    override fun reset() {
        signals.forEach { it.reset() }
    }

    fun clear(){
        signals.clear()
    }

    fun addSignal(newSignal: Signal){
        when(newSignal){
            is FuncSignal -> signals.add(newSignal)
            is SumSignal  -> signals.addAll(newSignal.signals)
        }
        period = signals.map{ it.period }.lcm()
        index.maxValue = period
    }

    fun addSignals(newSignals: Collection<Signal>){
        for(signal in newSignals){
            when(signal){
                is FuncSignal -> signals.add(signal)
                is SumSignal  -> signals.addAll(signal.signals)
            }
        }

        period = signals.map{ it.period }.lcm()
        index.maxValue = period
    }

    override fun evaluateNext() =
        signals.fold(0f){ sum, signal -> sum + signal.evaluateNext() * amp }

    override fun evaluate(periods: Int, startFromBeginning: Boolean): FloatArray{
        if (startFromBeginning) reset()
        return FloatArray(period * periods){ evaluateNext() }
    }

    override fun evaluateTo(destination: FloatArray, startFromBeginning: Boolean) {
        if (startFromBeginning) reset()
        for (i in destination.indices) {
            destination[i] = evaluateNext() }
        }

    operator fun plusAssign(that: Signal){ addSignal(that) }
}

class HarmonicSignal(
    val func: (Int, Int) -> Float,
    var fundamental: Int,
    val harmonicSeries: Map<Int, Int>
): SumSignal() {
    private val signals = mutableSetOf<Signal>()
    private val index: CircularIndex

    init{
        this.amp = amp
        signals.add(FuncSignal(func, fundamental))
        period = signals.map{ it.period }.lcm()
        index = CircularIndex(period)
    }

}

fun main(){
    val s1 = FuncSignal(Signal.sine, 440, 1f)
    val s2 = FuncSignal(Signal.sine, 880,1f)
    val sum = SumSignal(s1, s2)

    s1.plotInConsole()
    s2.plotInConsole()
    sum.plotInConsole()




}