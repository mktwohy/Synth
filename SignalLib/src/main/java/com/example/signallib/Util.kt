package com.example.signallib

import android.util.Rational
import com.example.signallib.signals.Signal
import java.lang.StringBuilder
import kotlin.math.PI
import kotlin.math.pow
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

fun volumeToAmplitude(volume: Float) = volume.pow(3f)
fun amplitudeToVolume(amplitude: Float) = amplitude.pow(1/3f)

fun radianToDegree(radian: Float) = (radian *180 / PI).toFloat()
fun radianToDegree(radian: Double) = radianToDegree(radian.toFloat())
fun degreeToRadian(degree: Float) = (degree * PI / 180).toFloat()
fun degreeToRadian(degree: Double) = radianToDegree(degree.toFloat())


//----- Rational ----- //
fun Rational.times(that: Int) = Rational(numerator*that, numerator)


//----- List<Int> ----- //
/** [source](https://www.geeksforgeeks.org/gcd-two-array-numbers/) */
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
        else -> this.reduce { lcm, value ->
            if (value == 0) 1
            else (lcm * value) / gcd(lcm, value)
        }
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

inline fun FloatArray.mapInPlace(transform: (Float) -> Float){
    this.indices.forEach{ this[it] = transform(this[it]) }
}


fun FloatArray.normalize(
    lowerBound: Float = -1f,
    upperBound: Float = 1f
) {
    //Check that array isn't empty
    if (isEmpty()) return

    val minValue   = this.minByOrNull { it }!!
    val maxValue   = this.maxByOrNull { it }!!
    val valueRange = (maxValue - minValue).toFloat()
    val boundRange = (upperBound - lowerBound).toFloat()

    //Check that array isn't already normalized
    // (I would use in range, but this produces excess memory)
    if ((minValue == 0f && maxValue == 0f)
        || (maxValue <= upperBound && maxValue > upperBound
                && minValue >= lowerBound && minValue < lowerBound)) {
        return
    }

    //Normalize
    for (i in indices) {
        this[i] = ((boundRange * (this[i] - minValue)) / valueRange) + lowerBound
    }
}

fun Signal.plotInConsole(
    allowClipping: Boolean = true,
    startFromBeginning: Boolean = true,
    periods: Int = 1,
    scale: Int = 17
){
    if(startFromBeginning) this.reset()
    val lowerBound = 0
    val upperBound = (scale.toFloat()+1).toInt()
    val middle = (upperBound + lowerBound) / 2

    val values = evaluate(periods)
    if(values.isEmpty()) return
    var min = values.minOrNull()!!
    var max = values.maxOrNull()!!

    val valueToString = values
        .apply {
            if (!allowClipping && (min < -1f || max > 1f)){
                min = -1f
                max = 1f
                normalize(min, max)
            }
            normalize(middle + min*scale/2, (middle + max*scale/2))
        }
        .map { value -> value to CharArray(scale+2){ ' ' } } // move up one

    for((value, string) in valueToString){
        string[lowerBound]      = '='
        string[upperBound/2]    = '-'
        string[upperBound]      = '='

        when{
            value >= upperBound  -> string[upperBound] = '!'
            value <= lowerBound  -> string[lowerBound] = '!'
            else                 -> string[value.toInt()] = '#'

        }
    }

    println(this)
    for(i in (scale+1 downTo 0)){
        for((_, string) in valueToString){
            print(string[i])
        }
        println()
    }
    println("\n\n")
}

fun List<Float>.toHistogram(scale: Int): String{
    val sb = StringBuilder()
    this.forEach{
        val scaledValue = (scale * it).toInt()
        if(it < 0){
            sb.append(" " * (scale - scaledValue))
        }else{
            sb.append(" " * (scale/2))
        }
        sb.append("#" * scaledValue)
        sb.append("\n")
    }
    return sb.toString()
}

fun <T>MutableCollection<T>.replaceAll(elements: Collection<T>){
    this.clear()
    this.addAll(elements)
}

fun printTime(title: String = "", block: () -> Unit){
    measureTimeMillis { block() }.also { println("$title $it ms") }
}
fun printAvgTimeMillis(title: String = "", repeat: Int = 100, block: () -> Unit){
    println("$title avg: ${avgTimeMillis(repeat, block)}")
}
fun avgTimeMillis(repeat: Int, block: () -> Unit): Double {
    val times = mutableListOf<Long>()
    repeat(repeat){
        measureTimeMillis{ block() }
            .also{ times += it }
    }
    return times.average()
}
fun avgTimeNano(repeat: Int, block: () -> Any?): Double {
    val times = mutableListOf<Long>()
    repeat(repeat){
        measureNanoTime{ block() }
            .also{ times += it }
    }
    return times.average()
}