package com.example.synth

import android.util.Log
import kotlin.math.PI
import kotlin.math.sin

interface SignalProperties{
    /** A List<FLoat>*/
    val data: IntArray
    val frequencies: Set<Int>
}


/** Represents a sound, which can be converted to PCM data to be played by an AudioTrack */
abstract class Signal: SignalProperties{
    open fun dataToPcm(): CircularShortArray = data.normalize().toCircularShortArray()

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

    operator fun plusAssign(that: Signal){ SumSignal(this, that) }
}


/**
 * Represents a silent signal.
 * @param size number of samples in ByteArray of data
 */
class NullSignal(size: Int = BUFFER_SIZE): Signal() {
    override val frequencies = setOf(0)
    override val data = IntArray(size)
    override fun dataToPcm() = CircularShortArray(BUFFER_SIZE)
    override fun transpose(step: Int) = NullSignal
}


/**
 * Represents a pure sine wave
 * @param freq frequency of wave
 */
class SinSignal(private val freq: Int) : Signal() {
    override val frequencies = setOf(freq)
    override val data = run{
        val period = SAMPLE_RATE / freq
        IntArray(period) { i -> (sin(TWO_PI * i / period) * MAX_16BIT_VALUE).toInt() }
    }

    override fun transpose(step: Int): Signal {
        val fundFreq = frequencies.minByOrNull { it }
            ?: return NullSignal
        return SinSignal( (fundFreq * Interval.stepToRatio(step)) .toInt() )
    }
}


/** Combines two or more Signals into one Signal. */
class SumSignal: Signal{
    override val frequencies: Set<Int>
    override val data: IntArray

    /**@param s1 first signal in sum
     * @param s2 second signal in sum */
    constructor(s1: Signal, s2: Signal){
        frequencies = (s1.frequencies + s2.frequencies).toSet()
        data = IntArray(lcm(s1.data.size, s2.data.size)).apply{
            val s1Index = CircularIndex(s1.data.size)
            val s2Index = CircularIndex(s2.data.size)

            for (i in this.indices){
                this[i] = (s1.data[s1Index.i] + s2.data[s2Index.i])
                s1Index.iterate()
                s2Index.iterate()
            }
        }
    }

    /** @param signals a set of Signals to sum together */
    constructor(signals: Set<Signal>){
        frequencies = mutableSetOf<Int>().apply {
            for(s in signals){
                addAll(s.frequencies)
            }
        }.toSet()

        val dataArrays = signals.map { it.data }
        data = IntArray(dataArrays.map { it.size }.lcm()).apply{
            val dataToIndex = mutableMapOf<IntArray, CircularIndex>().apply{
                for (arr in dataArrays){
                    this[arr] = CircularIndex(arr.size)
                }
            }

            for (i in this.indices){
                this[i] = ( dataArrays.reduce { acc, d -> acc + d[dataToIndex[d]!!.i] }.sum() )
                dataToIndex.values.forEach{ circularIndex -> circularIndex.iterate() }
            }

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
}