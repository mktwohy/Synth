package com.example.synth

import shark.PrimitiveType

/** Index for circularly navigating an array */
class CircularIndex(private val loopSize: Int){
    /** Current index value */
    var i: Int = 0

    fun iterate(step: Int = 1){
        i = (i + step) % loopSize
    }

    fun getIndexAndIterate() = i.also { iterate() }

    fun reset(){ i = 0 }
}

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

/** Circular Array of Ints*/
class CircularIntArray {
    val size: Int
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
     * CircularShortArray's data. The next time this method is called, its starting point
     * will be where the previous chunk ended.
     * @param chunkSize size of the returned ShortArray
     * @return a looped chunk of values from data
     */
    fun nextChunk(chunkSize: Int): IntArray {
        if (size == 0) throw Exception("cannot get chunk of array size 0")

        return IntArray(chunkSize){ data[index.getIndexAndIterate()] }
    }

    fun nextPcmChunk(chunkSize: Int): ShortArray{
        if (size == 0) throw Exception("cannot get chunk of array size 0")

        return ShortArray(chunkSize){ data[index.getIndexAndIterate()].toShort() }
    }


    operator fun get(index: Int): Int{ return data[index] }
    operator fun set(index: Int, value: Int){ data[index] = value }

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
