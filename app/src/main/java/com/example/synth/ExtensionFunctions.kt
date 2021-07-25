package com.example.synth

import android.util.Log
import java.lang.StringBuilder
import kotlin.system.measureTimeMillis


//----- List<Signal> ----- //

fun List<Signal>.sum(): Signal{
    val signalSet = this.toSet()
    if(signalSet in signalsToSumSignal) return signalsToSumSignal[signalSet]!!
    val ret = when (size){
        0 -> Signal.NullSignal
        1 -> this[0]
        2 -> SumSignal(this[0], this[1])
        else -> SumSignal(this.toSet())
    }
    return ret
        .also { signalsToSumSignal[signalSet] = it }

}

//----- IntArray ----- //
//https://stats.stackexchange.com/questions/178626/how-to-normalize-data-between-1-and-1
fun IntArray.normalize(
    lowerBound: Int = Signal.MIN_16BIT_VALUE,
    upperBound: Int = Signal.MAX_16BIT_VALUE
): IntArray {
    if (isEmpty()) return IntArray(0)

    val minValue = this.minByOrNull { it }!!
    val maxValue = this.maxByOrNull { it }!!

    return if ((minValue >= lowerBound && maxValue <= upperBound)
        || (minValue == 0 && maxValue == 0))
        this
    else
        this.apply {
            for (i in this.indices){
                this[i] = (upperBound - lowerBound) *
                        ((this[i] - minValue) / (maxValue - minValue)) + lowerBound }
        }
}

fun IntArray.toShortArray() = ShortArray(this.size) { i -> this[i].toShort() }

fun IntArray.toCircularShortArray() = CircularShortArray(this.toShortArray())

//https://www.geeksforgeeks.org/gcd-two-array-numbers/
fun gcd(a: Int, b: Int): Int =
    if (a == 0) b
    else gcd(b % a, a)

fun lcm(a: Int, b: Int): Int =
    a * b / gcd(a, b)

fun List<Int>.lcm(): Int{
    val list = this.filter { it != 0 }
    return when (list.size){
        0 -> 0
        1 -> list[0]
        2 -> lcm(list[0], list[1])
        else -> list.reduce { lcm, value -> (lcm * value) / gcd(lcm, value) }
    }
}


//----- String -----//
operator fun String.times(multiplier: Int) = this.repeat(multiplier)

fun String.repeat(times: Int): String{
    val s = StringBuilder()
    repeat(times) { s.append(this) }
    return s.toString()
}

val logTab = "\t" * 152
