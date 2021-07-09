package com.example.synth

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlin.math.PI
import kotlin.math.sin


fun List<Signal>.sum() = when(size){
        0 -> NullSignal()
        1 -> this[0]
        2 -> SumSignal(this[0], this[1])
        else -> this.reduce { sumSig: Signal, nextSig: Signal -> sumSig + nextSig}
}

interface SignalProperties{
    val data: List<Float>
    val pcmData: ShortArray
}

/** Generates a sound and the associated PCM data, which can be played by an AudioTrack */
abstract class Signal: SignalProperties{
    override val pcmData: ShortArray by lazy {
        data.normalize().toIntArray().toShortArray()
    }


    companion object{
        const val SAMPLE_RATE       = MainActivity.SAMPLE_RATE
        const val BUFFER_SIZE       = MainActivity.BUFFER_SIZE
        const val TWO_PI              = 2.0 * PI
        const val MIN_16BIT_VALUE     = -32_768
        const val MAX_16BIT_VALUE     = 32_767
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

        private fun List<Float>.toIntArray(scalar: Int = MAX_16BIT_VALUE) =
            this.map { (it * scalar).toInt() }

        private fun List<Int>.toShortArray() =
            this.map { it.toShort() }.toShortArray()


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
}

/**
 * Represents a pure sine wave
 * @param freq frequency of wave
 * @param numPeriods number of times the period will repeat in Signal's interval
 */
class SinSignal(private val freq: Float) : Signal() {
    override val data = run{
        val interval     = mutableListOf<Float>()
        val period       = mutableListOf<Float>()
        val periodLength = SAMPLE_RATE / freq .toInt()
        val numPeriods   = (BUFFER_SIZE / periodLength)
        Log.d("m_period", "$numPeriods periods")

        //Calculate y-values in a single period
        for (i in 0 until periodLength){
            period.add(sin(TWO_PI * i / periodLength).toFloat())
        }

        //Repeat this period until it is the desired length
        repeat(numPeriods){ interval.addAll(period) }
        interval
    }

}

/**
 * Creates a combined Signal of two Signal objects.
 * @param s1 first signal in sum
 * @param s2 second signal in sum
 */
class SumSignal(s1: Signal, s2: Signal): Signal(){
    override val data = s1.data
                            .zip(s2.data)
                            .map { it.first + it.second }

    operator fun plusAssign(that: Signal){ SumSignal(this, that) }
}