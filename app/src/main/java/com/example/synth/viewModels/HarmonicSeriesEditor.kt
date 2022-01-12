package com.example.synth.viewModels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.signallib.SignalSettings
import com.example.signallib.volumeToAmplitude
import com.example.synth.AppModel
import com.example.synth.mapInPlace
import com.example.synth.mapInPlaceIndexed
import java.util.*


class HarmonicSeriesViewModel(
    val signalSettings: SignalSettings
): ViewModel(){
    val sliderState = mutableStateListOf<Float>()

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

    fun reset(){
        signalSettings.harmonicSeries.reset()
        sliderState.mapInPlace { 0f }
    }

    fun random(){
        AppModel.signalEngine.registerAfterBufferWriteOneTimeCallback {
            signalSettings.harmonicSeries.generateRandom()
            sliderState.mapInPlaceIndexed { i, _ -> signalSettings.harmonicSeries[i+1] }
        }

    }
}