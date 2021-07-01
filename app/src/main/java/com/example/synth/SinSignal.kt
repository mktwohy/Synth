package com.example.synth

import java.io.File
import kotlin.math.PI
import kotlin.math.sin


class SinSignal(
    private val freq: Int,
    private val seconds: Int,
    private val sampleRate: Int= 44100
) {
    var data: ByteArray

    init {
        //From Source 1)
        val numSamples  = seconds * sampleRate
        val sinArray    = ByteArray(numSamples)
        val interval    = sampleRate.toDouble() / freq

        for (i in 0 until numSamples){
            val angle = 2.0*PI * i / interval
            sinArray[i] = (sin(angle) * 127).toInt().toByte()
        }
        data = sinArray
    }



    //---OPERATOR FUNCTIONS FOR OTHER SIN SIGNALS---
    //ToDo: operator functions currently modify themselves. Need to implement a copy method.
    operator fun plus(that: SinSignal): SinSignal{
        for (i in data.indices){
            data[i] = ((this.data[i].toInt() + that.data[i].toInt()) % 127).toByte()
        }
        /* Functional Approach (doesn't seem to work)
        data.mapIndexed{ index, it ->
            (it + that.data[index] % 127)
        }
         */
        return this
    }

    operator fun minus(that: SinSignal): SinSignal{
        for (i in data.indices){
            data[i] = (this.data[i].toInt() - that.data[i].toInt()).toByte()
        }
        return this
    }

    operator fun times(that: Float) = this.apply{ data.map{ it * that } }

    //---OPERATOR FUNCTIONS FOR INTERVALS---
    operator fun plus(that: Interval): SinSignal{
        return this + SinSignal(freq*that.ratio.toInt(), seconds, sampleRate)
    }

    operator fun minus(that: Interval): SinSignal{
        return this - SinSignal(freq*that.ratio.toInt(), seconds, sampleRate)
    }
}