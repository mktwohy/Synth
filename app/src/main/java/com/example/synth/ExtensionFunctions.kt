package com.example.synth

import android.util.Rational
import com.example.synth.CircularIntArray.Companion.MAX_16BIT_VALUE
import com.example.synth.CircularIntArray.Companion.MIN_16BIT_VALUE
import java.lang.StringBuilder

//----- Rational ----- //
fun Rational.times(that: Int) = Rational(numerator*that, numerator)


//----- IntArray ----- //
/** Performs an in-place mapping of an IntArray*/
fun IntArray.mapInPlace(transform: (Int) -> Int){
    for(i in indices){
        this[i] = transform(this[i])
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

fun FloatArray.mapInPlace(transform: (Float) -> Float){
    for(i in indices){
        this[i] = transform(this[i])
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

fun FloatArray.plotInConsoleEfficient(width: Float = 50f){
    this
        .also { it.normalize(0f, width) }
        .forEach {
            repeat(it.toInt()){
                print(" ")
            }
            println("#")
        }

}
fun FloatArray.plotInConsole(plotHeight: Int = 20, displayVertical: Boolean = false){
    val min     = this.minOrNull() ?: 0f
    val max     = this.maxOrNull() ?: 0f
    val middle  = (min + max) / 2

    val topRatio    = (max - middle) / (max - min)
    val bottomRatio = (middle - min) / (max - min)

    val scaledValues = this.map {
        if(it >= middle)
            it * topRatio * (plotHeight -1)
        else
            it * bottomRatio * (plotHeight -1)
    }

    val scaledMin = min * topRatio * (plotHeight-1)
    val shiftUp = if (min *topRatio * (plotHeight-1)  < 0) scaledMin*-1 else 0f

    val plotValues = scaledValues.map { it + shiftUp }

    val valueToString = plotValues.map { it to CharArray(plotHeight){ i -> ' ' } }
    for((value, string) in valueToString){

        string[0]              = '-'
        string[plotHeight/2]   = '-'
        string[plotHeight -1]  = '-'

        when{
            value > (plotHeight-1)   -> string[plotHeight-1] = 'C'
            value < 0                -> string[0] = 'C'
            else                     -> string[value.toInt()] = '#'
        }
    }


    if(displayVertical){
        for((_, string) in valueToString) {
            println(string)
        }
    }else{
        for(i in (0 until plotHeight).reversed()){
            for((value, string) in valueToString){
                print(string[i])
            }
            println()

        }
    }



}