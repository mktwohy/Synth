package com.example.synth

import com.example.synth.AudioGenerator.MAX_16BIT_VALUE
import com.example.synth.AudioGenerator.MIN_16BIT_VALUE
import java.lang.Exception
import java.lang.StringBuilder




//----- IntArray ----- //
//https://stats.stackexchange.com/questions/178626/how-to-normalize-data-between-1-and-1
//https://stackoverflow.com/questions/1226587/how-to-normalize-a-list-of-int-values
fun IntArray.normalize(
    lowerBound: Int = MIN_16BIT_VALUE,
    upperBound: Int = MAX_16BIT_VALUE
) {
    //Check that array isn't empty
    if (isEmpty()) return

    val minValue   = this.minByOrNull { it }!!
    val maxValue   = this.maxByOrNull { it }!!
    val valueRange = (maxValue - minValue).toFloat()
    val boundRange = (upperBound - lowerBound).toFloat()

    //Check that array isn't already normalized
    if ((minValue >= lowerBound && maxValue <= upperBound)
        || (minValue == 0 && maxValue == 0)) {
        return
    }

    //Normalize
    for (i in indices) {
        this[i] = ( ((boundRange * (this[i] - minValue)) / valueRange) + lowerBound ).toInt()
    }
}

fun IntArray.toShortArray() = ShortArray(this.size) { i -> this[i].toShort() }


//----- List<Int> ----- //
//https://www.geeksforgeeks.org/gcd-two-array-numbers/

fun List<Int>.lcm(): Int{
    fun gcd(a: Int, b: Int): Int =
        if (a == 0) b
        else gcd(b % a, a)

    fun lcm(a: Int, b: Int): Int =
        a * b / gcd(a, b)

    return when (this.size){
        0 -> 0
        1 -> this[0]
        2 -> lcm(this[0], this[1])
        else -> this.reduce { lcm, value -> (lcm * value) / gcd(lcm, value) }
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
