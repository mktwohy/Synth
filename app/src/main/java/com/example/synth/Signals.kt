package com.example.synth

import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

interface SignalProperties{
    /** A List<FLoat>*/
    val data: IntArray
    val frequencies: MutableSet<Int>
}


/** Represents a sound, which can be converted to PCM data to be played by an AudioTrack */
abstract class Signal: SignalProperties{
    open fun dataToPcm(): CircularShortArray = data.normalize().toCircularShortArray()

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
}


/**
 * Represents a silent signal.
 * @param size number of samples in ByteArray of data
 */
class NullSignal(size: Int = BUFFER_SIZE): Signal() {
    override val frequencies = mutableSetOf(0)
    override val data = IntArray(size)
    override fun dataToPcm() = CircularShortArray(BUFFER_SIZE)
    override fun transpose(step: Int) = NullSignal
}


/**
 * Represents a pure sine wave
 * @param freq frequency of wave
 */
class SinSignal(private val freq: Int) : Signal() {
    override val frequencies = mutableSetOf(freq)
    override val data = run{
        val period = SAMPLE_RATE / freq
        IntArray(period) { i -> (sin(TWO_PI * i / period) * MAX_16BIT_VALUE).toInt() }
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
    override val data = with(signals.map { it.data }){
        val cIterators = this.map { CircularIterator(it) }
        IntArray(this.map{ it.size }.lcm()){
            cIterators.fold(0){ sumAtIndex, iterator -> sumAtIndex + iterator.nextElement() }
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

//fun main() {
//    val sigs = setOf(SinSignal(440), SinSignal(880))
//    val sum = SumSignal(sigs)
//    sigs.forEach{ println("size: ${it.data.size} sig: $it") }
//    println("size: ${sum.data.size} sum: $sum")
//    println("size: ${sum.data.size} norm: ${sum.data.normalize().toList()}")
//
//    val arr = IntArray(10){ Random.nextInt(-50, 50) }
//    println("arr: ${arr.joinToString { "$it"  }}")
//    println("arr: ${arr.normalize(-10,10).joinToString { "$it" }}")
//
//}