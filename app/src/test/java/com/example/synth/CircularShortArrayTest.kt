package com.example.synth

import org.junit.After
import org.junit.Test
import org.junit.Assert.*
import java.lang.Exception

class CircularShortArrayTest {
    val bufferSize = MainActivity.BUFFER_SIZE
    var circularShortArray = CircularShortArray(0)

    @After
    fun tearDown(){
        var circularShortArray = CircularShortArray(0)
    }

    @Test
    fun `circularShortArrays of sizes greater than 1 have correct chunk sizes`(){
        (1..bufferSize*2).toList().forEach {
            circularShortArray = CircularShortArray(it)
            assertEquals(bufferSize, circularShortArray.nextChunk(bufferSize).size)
        }
    }

    @Test
    fun `getNextChunk when size == 0 throws exception`(){
        circularShortArray = CircularShortArray(0)
        var throwsInitException = false
        try{
            circularShortArray.nextChunk(bufferSize).size
        }catch (e: Exception){
            throwsInitException = true
        }
        assert(throwsInitException)
    }

    @Test
    fun `pcmData of each note has correct size`(){
        for (n in Note.values()){
            circularShortArray = SinSignal(n.freq).pcmData
            assertEquals(bufferSize, circularShortArray.nextChunk(bufferSize).size)
        }
    }

    @Test
    fun `chunk from each note matches its data`(){
        for (n in Note.values()){
            circularShortArray = SinSignal(n.freq).pcmData
            var data = circularShortArray.toList()
            var chunk = circularShortArray.run { reset() ; nextChunk(bufferSize).toList() }
            if (data.size > chunk.size) data = data.subList(0, chunk.size)
            if (data.size < chunk.size) chunk = chunk.subList(0, data.size)

            assertEquals(data, chunk)
        }
    }

    @Test
    fun `pcm data of each note starts with 0`(){
        for (n in Note.values()){
            assertEquals(0.toShort(), SinSignal(n.freq).pcmData[0])
        }
    }

    @Test
    fun `pcm data of each note ends with 0`(){
        for (n in Note.values()){
            with(SinSignal(n.freq).pcmData){
                assertEquals("note: $n \n data: $this \n",
                    0.toShort(), this[this.size-1]
                )
            }

        }
    }

    @Test
    fun `circular index moves to end of previous chunk`(){
        for (n in Note.values()){
            with (SinSignal(n.freq).pcmData){
                repeat(40){
                    assertNotEquals(
                        circularIndex.also { nextChunk(bufferSize) } + bufferSize - 1,
                        circularIndex
                        )
                }
                println()
            }

        }
    }
}

