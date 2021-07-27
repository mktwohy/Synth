package com.example.synth

import com.example.synth.LatencyUtil.makeRandomSignals
import java.lang.StringBuilder
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
        val numNotes = Random.nextInt(4,5)
        val notes = mutableSetOf<Signal>().apply {
            repeat(numNotes){
                add(SinSignal(Note.toList(5).random().freq))
            }
        }
        return SumSignal(notes)
    }

    fun makeRandomSignals(
        n: Int = Random.nextInt(10)
    ) = Array(n) { makeRandomSumSignal() }

}

fun <T, R>measureAvgRunTime( cases: Iterable<T>, funcName: String, func: (T) -> R) {
    val times = mutableListOf<Long>()
    for (c in cases) {
        measureTimeMillis {
            func(c)
        }.run { times.add(this) }
    }
    println("$funcName \n\tavg time: ${times.average()} times: $times")
}

fun <T, R>measureAvgRunTime( cases: Array<T>, funcName: String, func: (T) -> R) {
    measureAvgRunTime(cases.toList(), funcName, func)
}

val convertIntArrayToShortArray = { a: IntArray -> a.toShortArray() }
val calculateLcm = { l: List<Int> -> l.lcm() }
//val createSumSignal = { s: Set<Signal> -> SumSignal(s) }
val createSumSignal = { s: Set<Int> -> SumSignal(s.map{ SinSignal(it) }.toSet()) }

val threadSleep = { d: Long -> run{Thread.sleep(d)} }

fun main(){

    val numCases = 50

    measureAvgRunTime( makeRandomSignals(numCases).map{it.amplitudes},
        "convertIntArrayToShortArray")
    { convertIntArrayToShortArray }

    measureAvgRunTime( makeRandomSignals(numCases).map{ it.amplitudes.toList() },
    "calculateLcm")
    { calculateLcm }

//    measureAvgRunTime( Array(numCases) { Random.nextInt(1,1000) },
//    "createSumSignal")
//    { createSumSignal }

    measureAvgRunTime( Array(numCases) { Random.nextInt(1,1000) },
        "createSumSignal")
    { createSumSignal }

    measureAvgRunTime( Array(numCases) { i -> i*1 },
        "sleep")
    { threadSleep }


    val cases = listOf(
        listOf(123, 1200, 40301, 123, 12445,23,88,12355,303,1003),
        listOf(25, 457, 40301, 23),
        listOf(123, 1200, 235, 123),
        listOf(123, 1200, 234, 123),
        listOf(123, 1200, 4031401, 123),
        listOf(123, 1200, 40301, 123)
    )
    measureAvgRunTime(cases, "test"){ l: List<Int> -> }

}

