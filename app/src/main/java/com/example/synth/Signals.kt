package com.example.synth

import android.util.Log
import java.lang.Exception
import kotlin.math.PI
import kotlin.math.sin

interface SignalProperties{
    val data: List<Float>
    val pcmData: CircularShortArray
    val frequencies: Set<Int>
}


/** Generates a sound and the associated PCM data, which can be played by an AudioTrack */
abstract class Signal: SignalProperties{
    override val pcmData: CircularShortArray by lazy {
        data.normalize().toIntList(MAX_16BIT_VALUE).toCircularShortArray()
    }

    companion object{
        const val SAMPLE_RATE       = MainActivity.SAMPLE_RATE
        const val BUFFER_SIZE       = MainActivity.BUFFER_SIZE
        const val TWO_PI              = 2.0 * PI
        const val MIN_16BIT_VALUE     = -32_768
        const val MAX_16BIT_VALUE     = 32_767
        val NullSignal = NullSignal(BUFFER_SIZE)
    }

    abstract fun transpose(step: Int): Signal

    override fun toString(): String{
        val s = StringBuilder()
        for(value in data){
            s.append(value)
            s.append(" ")
        }
        return s.toString()
    }

    operator fun plus(that: Signal) =
        SumSignal(this, that)
}


/**
 * Represents a silent signal.
 * @param size number of samples in ByteArray of data
 */
class NullSignal(size: Int = BUFFER_SIZE): Signal() {
    override val frequencies = setOf(0)
    override val data = List(size) { 0f }
    override fun transpose(step: Int) = NullSignal
}


/**
 * Represents a pure sine wave
 * @param freq frequency of wave
 */
class SinSignal(private val freq: Int) : Signal() {
    init{
        if (freq == 0) throw Exception("For freq == 0, use NullSignal")
    }

    override val frequencies = setOf(freq)
    override val data = mutableListOf<Float>().apply{
        val periodLength = SAMPLE_RATE / freq

        //Calculate y-values in a single period
        for (i in 0 until periodLength){
            add(sin(TWO_PI * i / periodLength).toFloat())
        }
    }

    override fun transpose(step: Int): Signal {
        val fundFreq = frequencies.minByOrNull { it }
            ?: return NullSignal
        return SinSignal( (fundFreq * Interval.stepToRatio(step)) .toInt() )
    }
}


/**
 * Creates a combined Signal of two Signal objects.
 * @param s1 first signal in sum
 * @param s2 second signal in sum
 */
class SumSignal(s1: Signal, s2: Signal): Signal(){

    override val frequencies = (s1.frequencies + s2.frequencies).toSet()

    override val data = mutableListOf<Float>().apply{
            val intervalLength = lcm(s1.pcmData.size, s2.pcmData.size)
            val s1Looped = s1.data.loopToFill(intervalLength)
            val s2Looped = s2.data.loopToFill(intervalLength)

            for (i in 0 until intervalLength){
                add(s1Looped[i] + s2Looped[i])
            }
        }

    override fun transpose(step: Int): Signal {
        val ratio = Interval.stepToRatio(step)
        val transposedSignals = mutableListOf<Signal>()
        for(f in frequencies){
            transposedSignals.add(SinSignal(f * ratio.toInt()))
        }
        return transposedSignals.sum()
    }

    //https://www.geeksforgeeks.org/gcd-two-array-numbers/
    private fun gcd(a: Int, b: Int): Int =
        if (a == 0) b
        else gcd(b % a, a)

    private fun lcm(a: Int, b: Int): Int =
        a / gcd(a, b) * b

    operator fun plusAssign(that: Signal){ SumSignal(this, that) }


}