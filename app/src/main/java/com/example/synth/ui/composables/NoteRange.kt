package com.example.synth.ui.composables

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.RangeSlider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.signallib.enums.Note
import com.example.synth.viewModels.PianoViewModel

@ExperimentalMaterialApi
@Composable
fun NoteRange(
    modifier: Modifier = Modifier,
    viewModel: PianoViewModel
){
    RangeSlider(
        modifier = modifier,
        values = viewModel.rangeSliderState,
        onValueChange = {
            viewModel.noteRange = it.start.toNote()..it.endInclusive.toNote()
            viewModel.pianoGrid.recalculateWidths()
            viewModel.rangeSliderState = it
        }
    )
}
fun Float.toNote() = Note.notes[(this * (Note.notes.size-1)).toInt()]
fun Note.toFloat() = Note.notes.indexOf(this).toFloat() / Note.notes.size.toFloat()