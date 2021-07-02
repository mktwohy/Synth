package com.example.synth

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import java.lang.UnsupportedOperationException
import kotlin.math.PI
import kotlin.math.sin


fun List<Int>.toByteArray(): ByteArray{
    val bytes = ByteArray(size)
    for(i in 0 until size){
        bytes[i] = this[i].toByte()
    }
    return bytes
}

interface Signal{
    val data: ByteArray
}

abstract class SignalOperations(): Signal{
    val sampleRate = 44100
//    private val seconds = 3
//    val numSamples  = seconds * sampleRate

    operator fun plus(that: Signal) = SumSignal(this,that)



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
        Log.d("Mike","That took $time milliseconds")

        return audio
    }
}

class SumSignal(s1: Signal, s2: Signal): SignalOperations(){
    override val data =
        s1.data.zip(s2.data).map { it.first + it.second }.toByteArray()

    operator fun plusAssign(that: Signal){ SumSignal(this,that) }
}

class SinSignal(private val freq: Int) : SignalOperations() {
    private val interval = sampleRate / freq
    override val data   = generateInterval(100)



    private fun generateInterval(numIntervals: Int, start: Int = 0): ByteArray{
        val size = numIntervals*interval
        val bytes = ByteArray(size)
        for (i in 0 until size){
            bytes[i] = (sin(2.0 * PI * i / interval) * 127).toInt().toByte()
        }
        return bytes
    }

}