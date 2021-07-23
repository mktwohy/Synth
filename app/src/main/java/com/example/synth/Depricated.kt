package com.example.synth

/** A recycling bin for this project. This code should not be used. */

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






