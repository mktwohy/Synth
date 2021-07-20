package com.example.synth

import java.lang.StringBuilder
import kotlin.random.Random
import kotlin.system.measureTimeMillis

object LatencyUtil {
    fun makeHashTagBar(yValue: Int): String{
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