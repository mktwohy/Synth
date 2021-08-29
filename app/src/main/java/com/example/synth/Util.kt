package com.example.synth

import android.util.Rational
import androidx.compose.ui.graphics.Color
import com.example.synth.Constants.MAX_16BIT_VALUE
import com.example.synth.Constants.MIN_16BIT_VALUE
import java.lang.StringBuilder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.truncate
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

fun logTime(title: String = "", block: () -> Unit){
    measureTimeMillis { block() }.also { logd("$title $it ms") }
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



fun volumeToAmplitude(volume: Float) = volume.pow(3f)
fun amplitudeToVolume(amplitude: Float) = amplitude.pow(1/3f)

fun radianToDegree(radian: Float) = (radian *180 / PI).toFloat()
fun radianToDegree(radian: Double) = radianToDegree(radian.toFloat())
fun degreeToRadian(degree: Float) = (degree * PI / 180).toFloat()
fun degreeToRadian(degree: Double) = radianToDegree(degree.toFloat())

//----- Color -----//
fun Color.mix(that: Color) =
    Color(
    this.red/2 + that.red/2,
    this.green/2 + that.green/2,
    this.blue/2 + that.blue/2,
    this.alpha/2 + that.alpha/2
    )

operator fun Color.plus(that: Color) = this.mix(that)

//----- Rational ----- //
fun Rational.times(that: Int) = Rational(numerator*that, numerator)


//----- IntArray ----- //
/** Performs an in-place mapping of an IntArray*/
fun IntArray.mapInPlace(transform: (Int) -> Int){
    for(i in indices){
        this[i] = transform(this[i])
    }
}

fun IntArray.mapInPlaceIndexed(transform: (Int, Int) -> Int){
    for(i in indices){
        this[i] = transform(i, this[i])
    }
}

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
    // (I would use in range, but this produces excess memory)
    if ((minValue == 0 && maxValue == 0)
        || (maxValue <= upperBound && maxValue > upperBound-2
                && minValue >= lowerBound && minValue < lowerBound+2)) {
        return
    }

    //Normalize
    for (i in indices) {
        this[i] = ( ((boundRange * (this[i] - minValue)) / valueRange) + lowerBound ).toInt()
    }
}

fun IntArray.toShortArray(destination: ShortArray){
    if (this.size != destination.size)
        throw Exception("Cannot clone to array of different size")
    for (i in destination.indices){
        destination[i] = this[i].toShort()
    }
}


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

fun FloatArray.mapInPlace(transform: (Float) -> Float){
    for(i in indices){
        this[i] = transform(this[i])
    }
}

fun FloatArray.clear(){
    indices.forEach{ i -> this[i] = 0f }
}

fun FloatArray.toShortArray(destination: ShortArray, scalar: Int){
    if (this.size != destination.size)
        throw Exception("Cannot clone to array of different size")
    for (i in destination.indices){
        destination[i] = (this[i] * scalar).toInt().toShort()
    }
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