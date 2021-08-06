package com.example.synth


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
 * Inspired by Allen Downey's ThinkDSP Python module
 * */
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

    open var period: Int = 0
    open var amp: Float = 1f
    abstract val index: CircularIndex


    abstract fun reset()
    abstract fun evaluateNext(): Float
    abstract fun evaluate(periods: Int, startFromBeginning: Boolean = false): FloatArray
    abstract fun evaluate(destination: FloatArray, startFromBeginning: Boolean = false)
    operator fun plus(that: Signal) = SumSignal(this, that)
    override fun toString() = evaluate(1).contentToString()
}

class FuncSignal(var func: (Int, Int) -> Float,
                 var freq: Int = 440,
                 override var amp: Float = 1f,
                 var offset: Int = 0
): Signal() {
    override val index: CircularIndex
    init{
        period = AudioEngine.SAMPLE_RATE / freq
        index = CircularIndex(period)
    }


    override fun reset() { index.reset() }

    override fun evaluateNext() =
        func(index.getIndexAndIterate()+offset, period) * amp


    override fun evaluate(periods: Int, startFromBeginning: Boolean): FloatArray{
        if (startFromBeginning) reset()
        return FloatArray(period * periods){ evaluateNext() }
    }

    override fun evaluate(destination: FloatArray, startFromBeginning: Boolean) {
        if (startFromBeginning) reset()
        for(i in destination.indices){
            destination[i] = evaluateNext()
        }
    }
}

/** Combines two or more Signals into one Signal. */
class SumSignal(vararg signal: Signal, amp: Float = 1f) : Signal() {
    private val signals = mutableSetOf<Signal>()
    override val index: CircularIndex

    init{
        signals.addAll(signal)
        this.amp = amp
        period = signals.maxByOrNull { it.period }?.period ?: 1
        index = CircularIndex(period)
    }


    override fun reset() {
        signals.forEach { it.index.reset() }
    }

    fun addSignal(newSignal: Signal){
        signals.add(newSignal)
    }

    fun addSignals(newSignals: Collection<Signal>){
        signals.addAll(signals)
    }

    override fun evaluateNext() =
        signals.fold(0f){ sum, signal -> sum + signal.evaluateNext() * amp }

    override fun evaluate(periods: Int, startFromBeginning: Boolean): FloatArray{
        if (startFromBeginning) reset()
        return FloatArray(period * periods){ evaluateNext() }
    }

    override fun evaluate(destination: FloatArray, startFromBeginning: Boolean) {
        if (startFromBeginning) reset()
        for (i in destination.indices) {
            destination[i] = evaluateNext() }
        }

}

fun main(){
    val s1 = FuncSignal(Signal.sine, 440, 0.5f)
    val s2 = FuncSignal(Signal.sine, 880,0.5f)
    val sum = SumSignal(s1, s2)

    println(s1.period)
    println(s2.period)
    println(sum.period)

    sum.evaluate(4, true)
        .plotInConsole()

    s1.evaluate(1, true)
        .plotInConsole()

    s2.evaluate(2, true)
        .plotInConsole()


    println(sum.evaluate(1,true).contentToString())
//    FloatArray(20){ i -> Signal.sine(i, 20) }
//        .plotInConsole()
//
//    val s = FuncSignal(Signal.sine)
//    repeat(20) { println(s.evaluateNext()) }

}