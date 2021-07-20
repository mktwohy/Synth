package com.example.synth

import android.util.Log
import kotlin.random.Random
import kotlin.system.measureTimeMillis

val signals = listOf(
    SinSignal(Note.C_4.freq),
    SinSignal(Note.D_4.freq),
    SinSignal(Note.E_4.freq),
    SinSignal(Note.G_4.freq)
)

fun makeRandomSumSignal(newSum: Boolean = true): Signal{
    val numNotes = Random.nextInt(5)
    val notes = mutableListOf<Signal>().apply {
        repeat(numNotes){
            add(SinSignal(Note.toList(5).random().freq))
        }
    }
    return if (newSum)
        notes.newSum()
    else notes.oldSum()
}

var oldTimes = mutableListOf<Long>()
var newTimes = mutableListOf<Long>()

fun List<Signal>.oldSum(): Signal{
    var ret: Signal
    val t = measureTimeMillis {
        ret = when(size){
            0 -> NullSignal()
            1 -> this[0]
            2 -> this[0] + this[1]
            else -> this.reduce { acc: Signal, sig: Signal -> acc + sig }
        }
    }
    println("OldSum(): size: $size  \n time: $t")
    oldTimes.add(t)
    return ret
}

fun List<Signal>.newSum(): Signal{
    var ret: Signal
    val t = measureTimeMillis {
        ret = when(size){
            0 -> NullSignal()
            1 -> this[0]
            2 -> this[0] + this[1]
            else -> run{
                var sum = this[0]
                for (i in 1..this.indices.last){
                    sum += this[i]
                }
                sum
            }
        }
    }
    println("NewSum(): size: $size  \n time: $t")
    newTimes.add(t)
    return ret
}

fun main() {
    repeat(200){
        makeRandomSumSignal(false)
    }
    repeat(200){
        makeRandomSumSignal()
    }

    println("Old Avg Time: ${oldTimes.average()}")
    println("New Avg Time: ${newTimes.average()}")
}