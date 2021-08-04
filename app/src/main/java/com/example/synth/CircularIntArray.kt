package com.example.synth

import android.util.Log
import com.example.synth.CircularIntArray.Companion.sine
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos
import kotlin.random.Random

/** Index for circularly navigating an array */
class CircularIndex{
    private var leftBound: Int
    private var rightBound: Int

    constructor(loopSize: Int){
        leftBound = 0
        rightBound = loopSize -1
    }

    constructor(leftBound: Int, rightBound: Int){
        this.leftBound  = leftBound
        this.rightBound = rightBound
    }

    /** Current index value */
    var i: Int = 0

    /** Circularly moves the underlying index inside the bounds of loopSize. */
    fun iterate(step: Int = 1){
        i = ( (i + step) % (rightBound + 1) ) + leftBound
    }

    fun getIndexAndIterate(step: Int = 1, noise: Boolean = false) =
        i.also {
            if (noise) iterate(Random.nextInt(1, step+1))
            else iterate(step)
        }

    /** Moves underlying index to 0 */
    fun reset(){ i = 0 }
}

/**
 * - Wrapper class for IntArray that keeps track of a CircularIndex to navigate the
 * underlying data structure.
 * - This class is used in Synth to represent [Signal.amplitudes]
 * - Note that nextElement() and nextChunk() use the same Circular Index. This will be changed if
 * it causes issues
 * */
class CircularIntArray: Collection<Int>{
    companion object{
        const val TWO_PI              = 2.0 * PI
        const val MIN_16BIT_VALUE     = -32_768
        const val MAX_16BIT_VALUE     = 32_767

        val sine = { i: Int, period: Int ->
            (sin(TWO_PI * i / period) * MAX_16BIT_VALUE).toInt()
        }
        val cosine = { i: Int, period: Int ->
            (cos(TWO_PI * i / period) * MAX_16BIT_VALUE).toInt()
        }

        fun signal(size: Int, func: (Int, Int) -> Int) =
             CircularIntArray(size) { i -> func(i, size) }


        fun sinSignal(freq: Int, func: (Int, Int) -> Int) =
            signal(calculatePeriod(freq), sine)


        fun harmonicSignal(
            fundamental: Note,
            harmonicSeries: Map<Int, Int>,
            func: (Int, Int) -> Int
        ): CircularIntArray {
            val ret = sinSignal(fundamental.freq, func)
            harmonicSeries
                .filter { it.key != 1 }
                .forEach {
                    ret += sinSignal(fundamental.freq * it.key, func)
                        .apply{ volume = it.value }
                }
            return ret
        }

        fun calculateCommonInterval(freqs: Set<Int>) =
            freqs.map { calculatePeriod(it) }.lcm()

        fun calculatePeriod(freq: Int) = AudioEngine.SAMPLE_RATE / freq
    }

    var volume: Int = 100
        set(value) {
            if (value in 0..100){
                field = value
                normalize()
            }
        }
    override val size: Int
        get() = data.size
    var noiseAmount: Int = 0
    var data: IntArray
    private var index: CircularIndex

    constructor(size: Int, init: (Int) -> Int = {0} ){
        this.data = IntArray(size, init).also{ it.normalize() }
        this.index = CircularIndex(size)
    }

    constructor(data: IntArray){
        this.data = data.also{ it.normalize() }
        this.index = CircularIndex(size)
    }

    /** Ensures that the chunk returned from getNextChunk() starts from the beginning of data */
    fun reset(){ index.reset() }

    fun normalize(){
        data.normalize(
            (MIN_16BIT_VALUE * volume/100f).toInt(),
            (MAX_16BIT_VALUE * volume/100f).toInt()
        )
    }

    fun nextElement() = data[index.getIndexAndIterate()]

    /**
     * Builds and returns a chunk of data by circularly iterating over and appending
     * CircularIntArray's data to an IntArray of size [chunkSize].
     *
     *  The next time this method is called, its starting point will be where
     * the previous chunk ended.
     * @param chunkSize size of the returned IntArray
     * @return a looped chunk of values from data
     */
    fun nextChunk(chunkSize: Int): IntArray {
        val step = if (noiseAmount == 0) 1 else noiseAmount
        return IntArray(chunkSize){ data[index.getIndexAndIterate(step, (noiseAmount > 0))] }
    }

    /** Does the same as [nextChunk], but writes to an existing array
     * @param destination array that chunk is written to */
    fun nextChunkTo(destination: IntArray) {
        val step = if (noiseAmount == 0) 1 else noiseAmount
        for (i in destination.indices){
            destination[i] = data[index.getIndexAndIterate(step, (noiseAmount > 0))]
        }
    }

    fun addValuesOfNextChunkTo(destination: IntArray){
        val step = if (noiseAmount == 0) 1 else noiseAmount
        for (i in destination.indices){
            destination[i] += data[index.getIndexAndIterate(step, (noiseAmount > 0))]
        }
    }

    operator fun plusAssign(that: CircularIntArray){
        for (i in this.indices) {
            this[i] += that.data[that.index.getIndexAndIterate(1, (noiseAmount > 0))]
        }
        this.normalize()
    }

    operator fun get(index: Int): Int{ return data[index] }
    operator fun set(index: Int, value: Int){ data[index] = value }

    override fun toString() = data.contentToString()
    override fun isEmpty() = size != 0
    override fun iterator(): Iterator<Int> = data.iterator()
    override fun contains(element: Int) = data.contains(element)
    override fun containsAll(elements: Collection<Int>): Boolean {
        for(e in elements){
            if (!data.contains(e)) return false
        }
        return true
    }
}