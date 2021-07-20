package com.example.synth

import java.lang.IndexOutOfBoundsException

class CircularShortArray(size: Int) {
    var size = 0
    private val data = ShortArray(size)
    private var currentIndex = 0

    fun getNextChunk(chunkSize: Int): ShortArray {
        val chunk = ShortArray(chunkSize)

        for (i in 0 until chunkSize) {
            chunk[i] = data[currentIndex]
            currentIndex = (currentIndex + 1) % data.size
        }

        return chunk
    }

    operator fun get(index: Int): Short{
        if (index !in data.indices) throw IndexOutOfBoundsException()
        return data[index]
    }

    operator fun set(index: Int, value: Short){
        if (index !in data.indices) throw IndexOutOfBoundsException()
        data[index] = value
        size++
    }

}