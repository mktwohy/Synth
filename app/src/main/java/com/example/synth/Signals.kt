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

//    fun lcm(a: Int, b: Int): Int =
//        a / gcd(a, b) * b

    private fun List<Int>.lcm(): Int{
        //https://www.geeksforgeeks.org/gcd-two-array-numbers/
        fun gcd(a: Int, b: Int): Int =
            if (a == 0) b
            else gcd(b % a, a)
        var lcm = this[0]
        for (i in this.indices) {
            lcm *= gcd(lcm, this[i])
        }
        Log.d("m_lcm","$lcm")
        return lcm
    }

    operator fun plusAssign(that: Signal){ SumSignal(this, that) }
}



//---Extension Functions---
fun List<Float>.loopToFill(newSize: Int): List<Float>{
    val newList = mutableListOf<Float>()
    repeat((newSize / this.size) + 1) { newList.addAll(this) }
    return newList.subList(0, newSize - 1)
}

fun List<Int>.toCircularShortArray(): CircularShortArray{
    val ret = CircularShortArray(this.size)
    for(i in this.indices){
        ret[i] = this[i].toShort()
    }
    return ret
}

fun List<Signal>.sum() = when(size){
    0 -> NullSignal()
    1 -> this[0]
    2 -> SumSignal(this[0], this[1])
    else -> this.reduce { sumSig: Signal, nextSig: Signal ->
        sumSig + nextSig
    }
}

//https://stats.stackexchange.com/questions/178626/how-to-normalize-data-between-1-and-1
private fun List<Float>.normalize(
    lowerBound: Float = -1f,
    upperBound: Float = 1f
) =
    if (size > 0) {
        val minValue = this.minByOrNull { it }!!
        val maxValue = this.maxByOrNull { it }!!

        if (minValue >= -1 && maxValue <= 1) this
        else this.map {
            (upperBound - lowerBound) * ( (it - minValue) / (maxValue - minValue) ) + lowerBound
        }
    }
    else NullSignal().data


private fun List<Float>.toIntList(scalar: Int) =
    this.map { (it * scalar).toInt() }