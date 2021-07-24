package com.example.synth

import android.util.Log
import java.lang.StringBuilder
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.system.measureTimeMillis

/** Used for latency testing. */

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

fun main(){
    val times = mutableListOf<Long>()
    repeat(1000){
        val listSize = Random.nextInt(1, 10)
        val list = (0..listSize).map{ Random.nextInt(1, 500) }

        val lcm: Int
        val time = measureTimeMillis { lcm = list.lcm() }
            .also{ times.add(it) }
        println(" $time ms to calculate lcm = $lcm for $list")
    }
    println("Average lcm time: ${times.average()}")
}