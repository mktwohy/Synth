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
    override fun toString() = "Signal \nperiod: $period, amp: $amp"

    fun plotInConsole(allowClipping: Boolean = true, periods: Int = 1, scale: Int = 23){
        val lowerBound = 0
        val upperBound = (scale.toFloat()+1).toInt()
        val middle = (upperBound + lowerBound) / 2

        val values = evaluate(periods)
        var min = values.minOrNull()!!
        var max = values.maxOrNull()!!

        val valueToString = values
            .apply {
                if (!allowClipping && (min < -1f || max > 1f)){
                    min = -1f
                    max = 1f
                    normalize(min, max)
                }
                normalize(middle + min*scale/2, (middle + max*scale/2))
            }
            .map { value -> value to CharArray(scale+2){ ' ' } } // move up one

        for((value, string) in valueToString){
            string[lowerBound]      = '='
            string[upperBound/2]    = '-'
            string[upperBound]      = '='

            when{
                value >= upperBound  -> string[upperBound] = '!'
                value <= lowerBound  -> string[lowerBound] = '!'
                else                 -> string[value.toInt()] = '#'

            }
        }

        println(this)
        for(i in (scale+1 downTo 0)){
            for((_, string) in valueToString){
                print(string[i])
            }
            println()
        }
        println("\n\n")
    }
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

    override fun toString() = "FuncSignal \nperiod: $period, amp: $amp, freq: $freq, offset: $offset"

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

    sum.plotInConsole()

    s1.plotInConsole()

    s2.plotInConsole()

    FuncSignal(Signal.sine, 200,2f).plotInConsole(false, 1, 43)


}