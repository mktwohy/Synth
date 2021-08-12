package com.example.synth

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

val calculateLcm = { l: List<Int> -> l.lcm() }
//val createSumSignal = { s: Set<Signal> -> SumSignal(s) }

val threadSleep = { d: Long -> run{Thread.sleep(d)} }



