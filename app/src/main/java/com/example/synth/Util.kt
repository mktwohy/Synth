package com.example.synth

import android.util.Log
import java.lang.StringBuilder
import kotlin.system.measureTimeMillis

fun makeHashTagBar(yValue: Int): String{
    val s = StringBuilder()
    repeat(yValue){ s.append("#") }
    return s.toString()
}

operator fun String.times(multiplier: Int) = this.repeat(multiplier)

fun String.repeat(times: Int): String{
    val s = StringBuilder()
    repeat(times) { s.append(this) }
    return s.toString()
}

val logTab = "\t" * 152

//----- List<Signal> ----- //
val signalsToSumSignal = mutableMapOf<Set<Signal>, Signal>()
fun List<Signal>.sum(): Signal{
    if (size == 0) return Signal.NullSignal
    val ret: Signal
    val t = measureTimeMillis {
         ret = toSet().run{
            if (this !in signalsToSumSignal)
                signalsToSumSignal[this] = this.reduce { acc: Signal, sig: Signal -> acc + sig }

            signalsToSumSignal[this]!!
        }
    }

    Log.d("m_mapSize", "signalsToSumSignal size: ${signalsToSumSignal.size}")
    Log.d("m_time","$t milliseconds to sum $size notes")
    return ret
}



//----- List<Int> ----- //
//https://www.geeksforgeeks.org/gcd-two-array-numbers/
fun gcd(a: Int, b: Int): Int =
    if (a == 0) b
    else gcd(b % a, a)

fun lcm(a: Int, b: Int): Int =
    a / gcd(a, b) * b


fun List<Int>.lcm(): Int{
    return when (size){
        0 -> 0
        1 -> this[0]
        2 -> lcm(this[0], this[1])
        else -> run{
            var lcm = this[0]
            println("0: $lcm")
            for (i in indices) {
                if(i != 0){
                    lcm *= lcm(lcm, this[i])
                    println("$i: $lcm")

                }
            }
            lcm
        }
    }
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
    return newList.subList(0, newSize)
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
        else this
            .map {
                (upperBound - lowerBound) * ( (it - minValue) / (maxValue - minValue) ) + lowerBound
            }
    }
    else NullSignal().data


fun List<Float>.toIntList(scalar: Int) =
    this.map { (it * scalar).toInt() }