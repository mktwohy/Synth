package com.example.synth

/** A recycling bin for this project. This code should not be used. */

/*
@Deprecated("ByteArrays are slow and overly-complicated. Use CircularShortArray instead")
val IntToByteArrayLookupTable = run{
    val table = mutableMapOf<Int, ByteArray>()
    for (i in Signal.MIN_16BIT_VALUE..Signal.MAX_16BIT_VALUE){
        table[i] = i.toBigInteger().toByteArray()
    }
    table
}

@Deprecated("ByteArrays are slow and overly-complicated. Use CircularShortArray instead")
fun List<Int>.toByteArray(): ByteArray {
    val byteArray = ByteArray(size * 2)
    var bIndex = 0
    for (i in indices) {
        val bytes = IntToByteArrayLookupTable[this[i]]
        if (bytes != null){
            if (bytes.size > 1)
                byteArray[bIndex] = bytes[1]
            byteArray[bIndex+1] = bytes[0]
        }
        bIndex += 2
    }
    return byteArray
}

@Deprecated("ByteArrays are slow and overly-complicated. Use CircularShortArray instead")
fun ByteArray.toList(bit: Int = 16): List<Int> =
    if(bit == 8) this.toList()
    else this
        .toList()
        .chunked(2)
        .map{ it[0] + it[1] }
        .toList()

@Deprecated("Use CircularShortArray instead")
private fun List<Int>.toShortArray() =
    this.map { it.toShort() }.toShortArray()

//fun List<Signal>.sum(): Signal{
//    if (size == 0) return Signal.NullSignal
//    val ret: Signal
//    with (measureTimeMillis {
//         ret = toSet().run{
//            if (this !in signalsToSumSignal)
//                signalsToSumSignal[this] = this.reduce { acc: Signal, sig: Signal -> acc + sig }
//
//            signalsToSumSignal[this]!!
//            }
//        }
//    ){ Log.d("m_time","$this milliseconds to sum $size notes") }
//
//    Log.d("m_mapSize", "signalsToSumSignal size: ${signalsToSumSignal.size}")
//    return ret
//}


class CircularIterator(val data: IntArray){
    private val index = CircularIndex(data.size)

    /** Ensures that the chunk returned from getNextChunk() starts from the beginning of data */
    fun reset(){ index.reset() }

    /**
     * Builds and returns a chunk of data by circularly iterating over and appending
     * CircularShortArray's data. The next time this method is called, its starting point
     * will be where the previous chunk ended.
     * @param chunkSize size of the returned ShortArray
     * @return a looped chunk of values from data
     */
    fun nextChunk(chunkSize: Int): IntArray {
        if (data.isEmpty()) throw Exception("cannot get chunk of array size 0")

        return IntArray(chunkSize){ data[index.getIndexAndIterate()] }
    }

    fun nextElement(): Int {
        if (data.isEmpty()) throw Exception("cannot get chunk of array size 0")

        return data[index.getIndexAndIterate()]
    }
}


/**
 * Circular Array of Shorts. This is ideal for Signals, as it means they can store just one period
 * of their wave and the AudioEngine can loop over it.
 */
class CircularShortArray {
    val size: Int
    private val data: ShortArray
    var index: CircularIndex

    constructor(size: Int){
        this.size = size
        this.data = ShortArray(size)
        this.index = CircularIndex(size)
    }

    constructor(data: ShortArray){
        this.size = data.size
        this.data = data
        this.index = CircularIndex(size)
    }


    /** Ensures that the chunk returned from getNextChunk() starts from the beginning of data */
    fun reset(){ index.reset() }

    /**
     * Builds and returns a chunk of data by circularly iterating over and appending
     * CircularShortArray's data. The next time this method is called, its starting point
     * will be where the previous chunk ended.
     * @param chunkSize size of the returned ShortArray
     * @return a looped chunk of values from data
     */
    fun nextChunk(chunkSize: Int): ShortArray {
        if (size == 0) throw Exception("cannot get chunk of array size 0")

        return ShortArray(chunkSize).also{ chunk ->
            for (i in chunk.indices) {
                chunk[i] = data[index.i]
                index.iterate()
            }
        }
    }
}

fun IntArray.toCircularShortArray() = CircularShortArray(this.toShortArray())



    /** Does the same as [nextChunk], but writes to an already existing array
     * @param array array that chunk is written to */
    fun writeNextChunkTo(array: IntArray, noiseAmount: Int = 0) {
        val step = if (noiseAmount == 0) 1 else noiseAmount
        for (i in array.indices){
            array[i] = data[index.getIndexAndIterate(step, (noiseAmount > 0))]
        }
    }

    /** Does the same as [writeNextChunkTo], but converts each value to a Short first
     * @param array array that chunk is written to */
    fun writeNextChunkAsShortArrayTo(array: ShortArray, noiseAmount: Int = 0) {
        data.normalize()
        val step = if (noiseAmount == 0) 1 else noiseAmount
        for (i in array.indices){
            array[i] = data[index.getIndexAndIterate(step, (noiseAmount > 0))].toShort()
        }
    }

    /** Does the same as [writeNextChunkTo], but converts each value to a Short first
     * @param destination array that chunk is written to */
    fun nextChunkAsShortArray(destination: ShortArray, noiseAmount: Int = 0) {
        data.normalize()
        val step = if (noiseAmount == 0) 1 else noiseAmount
        for (i in destination.indices){
            destination[i] = data[index.getIndexAndIterate(step, (noiseAmount > 0))].toShort()
        }
    }
    fun IntArray.toShortArray(destination: ShortArray){
    if (destination.size == this.size){
        for(i in indices) {
            destination[i] = this[i].toShort()
        }
    }
}

*/

/*

import kotlin.math.PI
import kotlin.math.sin

/**
 * @property amplitudes represents the audio data.
 * @property frequencies the known frequencies in the Signal
 */
interface SignalProperties{
    val amplitudes: CircularIntArray
    val frequencies: MutableSet<Int>

}


/** Represents a sound, which can played by:
 * - to play once: use [android.media.AudioTrack]
 * - to play on a loop:
 * */
abstract class Signal: SignalProperties{
    companion object{
        const val SAMPLE_RATE       = AudioEngine.SAMPLE_RATE
        const val BUFFER_SIZE       = AudioEngine.BUFFER_SIZE
        const val TWO_PI              = 2.0 * PI
        const val MIN_16BIT_VALUE     = -32_768
        const val MAX_16BIT_VALUE     = 32_767
    }

    abstract fun transpose(step: Int): Signal

    override fun toString(): String{
        val s = StringBuilder()
        for(value in amplitudes){
            s.append(value)
            s.append(" ")
        }
        return s.toString()
    }
}


/** Represents a silent signal of size [AudioEngine.BUFFER_SIZE] */
object NullSignal: Signal() {
    override val frequencies = mutableSetOf<Int>()
    override val amplitudes = CircularIntArray(BUFFER_SIZE)
    override fun transpose(step: Int) = NullSignal
}


/** Represents a pure sine wave */
class SinSignal(private val freq: Int) : Signal() {
    override val frequencies = mutableSetOf(freq)
    override val amplitudes = run{
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
    override val amplitudes = run{
        val amps = signals.map { it.amplitudes }
        val intervalSize = amps.map{ it.size }.lcm()
        CircularIntArray(intervalSize){
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
 */

/*
//----- List<Signal> ----- //

//fun List<Signal>.sum(): Signal{
//    val signalSet = this.toSet()
//    if(signalSet in signalsToSumSignal) return signalsToSumSignal[signalSet]!!
//    return when (size){
//        0 -> NullSignal
//        1 -> this[0]
//        else -> SumSignal(this.toSet())
//    }.also { signalsToSumSignal[signalSet] = it }
//}

fun List<Signal>.sum(): Signal{
    return when (size){
        0 -> NullSignal
        1 -> this[0]
        else -> SumSignal(this.toSet())
    }
}

    fun makeRandomSumSignal(): Signal{
        val numNotes = Random.nextInt(4,5)
        val notes = mutableSetOf<Signal>().apply {
            repeat(numNotes){
                add(SinSignal(Note.toList(5).random().freq))
            }
        }
        return SumSignal(notes)
    }

    fun makeRandomSignals(
        n: Int = Random.nextInt(10)
    ) = Array(n) { makeRandomSumSignal() }

 */



