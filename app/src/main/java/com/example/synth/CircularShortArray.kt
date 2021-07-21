package com.example.synth

/**
 * Circular Array of Shorts. This is ideal for Signals, as it means they can store just one period
 * of their wave and loop over it.
 */
class CircularShortArray(override var size: Int = 0): Collection<Short> {
    private val data = ShortArray(size)
    var circularIndex = 0

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
                chunk[i] = data[circularIndex]
                circularIndex = (circularIndex + 1) % data.size
            }
        }
    }

    /** Ensures that the chunk returned from getNextChunk() starts from the beginning of data */
    fun reset(){ circularIndex = 0 }

    fun toList() = data.toList()
    operator fun get(index: Int): Short{ return data[index] }
    operator fun set(index: Int, value: Short){ data[index] = value }

    override fun isEmpty()                  = size == 0
    override fun toString()                 = data.contentToString()
    override fun iterator()                 = data.iterator()
    override fun contains(element: Short)   = data.contains(element)
    override fun containsAll(elements: Collection<Short>): Boolean{
        for(e in elements){
            if (e !in data) return false
        }
        return true
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CircularShortArray

        if (!data.contentEquals(other.data)) return false

        return true
    }



}