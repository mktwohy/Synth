package com.example.synth

import com.example.synth.AudioGenerator.chord
import com.example.synth.AudioGenerator.sine
import com.example.synth.AudioGenerator.sinusoid
import com.example.synth.Interval.*
import kotlin.math.PI
import kotlin.math.sin

object AudioGenerator {
    const val TWO_PI              = 2.0 * PI
    const val MIN_16BIT_VALUE     = -32_768
    const val MAX_16BIT_VALUE     = 32_767
    val sine = { i: Int, period: Int ->
        (sin(TWO_PI * i / period) * MAX_16BIT_VALUE).toInt()
    }
    val cosine = { i: Int, period: Int ->
        (sin(TWO_PI * i / period) * MAX_16BIT_VALUE).toInt()
    }

    val silence = CircularIntArray(AudioEngine.BUFFER_SIZE)

    fun sinusoid(freq: Int, func: (Int, Int) -> Int): CircularIntArray {
        val period = calculatePeriod(freq)
        return CircularIntArray(period) { i -> func(i, period) }
    }

    fun sinusoid(freqs: Set<Int>, func: (Int, Int) -> Int): CircularIntArray {
        val freqToPeriod = mutableMapOf<Int, Int>().apply {
            freqs.forEach{ freq -> put(freq, calculatePeriod(freq)) }
        }
        val intervalSize = calculateCommonInterval(freqs)
        return CircularIntArray(intervalSize){ i ->
            freqs.fold(0){
                    sumAtIndex, freq -> sumAtIndex + func(i, freqToPeriod[freq]!!)
            }
        }
    }

    fun chord(fundamental: Note, overtones: Set<Int>): MutableSet<Int>{
        return mutableSetOf(fundamental.freq).apply{
            for(o in overtones){
                add(fundamental.freq * o)
            }
        }
    }

    private fun calculateCommonInterval(freqs: Set<Int>) =
        freqs.map { calculatePeriod(it) }.lcm()

    private fun calculatePeriod(freq: Int) = AudioEngine.SAMPLE_RATE / freq
}

fun main(){
    //println(sinusoid(440, sine))
    println((sinusoid(chord(Note.C_4, setOf(1, 3, 5, 7)), sine)).size)

}