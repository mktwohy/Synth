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


*/



