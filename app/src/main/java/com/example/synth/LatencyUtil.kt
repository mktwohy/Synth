package com.example.synth

import android.util.Log
import java.lang.StringBuilder
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.system.measureTimeMillis

/**
 * Used for latency testing. It's currently incomplete, ignore it for now
 */

object LatencyUtil {
    /** Generates a horizontal bar of #'s */
    fun histogramBar(yValue: Int): String{
        val s = StringBuilder()
        repeat(yValue){ s.append("#") }
        return s.toString()
    }

    fun makeRandomSumSignal(): Signal{
        val numNotes = Random.nextInt(5)
        val notes = mutableListOf<Signal>().apply {
            repeat(numNotes){
                add(SinSignal(Note.toList(5).random().freq))
            }
        }
        return notes.sum()
    }
}




fun main() {
    val newSize = 10
    val list = listOf(1,2,3,4,5)
    val sequence = list.asSequence().iterator()

    val newList = IntArray(newSize)
    for(i in newList.indices){
        newList[i] = sequence.next()
    }

    println(newList)

}
