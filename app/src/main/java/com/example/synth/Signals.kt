package com.example.synth

import android.util.Log
import kotlin.math.PI
import kotlin.math.sin

interface SignalProperties{
    val data: List<Float>
    val pcmData: CircularShortArray
    val frequencies: List<Int>
}

/** Generates a sound and the associated PCM data, which can be played by an AudioTrack */
abstract class Signal: SignalProperties{
    private var chunkStartIndex: Int = 0

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
 * Silent signal.
 * @param length number of samples in ByteArray of data
 */
class NullSignal(size: Int = BUFFER_SIZE): Signal() {
    override val data = List(size) { 0f }

    override val frequencies = listOf(0)
}

/**
 * Represents a pure sine wave
 * @param freq frequency of wave
 * @param numPeriods number of times the period will repeat in Signal's interval
 */
class SinSignal(private val freq: Int) : Signal() {
    override val frequencies = listOf(freq)

    override val data = run{
        val period       = mutableListOf<Float>()
        val periodLength = SAMPLE_RATE / freq

        //Calculate y-values in a single period
        for (i in 0 until periodLength){
            period.add(sin(TWO_PI * i / periodLength).toFloat())
        }

        period
    }
}

/**
 * Creates a combined Signal of two Signal objects.
 * @param s1 first signal in sum
 * @param s2 second signal in sum
 */
class SumSignal(s1: Signal, s2: Signal): Signal(){
    override val frequencies = s1.frequencies + s2.frequencies

    override val data =
        run {
            val intervalLength = SAMPLE_RATE / frequencies.lcm()
            val sum = mutableListOf<Float>()
            val s1Looped = s1.data.loopToFill(intervalLength)
            val s2Looped = s2.data.loopToFill(intervalLength)
            for (i in 0 until intervalLength){
                sum.add(s1Looped[i] + s2Looped[i])
            }
            sum.toList()
        }

    operator fun plusAssign(that: Signal){ SumSignal(this, that) }
}