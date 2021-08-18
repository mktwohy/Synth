package com.example.synth

import java.lang.StringBuilder
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow

class HarmonicSeries(numOvertones: Int = 1) {
    companion object{
        val fundamental = { i: Int -> i == 1 }
        val odd         = { i: Int -> i % 2 != 0 }
        val even        = { i: Int -> i % 2 == 0 }
        val all         = { _: Int -> true }
        val none        = { _: Int -> false }
    }

    //indices refer to overtone-1. for example, index 0 refers to the first overtone
    private var harmonicSeries = FloatArray(numOvertones){ 0f }

    private val callbacks = mutableListOf<(HarmonicSeries) -> Unit>()
    private fun invokeCallbacks(){ callbacks.forEach { it.invoke(this) } }
    fun registerCallback(onHarmonicSeriesUpdated: (HarmonicSeries) -> Unit){
        callbacks.add(onHarmonicSeriesUpdated)
    }

    var numOvertones: Int = 1
        set(value){
            if(numOvertones >= 1) field = value
            val copy = harmonicSeries.copyOf()
            harmonicSeries = FloatArray(numOvertones){ 0f }
            for(i in harmonicSeries.indices){
                harmonicSeries[i] = if(i in copy.indices) copy[i] else 0f
            }
            invokeCallbacks()
        }

    init { this.numOvertones = numOvertones }

    operator fun get(overtone: Int) = harmonicSeries[overtone-1]
    operator fun set(overtone: Int, amplitude: Float){
        if(overtone in 1..numOvertones && amplitude in 0f..1f){
            harmonicSeries[overtone-1] = amplitude
        }
        invokeCallbacks()
    }

    /** produces a harmonic series with exponential decay
     * (represented as a map of overtones to amplitude) */
    fun generate(
        decayRate: Float = 0.75f,
        floor: Float = 0.01f,
        ceiling: Float = 1.0f,
        filter: (Int) -> Boolean = all
    ) {
        for(index in harmonicSeries.indices){
            val overtone = index+1
            if(filter(overtone)){
                this[overtone] = ((ceiling-floor) * (1f-decayRate).pow(index) + floor)
            }
        }
        invokeCallbacks()
    }

    override fun toString(): String {
        fun Int.length() = when(this) {
            0 -> 1
            else -> log10(abs(toDouble())).toInt() + 1
        }
        fun createRow(overtone: Int, amplitude: Float): String{
            val s = StringBuilder()
            s.append("$overtone")
            repeat(numOvertones.length() - overtone.length()){
                s.append(" ")
            }
            s.append("|")
            s.append("#"*(amplitude*100).toInt())
            return s.toString()
        }

        val s = StringBuilder()
        for(overtone in 1..numOvertones){
            s.append(createRow(overtone, this[overtone]))
            s.append("\n")
        }
        return s.toString()
    }
}