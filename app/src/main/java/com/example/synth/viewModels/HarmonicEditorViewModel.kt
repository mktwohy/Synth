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
import com.example.synth.logd
import com.example.synth.mapInPlace
import com.example.synth.mapInPlaceIndexed
import java.util.*


class HarmonicEditorViewModel(
    val signalSettings: SignalSettings,
): ViewModel(){
    val sliderState = mutableStateListOf<Float>()
    var decayState by mutableStateOf(0.5f)
    var floorState by mutableStateOf(0.4f)
    var ceilingState by mutableStateOf(0.5f)
    var filterState by mutableStateOf(setOf(HarmonicFilter.ALL))



    /** Queue of Pairs of sliderIndex, sliderValue */
    val harmonicSeriesUpdateQueue: Queue<Pair<Int, Float>> = LinkedList()

    init {
        with(signalSettings.harmonicSeries){
            for (i in 1..numHarmonics){
                sliderState.add(this[i]) }
        }
        AppModel.signalEngine.registerAfterBufferWriteCallback {
            if (harmonicSeriesUpdateQueue.isNotEmpty()){
                val (sliderIndex, sliderValue) = harmonicSeriesUpdateQueue.poll()!!
                signalSettings.harmonicSeries[sliderIndex+1] = volumeToAmplitude(sliderValue)
            }
        }
    }

    fun generate(){
        AppModel.signalEngine.registerAfterBufferWriteOneTimeCallback {
            signalSettings.harmonicSeries.generate(
                decayRate   = (0f..1f).getValueAt(decayState * 100f),
                floor       = (0f..1f).getValueAt(floorState * 100f),
                ceiling     = (0f..1f).getValueAt(ceilingState * 100f),
                filter      = calcBoolean
            )
            updateSliders()
        }

    }


    fun reset(){
        signalSettings.harmonicSeries.reset()
        updateSliders()
    }

    fun generateRandom(){
        AppModel.signalEngine.registerAfterBufferWriteOneTimeCallback {
            signalSettings.harmonicSeries.generateRandom()
            updateSliders()
        }

    }

    private val calcBoolean = { harmonic: Int ->
        this.filterState
            .map { it.function(harmonic) }
            .contains(true)
            .also { updateSliders() }
    }

    private fun updateSliders(){
        sliderState.mapInPlaceIndexed { i, _ -> signalSettings.harmonicSeries[i+1] }
        logd("size: ${harmonicSeriesUpdateQueue.size}")

    }

    private fun ClosedRange<Float>.getValueAt(percent: Float): Float{
        val rangeSize = this.endInclusive - this.start
        return (rangeSize * percent / 100f) + this.start
    }
}