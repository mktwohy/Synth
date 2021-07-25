package com.example.synth

import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

interface SignalProperties{
    /** A List<FLoat>*/
    val audio: CircularIntArray
    val frequencies: MutableSet<Int>
}


/** Represents a sound, which can be converted to PCM data to be played by an AudioTrack */
abstract class Signal: SignalProperties{
    companion object{
        const val SAMPLE_RATE       = AudioEngine.SAMPLE_RATE
        const val BUFFER_SIZE       = AudioEngine.BUFFER_SIZE
        const val TWO_PI              = 2.0 * PI
        const val MIN_16BIT_VALUE     = -32_768
        const val MAX_16BIT_VALUE     = 32_767
        val NullSignal = NullSignal(BUFFER_SIZE)
    }

    abstract fun transpose(step: Int): Signal

    override fun toString(): String{
        val s = StringBuilder()
        for(value in audio){
            s.append(value)
            s.append(" ")
        }
        return s.toString()
    }
}


/**
 * Represents a silent signal.
 * @param size number of samples in ByteArray of data
 */
class NullSignal(size: Int = BUFFER_SIZE): Signal() {
    override val frequencies = mutableSetOf(0)
    override val audio = CircularIntArray(size)
    override fun transpose(step: Int) = NullSignal
}


/**
 * Represents a pure sine wave
 * @param freq frequency of wave
 */
class SinSignal(private val freq: Int) : Signal() {
    override val frequencies = mutableSetOf(freq)
    override val audio = run{
        val period = SAMPLE_RATE / freq
        CircularIntArray(period) { i -> (sin(TWO_PI * i / period) * MAX_16BIT_VALUE).toInt() }
    }

    override fun transpose(step: Int): Signal {
        val fundFreq = frequencies.minByOrNull { it }
            ?: return NullSignal
        return SinSignal( (fundFreq * Interval.stepToRatio(step)) .toInt() )
    }
}


/** Combines two or more Signals into one Signal. */
class SumSignal(signals: Set<Signal>): Signal() {
    override val frequencies = mutableSetOf<Int>().apply {
        for(s in signals){
            addAll(s.frequencies)
        }
    }
    override val audio = run{
        val amps = signals.map { it.audio }
        CircularIntArray(amps.map{ it.size }.lcm()){
            amps.fold(0){ sumAtIndex, circIntArr -> sumAtIndex + circIntArr.nextElement() }
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

fun main() {
    val sigs = setOf(SinSignal(440), SinSignal(880))
    val sum = SumSignal(sigs)
    sigs.forEach{ println("size: ${it.audio.size} sig: $it") }
    println("size: ${sum.audio.size} sum: $sum")
    println("size: ${sum.audio.size} norm: ${sum.audio.normalize()}")

    val arr = IntArray(10){ Random.nextInt(-50, 50) }
    println("arr: ${arr.joinToString { "$it"  }}")
//    println("arr: ${arr.normalize(-10,10).joinToString { "$it" }}")


}