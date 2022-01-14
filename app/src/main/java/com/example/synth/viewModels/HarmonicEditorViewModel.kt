package com.example.synth.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.signallib.HarmonicSeries
import com.example.signallib.SignalSettings
import com.example.signallib.enums.HarmonicFilter
import com.example.signallib.volumeToAmplitude
import com.example.synth.AppModel
import com.example.synth.mapInPlaceIndexed
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

    init {
        // initialize slider state
        with(signalSettings.harmonicSeries){
            for (i in 1..numHarmonics){
                sliderState.add(this[i]) }
        }
    }

    fun editHarmonicSeries(block: (HarmonicSeries) -> Unit) {
        AppModel.signalEngine.registerAfterBufferWriteOneTimeCallback {
            block(signalSettings.harmonicSeries)
        }
    }

    fun editHarmonicSeries(harmonic: Int, amp: Float){
        editHarmonicSeries { hs ->
            hs[harmonic+1] = volumeToAmplitude(amp)
        }
    }

    fun editHarmonicSeries(transform: (Int, Float) -> Float){
        editHarmonicSeries { hs ->
            hs.map { (harm, amp) ->
                volumeToAmplitude(transform(harm, amp))
            }
            updateSliders()
        }
    }



    fun applyDialsAndFilters(){
        editHarmonicSeries { hs ->
            hs.generate(
                decayRate   = volumeToAmplitude(decayState),
                floor       = volumeToAmplitude(floorState),
                ceiling     = volumeToAmplitude(ceilingState),
                filter      = compileFilters()
            )
            updateSliders()
        }
    }

    fun resetDialsAndFilters(){
        decayState = 0.9f
        floorState = 0.1f
        ceilingState = 0.9f

        applyDialsAndFilters()
    }

    fun randomizeDialsAndFilters(){
        resetDialsAndFilters()

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
        applyDialsAndFilters()
    }

    private fun compileFilters(): (Int) -> Boolean {
        val filters = mutableSetOf<HarmonicFilter>()

        if (evenState) {
            filters += HarmonicFilter.EVEN
            filters += HarmonicFilter.FUNDAMENTAL
        }
        if (oddState)
            filters += HarmonicFilter.ODD

        return { harmonic: Int ->
            filters
                .map { it.function(harmonic) }
                .contains(true)
        }
    }

    private fun updateSliders(){
        sliderState.mapInPlaceIndexed { i, _ -> signalSettings.harmonicSeries[i+1] }
    }

    private fun ClosedRange<Float>.getValueAt(percent: Float): Float{
        val rangeSize = this.endInclusive - this.start
        return (rangeSize * percent / 100f) + this.start
    }
}