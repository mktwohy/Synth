package com.example.signallib


import com.example.signallib.enums.HarmonicFilter
import java.lang.StringBuilder
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow
import kotlin.random.Random

class HarmonicSeries(
    val numHarmonics: Int
) : Iterable<Pair<Int, Float>> {
    //indices refer to overtone-1. for example, index 0 refers to the first overtone
    private val amplitudes = FloatArray(numHarmonics){ 0f }

    private val broadcaster = Broadcaster<HarmonicSeries>()

    fun registerOnUpdatedCallback(callback: (HarmonicSeries) -> Unit){
        broadcaster.registerListener(callback)
    }

    operator fun get(overtone: Int) = amplitudes[overtone-1]
    operator fun set(overtone: Int, amplitude: Float){
        if(overtone in 1..numHarmonics && amplitude in 0f..1f){
            amplitudes[overtone-1] = amplitude
        }
        broadcaster.broadcast(this)
    }

    fun reset(){
        amplitudes.mapInPlace{ 0f }
        broadcaster.broadcast(this)
    }

    /** produces a harmonic series with exponential decay
     * (represented as a map of overtones to amplitude) */
    fun generate(
        decayRate: Float = 0.75f,
        floor: Float = 0.01f,
        ceiling: Float = 1.0f,
        filter: (Int) -> Boolean = HarmonicFilter.ALL.function
    ) {

        for (index in amplitudes.indices) {
            val overtone = index + 1
            if (filter(overtone)) {
                amplitudes[index] = ((ceiling - floor) * (1f - decayRate).pow(index) + floor)
            }
        }
        broadcaster.broadcast(this)

    }

    fun generateRandom(){
        generate(
            decayRate   = (0..100).random() / 100f,
            floor       = (0..20).random() / 100f,
            ceiling     = (90..100).random() / 100f,
            filter = { i ->
                HarmonicFilter.values()
                    .filter { it != HarmonicFilter.FUNDAMENTAL }
                    .random()
                    .function(i)
                ||
                if(Random.nextBoolean())
                    HarmonicFilter.FUNDAMENTAL.function(i)
                else !HarmonicFilter.FUNDAMENTAL.function(i)
            }
        )
    }

    override fun toString(): String {
        fun Int.numDigits() = when(this) {
            0 -> 1
            else -> log10(abs(toDouble())).toInt() + 1
        }
        fun createRow(overtone: Int, amplitude: Float): String{
            val s = StringBuilder()
            s.append("$overtone")
            repeat(numHarmonics.numDigits() - overtone.numDigits()){
                s.append(" ")
            }
            s.append("|")
            s.append("#"*(amplitude*100).toInt())
            return s.toString()
        }

        val s = StringBuilder()
        for(overtone in 1..numHarmonics){
            s.append(createRow(overtone, this[overtone]))
            s.append("\n")
        }
        return s.toString()
    }

    override fun iterator() =
        amplitudes.mapIndexed { index, amplitude ->
            index+1 to amplitude
        }.iterator()
}

