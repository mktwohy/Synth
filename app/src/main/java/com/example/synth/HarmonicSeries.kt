package com.example.synth

import java.lang.StringBuilder
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow
import kotlin.random.Random

class HarmonicSeries : Iterable<Pair<Int, Float>>{
    //indices refer to overtone-1. for example, index 0 refers to the first overtone
    private val harmonicSeries = FloatArray(Constants.NUM_HARMONICS){ 0f }

    private val callbacks = mutableListOf<() -> Unit>()
    private fun invokeCallbacks(){ callbacks.forEach { it.invoke() } }
    fun registerOnUpdatedCallback(callback: () -> Unit){
        callbacks.add(callback)
    }



    operator fun get(overtone: Int) = harmonicSeries[overtone-1]
    operator fun set(overtone: Int, amplitude: Float){
        if(overtone in 1..Constants.NUM_HARMONICS && amplitude in 0f..1f){
            harmonicSeries[overtone-1] = amplitude
        }
        invokeCallbacks()
    }

    fun reset(){
        harmonicSeries.indices.forEach{ harmonicSeries[it] = 0f }
        invokeCallbacks()
    }

    /** produces a harmonic series with exponential decay
     * (represented as a map of overtones to amplitude) */
    fun generate(
        decayRate: Float = 0.75f,
        floor: Float = 0.01f,
        ceiling: Float = 1.0f,
        filter: (Int) -> Boolean = HarmonicFilter.ALL.function
    ) {
        for(index in harmonicSeries.indices){
            val overtone = index+1
            if(filter(overtone)){
                this[overtone] = ((ceiling-floor) * (1f-decayRate).pow(index) + floor)
            }
        }
        invokeCallbacks()
    }

    fun generateRandom(){
        val decayRate   = (0..50).random() / 100f
        val floor       = (10..60).random() / 100f
        val ceiling     = (90..100).random() / 100f
        logd("Random: \ndecay: $decayRate \nfloor: $floor \nceiling: $ceiling")
        generate(
            decayRate   = decayRate,
            floor       = floor,
            ceiling     = ceiling,
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
        fun Int.length() = when(this) {
            0 -> 1
            else -> log10(abs(toDouble())).toInt() + 1
        }
        fun createRow(overtone: Int, amplitude: Float): String{
            val s = StringBuilder()
            s.append("$overtone")
            repeat(Constants.NUM_HARMONICS.length() - overtone.length()){
                s.append(" ")
            }
            s.append("|")
            s.append("#"*(amplitude*100).toInt())
            return s.toString()
        }

        val s = StringBuilder()
        for(overtone in 1..Constants.NUM_HARMONICS){
            s.append(createRow(overtone, this[overtone]))
            s.append("\n")
        }
        return s.toString()
    }

    override fun iterator() =
        harmonicSeries.mapIndexed { index, amplitude ->
            index+1 to amplitude
        }.iterator()
}