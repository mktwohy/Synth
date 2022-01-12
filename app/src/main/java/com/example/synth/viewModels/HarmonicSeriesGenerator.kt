package com.example.synth.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.signallib.SignalSettings
import com.example.signallib.enums.HarmonicFilter

class HarmonicSeriesGeneratorViewModel(
    val signalSettings: SignalSettings
): ViewModel() {
    var decayPercent by mutableStateOf(0f)
    var floorPercent by mutableStateOf(0f)
    var ceilingPercent by mutableStateOf(0f)
    var filter by mutableStateOf(HarmonicFilter.ALL)

    fun update(){
        signalSettings.harmonicSeries.generate(
            (0f..100f).getValueAt(decayPercent),
            (0f..100f).getValueAt(floorPercent),
            (0f..100f).getValueAt(ceilingPercent),
            filter.function
        )
    }

    fun ClosedRange<Float>.getValueAt(percent: Float): Float{
        val rangeSize = this.endInclusive - this.start
        return (rangeSize * percent / 100f) + this.start
    }
}
