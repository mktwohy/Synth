package com.example.synth

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlin.math.PI
import kotlin.math.sin



interface Signal{
    val data: ByteArray
}

abstract class SignalOperations(): Signal{
    val sampleRate = 44100
    val seconds = 3
    val numSamples  = seconds * sampleRate


    operator fun plus(that: Signal) = SumSignal(this,that)

    fun play(){
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
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audio.write(data, 0, data.size)
        audio.play()

    }
}

class SumSignal(s1: Signal, s2: Signal): SignalOperations(){
    override val data =
        s1.data.zip(s2.data).map { it.first + it.second } as ByteArray
}

/**
 * @property freq frequency in hz
 * @property data amplitude values that represent the signal
 */
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