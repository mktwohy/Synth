package com.example.synth

import java.lang.IndexOutOfBoundsException

class CircularShortArray(size: Int) {
    val data = ShortArray(size)
    var size = 0
    private var currentIndex = 0

    fun getNextChunk(chunkSize: Int): ShortArray {
        val chunk = ShortArray(chunkSize)

        for (i in 0 until chunkSize) {
            chunk[i] = data[currentIndex]
            currentIndex = (currentIndex + 1) % data.size
        }

        return chunk
    }

    operator fun get(i: Int): Short{
        if (i !in data.indices) throw IndexOutOfBoundsException()
        return data[i]
    }

    operator fun set(index: Int, value: Short){
        data[index] = value
        size++
    }

}