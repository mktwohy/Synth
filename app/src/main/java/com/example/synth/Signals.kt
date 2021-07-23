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
    init {
        Log.d("m_Signal", "NullSignal created!")
    }
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
        Log.d("m_Signal", "SinSignal(freq) created!")
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
class SumSignal: Signal{

    override val frequencies: Set<Int>

    override val data: List<Float>

    constructor(s1: Signal, s2: Signal){
        Log.d("m_Signal", "SumSignal(s1,s2) created!")
        frequencies = (s1.frequencies + s2.frequencies).toSet()
        data = mutableListOf<Float>().apply{
            val intervalLength = lcm(s1.pcmData.size, s2.pcmData.size)
            var s1Index = CircularIndex(s1.data.size)
            var s2Index = CircularIndex(s2.data.size)

            for (i in 0 until intervalLength){
                this.add(s1.data[s1Index.i] + s2.data[s2Index.i])
                s1Index.iterate()
                s2Index.iterate()
            }
        }
    }

    constructor(signals: Set<Signal>){
        Log.d("m_Signal", "SumSignal(set<Signal>) created!")
        frequencies = mutableSetOf<Int>().apply {
            for(s in signals){
                addAll(s.frequencies)
            }
        }.toSet()

        data = mutableListOf<Float>().apply{
            val datas = signals.map { it.data }
            val intervalLength = datas.map { it.size }.lcm()
            val dataToIndex = mutableMapOf<List<Float>, CircularIndex>().apply{
                for (d in datas){
                    this[d] = CircularIndex(d.size)
                }
            }

            for (i in 0 until intervalLength){
                this.add( datas.reduce { acc, d -> acc + d[dataToIndex[d]!!.i] }.sum() )
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


    operator fun plusAssign(that: Signal){ SumSignal(this, that) }


}