package com.example.synth

import android.util.Log
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
    override val frequencies = setOf(0)

    override val data = List(size) { 0f }
}

/**
 * Represents a pure sine wave
 * @param freq frequency of wave
 * @param numPeriods number of times the period will repeat in Signal's interval
 */
class SinSignal(private val freq: Int) : Signal() {
    override val frequencies = setOf(freq)

    override val data = mutableListOf<Float>().apply{
        val periodLength = SAMPLE_RATE / freq

        //Calculate y-values in a single period
        for (i in 0 until periodLength){
            add(sin(TWO_PI * i / periodLength).toFloat())
        }

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

    operator fun plusAssign(that: Signal){ SumSignal(this, that) }
}