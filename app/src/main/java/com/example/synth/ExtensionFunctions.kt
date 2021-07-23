package com.example.synth

import android.util.Log
import java.lang.StringBuilder
import kotlin.system.measureTimeMillis


//----- List<Signal> ----- //
val signalsToSumSignal = mutableMapOf<Set<Signal>, Signal>() //stores results of List<Signal>.sum() for faster sum times
//fun List<Signal>.sum(): Signal{
//    if (size == 0) return Signal.NullSignal
//    val ret: Signal
//    with (measureTimeMillis {
//         ret = toSet().run{
//            if (this !in signalsToSumSignal)
//                signalsToSumSignal[this] = this.reduce { acc: Signal, sig: Signal -> acc + sig }
//
//            signalsToSumSignal[this]!!
//            }
//        }
//    ){ Log.d("m_time","$this milliseconds to sum $size notes") }
//
//    Log.d("m_mapSize", "signalsToSumSignal size: ${signalsToSumSignal.size}")
//    return ret
//}
fun List<Signal>.sum(): Signal{
    val signalSet = this.toSet()
    if(signalSet in signalsToSumSignal) return signalsToSumSignal[signalSet]!!
    val ret = when (size){
        0 -> Signal.NullSignal
        1 -> this[0]
        2 -> SumSignal(this[0], this[1])
        else -> SumSignal(this.toSet())
    }
    return ret.also { signalsToSumSignal[signalSet] = it }

}

//----- List<Int> ----- //
fun List<Int>.toCircularShortArray(): CircularShortArray{
    val ret = CircularShortArray(this.size)
    for(i in this.indices){
        ret[i] = this[i].toShort()
    }
    return ret
}

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



//------ List<Float> ----- //
//https://stats.stackexchange.com/questions/178626/how-to-normalize-data-between-1-and-1
fun List<Float>.normalize(
    lowerBound: Float = -1f,
    upperBound: Float = 1f
): List<Float> {
    return if (size > 0) {
        val minValue = this.minByOrNull { it }!!
        val maxValue = this.maxByOrNull { it }!!

        if (minValue >= -1 && maxValue <= 1)
            this
        else
            this.map { (upperBound - lowerBound) * ((it - minValue) / (maxValue - minValue)) + lowerBound }
    } else {
        NullSignal().data
    }
}


fun List<Float>.toIntList(scalar: Int) =
    this.map { (it * scalar).toInt() }



//----- String -----//
operator fun String.times(multiplier: Int) = this.repeat(multiplier)

fun String.repeat(times: Int): String{
    val s = StringBuilder()
    repeat(times) { s.append(this) }
    return s.toString()
}

val logTab = "\t" * 152