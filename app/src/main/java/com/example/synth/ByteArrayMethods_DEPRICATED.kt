package com.example.synth

    val IntToByteArrayLookupTable = run{
        val table = mutableMapOf<Int, ByteArray>()
        for (i in Signal.MIN_16BIT_VALUE..Signal.MAX_16BIT_VALUE){
            table[i] = i.toBigInteger().toByteArray()
        }
        table
    }

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

    fun ByteArray.toList(bit: Int = 16): List<Int> =
        if(bit == 8) this.toList()
        else this
            .toList()
            .chunked(2)
            .map{ it[0] + it[1] }
            .toList()








