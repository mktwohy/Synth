package com.example.synth

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

fun main() {
    val c = CircularIndex(10)
    repeat(30){ print("${c.getIndexAndIterate()} ") }
}

/** Wrapper class for IntArray that keeps track of a CircularIndex to navigate the
 * underlying data structure.
 *
 * Note that nextElement() and nextChunk() use the same Circular Index
 * */
class CircularIntArray: Collection<Int>{
    override val size: Int
    private val data: IntArray
    private var index: CircularIndex

    constructor(size: Int, init: (Int) -> Int = {0} ){
        this.size = size
        this.data = IntArray(size, init)
        this.index = CircularIndex(size)
    }

    constructor(data: IntArray){
        this.size = data.size
        this.data = data
        this.index = CircularIndex(size)
    }


    /** Ensures that the chunk returned from getNextChunk() starts from the beginning of data */
    fun reset(){ index.reset() }

    /**
     * Builds and returns a chunk of data by circularly iterating over and appending
     * CircularIntArray's data. The next time this method is called, its starting point
     * will be where the previous chunk ended.
     * @param chunkSize size of the returned ShortArray
     * @return a looped chunk of values from data
     */
    fun nextChunk(chunkSize: Int, noiseAmount: Int = 0): IntArray {
        if (size == 0) throw Exception("cannot get chunk of array size 0")

        val step = if (noiseAmount == 0) 1 else noiseAmount
        return IntArray(chunkSize){ data[index.getIndexAndIterate(step, (noiseAmount > 0))] }
    }

    fun nextElement(): Int {
        if (data.isEmpty()) throw Exception("cannot get chunk of array size 0")

        return data[index.getIndexAndIterate()]
    }

    fun normalize(){
        data.normalize()
    }

    operator fun get(index: Int): Int{ return data[index] }
    operator fun set(index: Int, value: Int){ data[index] = value }

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

