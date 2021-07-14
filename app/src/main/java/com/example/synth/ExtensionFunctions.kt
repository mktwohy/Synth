package com.example.synth

import android.util.Log

//----- List<Signal> ----- //
fun List<Signal>.sum() = when(size){
    0 -> NullSignal()
    1 -> this[0]
    2 -> SumSignal(this[0], this[1])
    else -> this.reduce { sumSig: Signal, nextSig: Signal ->
        sumSig + nextSig
    }
}



//----- List<Int> ----- //
fun List<Int>.lcm(): Int{
    //https://www.geeksforgeeks.org/gcd-two-array-numbers/
    fun gcd(a: Int, b: Int): Int =
        if (a == 0) b
        else gcd(b % a, a)

    fun lcm(a: Int, b: Int): Int =
        a / gcd(a, b) * b

    var lcm = this[0]
    for (i in this.indices) {
        lcm *= gcd(lcm, this[i])
    }
    Log.d("m_lcm","$lcm")
    return lcm
}

fun List<Int>.toCircularShortArray(): CircularShortArray{
    val ret = CircularShortArray(this.size)
    for(i in this.indices){
        ret[i] = this[i].toShort()
    }
    return ret
}



//------ List<Float> ----- //
fun List<Float>.loopToFill(newSize: Int): List<Float>{
    val newList = mutableListOf<Float>()
    repeat((newSize / this.size) + 1) { newList.addAll(this) }
    return newList.subList(0, newSize - 1)
}

//https://stats.stackexchange.com/questions/178626/how-to-normalize-data-between-1-and-1
fun List<Float>.normalize(
    lowerBound: Float = -1f,
    upperBound: Float = 1f
) =
    if (size > 0) {
        val minValue = this.minByOrNull { it }!!
        val maxValue = this.maxByOrNull { it }!!

        if (minValue >= -1 && maxValue <= 1) this
        else this.map {
            (upperBound - lowerBound) * ( (it - minValue) / (maxValue - minValue) ) + lowerBound
        }
    }
    else NullSignal().data


fun List<Float>.toIntList(scalar: Int) =
    this.map { (it * scalar).toInt() }