package com.example.synth.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.signallib.SignalSettings
import com.example.signallib.enums.HarmonicFilter
import com.example.synth.AppModel
import com.example.synth.logd

class HarmonicDialsViewModel(
    val signalSettings: SignalSettings
): ViewModel() {
    var decayState by mutableStateOf(0f)
    var floorState by mutableStateOf(0f)
    var ceilingState by mutableStateOf(1f)
    var filterState by mutableStateOf(HarmonicFilter.ALL)

    init {
        AppModel.signalSettings.registerHarmonicSeriesListener {
            logd("update!!!")
        }
    }

    fun updateHarmonicSeries(){
        signalSettings.harmonicSeries.generate(
            (0f..100f).getValueAt(decayState * 100f),
            (0f..100f).getValueAt(floorState * 100f),
            (0f..100f).getValueAt(ceilingState * 100f),
            filterState.function
        )
    }

    private fun ClosedRange<Float>.getValueAt(percent: Float): Float{
        val rangeSize = this.endInclusive - this.start
        return (rangeSize * percent / 100f) + this.start
    }
}
