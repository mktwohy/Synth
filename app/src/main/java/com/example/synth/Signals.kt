package com.example.synth

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlin.math.PI
import kotlin.math.sin




fun List<Int>.normalize(
    lowerBound: Int = Signal.MIN_16BIT_VALUE,
    upperBound: Int = Signal.MAX_16BIT_VALUE
)
= if (size > 0) run {
    //https://stats.stackexchange.com/questions/178626/how-to-normalize-data-between-1-and-1
            val minValue = this.minByOrNull { it }!!
            val maxValue = this.maxByOrNull { it }!!
            this.map { (upperBound - lowerBound) * ( (it - minValue) / (maxValue - minValue) ) + lowerBound }
    }
    else NullSignal().data


fun ByteArray.toList(bit: Int = 16): List<Int>{
    return if(bit == 8) this.toList()
        else this
            .toList()
            .chunked(2)
            .map{ it[0] + it[1] }
            .toList()
}

fun List<Signal>.sum() = when(size){
        0 -> NullSignal()
        1 -> this[0]
        2 -> SumSignal(this[0], this[1])
        else -> this.reduce { sumSig: Signal, nextSig: Signal -> sumSig + nextSig}
}

interface SignalProperties{
    val data: List<Int>
}


abstract class Signal(): SignalProperties{
    companion object{
        const val SAMPLE_RATE       = MainActivity.SAMPLE_RATE
        const val BUFFER_DURATION   = MainActivity.BUFFER_DURATION
        const val BUFFER_SIZE       = MainActivity.BUFFER_SIZE
        const val TWO_PI            = 2.0 * PI
        const val MIN_16BIT_VALUE     = -32_768
        const val MAX_16BIT_VALUE     = 32_767

        val IntToByteArrayLookupTable = run{
            val table = mutableMapOf<Int, ByteArray>()
            for (i in MIN_16BIT_VALUE..MAX_16BIT_VALUE){
                table[i] = i.toBigInteger().toByteArray()
            }
            table
        }

    }

    override fun toString(): String{
        val s = StringBuilder()
        for(byte in data){
            s.append(byte.toInt())
            s.append(" ")
        }
        return s.toString()
    }

    operator fun plus(that: Signal) = SumSignal(this, that)

    fun play(): AudioTrack{
        val start = System.currentTimeMillis() //start latency timer

        val audio = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(44100)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build())
            .setBufferSizeInBytes(data.size)
            .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
            .build()


            audio.write(data.toByteArray(), 0, data.size * 2)
            audio.play()


        Log.d("m_latency",
            "Latency: ${System.currentTimeMillis() - start} ms") //end latency timer

        return audio
    }

    private fun List<Int>.toByteArray(): ByteArray {
        val bytes = ByteArray(size * 2)
        var bIndex = 0
        for (i in indices) {
            val bArray = Signal.IntToByteArrayLookupTable[this[i]]
            if (bArray != null){
                if (bArray.size > 1) bytes[bIndex] = bArray[1]
                bytes[bIndex+1] = bArray[0]
            }
            bIndex += 2
        }
        return bytes
    }
}

/**
 * Silent signal.
 * @param length number of samples in ByteArray of data
 */
class NullSignal(size: Int = BUFFER_SIZE): Signal() {
    override val data = List(size) { _ -> 0 }
}


/**
 * Represents a pure sine wave
 * @param freq frequency of wave
 * @param numPeriods number of times the period will repeat in Signal's interval
 */
class SinSignal(private val freq: Int, numPeriods: Int = 100) : Signal() {
    override val data = run{
        val interval     = mutableListOf<Int>()
        val period       = mutableListOf<Int>()
        val periodLength = SAMPLE_RATE / freq

        //Calculate y-values in a single period
        for (i in 0 until periodLength){
            period.add((sin(TWO_PI * i / periodLength) * MAX_16BIT_VALUE).toInt())
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
                            .normalize()



    operator fun plusAssign(that: Signal){ SumSignal(this, that) }
}