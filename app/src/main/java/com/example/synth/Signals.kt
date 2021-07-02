package com.example.synth

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import java.lang.StringBuilder
import kotlin.math.PI
import kotlin.math.sin


fun List<Int>.toByteArray(): ByteArray{
    val bytes = ByteArray(size)
    for(i in 0 until size){
        bytes[i] = this[i].toByte()
    }
    return bytes
}

fun List<Signal>.sum() = when(size){
        0 -> NullSignal(MainActivity.SAMPLES_PER_BUFFER)
        1 -> this[0]
        2 -> SumSignal(this[0], this[1])
        else -> this.fold(SinSignal(1)) { result: Signal, next: Signal -> result + next}
}

interface SignalProperties{
    val data: ByteArray
}

abstract class Signal(): SignalProperties{
    val sampleRate = 44100
//    private val seconds = 3
//    val numSamples  = seconds * sampleRate

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
        val start = System.currentTimeMillis()

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

            audio.write(data, 0, data.size)
            audio.play()




        val time = System.currentTimeMillis() - start
        Log.d("latencyTest","That took $time milliseconds")

        return audio
    }
}

/**
 * Silent signal. Useful when adding multiple signals together
 * @param length number of samples in ByteArray of data
 */
class NullSignal(length: Int): Signal() {
    override val data = ByteArray(length)
}

class SumSignal(s1: Signal, s2: Signal): Signal(){
    override val data = s1.data.zip(s2.data).map { it.first + it.second }.toByteArray()

    operator fun plusAssign(that: Signal){ SumSignal(this, that) }
}




class SinSignal(private val freq: Int) : Signal() {
    override val data = generatePeriod(100)

    private fun generatePeriod(numPeriods: Int, start: Int = 0): ByteArray{
        val period = sampleRate / freq
        val size = numPeriods*period
        val bytes = ByteArray(size)
        for (i in 0 until size){
            bytes[i] = (sin(2.0 * PI * i / period) * 127).toInt().toByte()
        }
        return bytes
    }

}