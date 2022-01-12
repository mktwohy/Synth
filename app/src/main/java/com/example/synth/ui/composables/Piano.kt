package com.example.synth.ui.composables

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.signallib.enums.Note
import com.example.synth.AppModel
import com.example.synth.viewModels.PianoViewModel
import com.example.synth.color


@ExperimentalComposeUiApi
@Composable
fun Piano(
    modifier: Modifier,
    viewModel: PianoViewModel
){
    val density = LocalDensity.current
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInteropFilter(
                onTouchEvent = { event ->
                    val newPressedNotes = getPressedNotes(event, density, viewModel)

                    // compare newNotes to old notes. Only update notes if notes changed
                    if (newPressedNotes != viewModel.pressedNotes) {
                        viewModel.pressedNotes = newPressedNotes
                        AppModel.signalEngine.updateNotes(viewModel.pressedNotes)
                    }

                    true
                }
            )
    ){
        SideEffect {
            if(viewModel.width != this.maxWidth || viewModel.height != this.maxHeight){
                viewModel.width = this.maxWidth
                viewModel.height = this.maxHeight
                viewModel.pianoGrid.recalculateWidths()
            }
        }

        Column(Modifier.fillMaxSize()) {
            PianoRow(viewModel = viewModel, row = viewModel.pianoGrid.topRow)
            PianoRow(viewModel = viewModel, row = viewModel.pianoGrid.bottomRow)
        }
        KeyBorders(viewModel)
    }
}

@Composable
private fun KeyBorders(viewModel: PianoViewModel) {
    Row(Modifier.fillMaxSize()) {
        for ((_, width) in viewModel.pianoGrid.bottomRow) {
            Box(
                Modifier
                    .size(width, viewModel.height)
                    .border(2.dp, Color.Black)
            )
        }
    }
}

@Composable
private fun PianoRow(
    viewModel: PianoViewModel,
    row: SnapshotStateList<Pair<Note, Dp>>
) {
    Row(
        modifier = Modifier
            .size(viewModel.width, viewModel.height / 2),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for ((note, width) in row) {
            Box(
                modifier = Modifier
                    .size(width, viewModel.height / 2)
                    .background(color = note.color(note in viewModel.pressedNotes))
            )
        }
    }
}


private fun getPressedNotes(
    event: MotionEvent,
    density: Density,
    viewModel: PianoViewModel,
): MutableSet<Note> {
    val pressedNotes = mutableSetOf<Note>()

    for (i in 0 until event.pointerCount) {

        // find the note/key the finger is touching
        val note = with(density) {
            viewModel.pianoGrid.findKeyAt(
                event.getX(i).toDp(),
                event.getY(i).toDp()
            )
        }

        // add or remove note from list
        if (note != null) {
            if (i == event.actionIndex) {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE
                    -> pressedNotes.add(note)

                    MotionEvent.ACTION_UP
                    -> pressedNotes.remove(note)
                }
            } else {
                pressedNotes.add(note)
            }
        }
    }

    return pressedNotes
}

