package com.example.synth.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.signallib.SignalSettings
import com.example.signallib.enums.HarmonicFilter
import com.example.signallib.volumeToAmplitude
import com.example.synth.AppModel
import com.example.synth.mapInPlaceIndexed
import java.util.*
import kotlin.random.Random


class HarmonicEditorViewModel(
    val signalSettings: SignalSettings,
): ViewModel(){
    val sliderState = mutableStateListOf<Float>()
    var decayState by mutableStateOf(0.3f)
    var floorState by mutableStateOf(0.2f)
    var ceilingState by mutableStateOf(0.9f)
    var evenState by mutableStateOf(true)
    var oddState by mutableStateOf(true)


    private val filters = mutableSetOf(HarmonicFilter.ALL)

    init {
        // initialize slider state
        with(signalSettings.harmonicSeries){
            for (i in 1..numHarmonics){
                sliderState.add(this[i]) }
        }
    }

    fun editHarmonicSeries(harmonic: Int, magnitude: Float){
        AppModel.signalEngine.registerAfterBufferWriteOneTimeCallback {
            signalSettings.harmonicSeries[harmonic+1] = volumeToAmplitude(magnitude)
        }
    }

    fun updateHarmonicSeries(){
        AppModel.signalEngine.registerAfterBufferWriteOneTimeCallback {
            signalSettings.harmonicSeries.generate(
                decayRate   = volumeToAmplitude(decayState),
                floor       = volumeToAmplitude(floorState),
                ceiling     = volumeToAmplitude(ceilingState),
                filter      = filter
            )
            updateSliders()
        }
    }

    fun reset(){
        decayState = 0.9f
        floorState = 0.1f
        ceilingState = 0.9f
        updateHarmonicSeries()
    }

    fun generateRandom(){
        decayState = Random.nextFloat()
        floorState = Random.nextFloat()
        ceilingState = Random.nextFloat()

        evenState = Random.nextBoolean()
        oddState = Random.nextBoolean()

        // ensure that at least one is true
        if (!evenState && !oddState){
            if (Random.nextBoolean())
                evenState = true
            else
                oddState = true
        }
        applyFilters()
        updateHarmonicSeries()
    }

    private val filter = { harmonic: Int ->
        this.filters
            .map { it.function(harmonic) }
            .contains(true)
    }

    fun applyFilters(){
        val newFilters = mutableSetOf<HarmonicFilter>()

        if (evenState) {
            newFilters += HarmonicFilter.EVEN
            newFilters += HarmonicFilter.FUNDAMENTAL
        }
        if (oddState)
            newFilters += HarmonicFilter.ODD

        filters.clear()
        filters.addAll(newFilters)
        updateHarmonicSeries()
    }

    private fun updateSliders(){
        sliderState.mapInPlaceIndexed { i, _ -> signalSettings.harmonicSeries[i+1] }
    }

    private fun ClosedRange<Float>.getValueAt(percent: Float): Float{
        val rangeSize = this.endInclusive - this.start
        return (rangeSize * percent / 100f) + this.start
    }
}