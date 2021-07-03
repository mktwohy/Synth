package com.example.synth

import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlin.math.PI
import kotlin.math.round
import kotlin.math.sin

/*
fun List<Int>.toByteArray(): ByteArray{
    val bytes = ByteArray(size*2)
    var bIndex = 0
    for(i in 0 until size){
//        val byteArray = this[i].toBigInteger().toByteArray()
//        bytes[bIndex] = byteArray[0]
//        bytes[bIndex+1] = byteArray[1]
//        bIndex += 2
        bytes[i] = this[i].toByte()
    }
    return bytes
}
 */

fun List<Int>.toByteArray(): ByteArray {
    val start = System.currentTimeMillis()

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
    val time = System.currentTimeMillis() - start
    Log.d("latencyTest","IntList convert took $time milliseconds")
    return bytes
}


fun ByteArray.toList(bit: Int = 16): List<Int>{
    return if(bit == 8) this.toList()
        else this
            .toList()
            .zipWithNext()
            .filterIndexed{ index, _ -> index % 2 == 0 }
            .map{ it.first + it.second }
            .toList()


}

fun List<Signal>.sum() = when(size){
        0 -> NullSignal()
        1 -> this[0]
        2 -> SumSignal(this[0], this[1])
        else -> this.fold(SinSignal(1)) { result: Signal, next: Signal -> result + next}
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
        val IntToByteArrayLookupTable = run{
            val table = mutableMapOf<Int, ByteArray>()
            val integerRange = -32_768..32_767
            for (i in integerRange){
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




        return audio
    }
}

/**
 * Silent signal. Useful when adding multiple signals together
 * @param length number of samples in ByteArray of data
 */
class NullSignal(size: Int = BUFFER_SIZE): Signal() {
    override val data = List(size) { _ -> 0 }
}

class SumSignal(s1: Signal, s2: Signal): Signal(){
    override val data = s1.data.zip(s2.data).map { (it.first + it.second) % 127 }

    operator fun plusAssign(that: Signal){ SumSignal(this, that) }
}

class SinSignal(private val freq: Int, numPeriods: Int = 100) : Signal() {
    override val data = run{
        val interval     = mutableListOf<Int>()
        val period       = mutableListOf<Int>()
        val periodLength = SAMPLE_RATE / freq

        for (i in 0 until periodLength){
            period.add((sin(TWO_PI * i / periodLength) * 127).toInt())
        }

        repeat(numPeriods){ interval.addAll(period) }

        interval
    }
}

/*
class SinSignal(
    val freq: Int,
) : Signal() {

    override val data = if (freq > 0) generateInterval(SAMPLE_RATE / freq)
                        else ByteArray(BUFFER_SIZE)

    private fun generateInterval(periodLength: Int): ByteArray{
//        val period = generatePeriod(periodLength)
//        val interval = ByteArray(BUFFER_SIZE)
//        Log.d("m_Iterate", "START: PeriodLength: $periodLength")
//        var relativeIndex = 0
//        for (i in 0 until BUFFER_SIZE){
//            Log.d("m_Iterate", "\t$i, ${i%periodLength}")
//            relativeIndex = i - i / periodLength
//            interval[i] = period[i % periodLength].toByte()
//        }
        val period = generatePeriod(periodLength)
        val interval = mutableListOf<Int>()
        while(interval.size < BUFFER_SIZE) { interval.addAll(period) }

        //.subList(0, BUFFER_SIZE)
        return interval.toByteArray()
    }

    private fun generatePeriod(periodLength: Int, start: Int = 0): List<Int>{
        val period = mutableListOf<Int>()
        for (i in 0 until periodLength){
            val angle = (TWO_PI*i / periodLength)
            period.add((127 * sin(angle)).toInt())
        }
        Log.d("m_period", "period: $period")
        return period
    }

}
 */