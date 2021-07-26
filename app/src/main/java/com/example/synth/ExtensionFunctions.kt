package com.example.synth

import java.lang.StringBuilder


//----- List<Signal> ----- //

fun List<Signal>.sum(): Signal{
    val signalSet = this.toSet()
    if(signalSet in signalsToSumSignal) return signalsToSumSignal[signalSet]!!
    return when (size){
        0 -> NullSignal
        1 -> this[0]
        else -> SumSignal(this.toSet())
    }.also { signalsToSumSignal[signalSet] = it }
}

//----- IntArray ----- //
//https://stats.stackexchange.com/questions/178626/how-to-normalize-data-between-1-and-1
//https://stackoverflow.com/questions/1226587/how-to-normalize-a-list-of-int-values
fun IntArray.normalize(
    lowerBound: Int = Signal.MIN_16BIT_VALUE,
    upperBound: Int = Signal.MAX_16BIT_VALUE
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
