package com.example.synth

import android.util.Log
import java.lang.StringBuilder
import kotlin.system.measureTimeMillis


//----- List<Signal> ----- //
val signalsToSumSignal = mutableMapOf<Set<Signal>, Signal>()
fun List<Signal>.sum(): Signal{
    if (size == 0) return Signal.NullSignal
    val ret: Signal
    with (measureTimeMillis {
         ret = toSet().run{
            if (this !in signalsToSumSignal)
                signalsToSumSignal[this] = this.reduce { acc: Signal, sig: Signal -> acc + sig }

            signalsToSumSignal[this]!!
            }
        }
    ){ Log.d("m_time","$this milliseconds to sum $size notes") }

    Log.d("m_mapSize", "signalsToSumSignal size: ${signalsToSumSignal.size}")
    return ret
}



//----- List<Int> ----- //
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
    Log.d("m_memoryLeak","Loop ${newSize / this.size} times")
    repeat((newSize / this.size) + 1) { newList.addAll(this) }
    return newList.subList(0, newSize)
}

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