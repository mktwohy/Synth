package com.example.synth

import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.synth.Note.Companion.color
import com.example.synth.Note.Companion.minus
import com.example.synth.Note.Companion.plus
import com.example.synth.Note.Companion.toList
import kotlin.math.pow

fun logd(text: String){ Log.d("m_tag",text) }

class PianoGrid(
    val width: MutableState<Dp>,
    val height: MutableState<Dp>,
    var noteRange: ClosedRange<Note>
){
    val topRow = mutableListOf<Pair<Note, Dp>>()
    val bottomRow = mutableListOf<Pair<Note, Dp>>()

    fun recalculateWidths(){
        topRow.clear()
        bottomRow.clear()
        val whiteNotes = noteRange.toList().filter { it.name[1] == '_' }
        whiteNotes.forEach { whiteNote ->
            for((note, ratio) in topRowNoteRatios(whiteNote)){
                topRow.add(note to width.value * ratio/whiteNotes.size)
            }
            bottomRow.add(whiteNote to width.value / whiteNotes.size)
        }
    }

    fun findKeyAt(x: Dp, y: Dp): Note? {
        var cumSum: Dp
        fun searchRow(row: List<Pair<Note, Dp>>): Note?{
            cumSum = 0.dp
            for ((note, width) in row){
                if (x in cumSum..cumSum+width) return note
                else cumSum += width
            }
            return null
        }
        return when{
            y < 0.dp || y > height.value -> null
            y < height.value / 2 -> searchRow(topRow)
            else -> searchRow(bottomRow)
        }

    }

    private fun topRowNoteRatios(whiteNote: Note) =
        when(whiteNote.toString()[0]) {
            'C' -> listOf(whiteNote to 3/4f, (whiteNote + 1) to 1/4f)
            'D' -> listOf((whiteNote - 1) to 1/4f, whiteNote to 1/2f, (whiteNote + 1) to 1/4f)
            'E' -> listOf((whiteNote - 1) to 1/4f, whiteNote to 3/4f)
            'F' -> listOf(whiteNote to 3/4f, (whiteNote + 1) to 1/4f)
            'G' -> listOf((whiteNote - 1) to 1/4f, whiteNote to 2/4f, (whiteNote + 1) to 1/4f)
            'A' -> listOf((whiteNote - 1) to 1/4f, whiteNote to 2/4f, (whiteNote + 1) to 1/4f)
            'B' -> listOf((whiteNote - 1) to 1/4f, whiteNote to 3/4f)
            else -> listOf()
        }
}

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
                onTouchEvent = {
                    val newPressedNotes = mutableSetOf<Note>()

                    for (i in 0 until it.pointerCount) {
                        val note: Note?
                        with(density) {
                            note = viewModel.pianoGrid.findKeyAt(
                                it
                                    .getX(i)
                                    .toDp(),
                                it
                                    .getY(i)
                                    .toDp()
                            )
                        }
                        if (note != null) {
                            if (i == it.actionIndex) {
                                when (it.actionMasked) {
                                    MotionEvent.ACTION_DOWN,
                                    MotionEvent.ACTION_MOVE
                                    -> newPressedNotes.add(note)

                                    MotionEvent.ACTION_UP
                                    -> newPressedNotes.remove(note)
                                }
                            } else {
                                newPressedNotes.add(note)
                            }
                        }
                    }
                    if (newPressedNotes != viewModel.pressedNotes) {
                        viewModel.pressedNotes = newPressedNotes
                    }
                    AppModel.audioEngine.signalBuffer += AppModel.oscillator.bundleSignals()

                    true
                }
            )
    ){
        SideEffect {
            if(viewModel.width.value != this.maxWidth
                || viewModel.height.value != this.maxHeight
            ){
                viewModel.width.value = this.maxWidth
                viewModel.height.value = this.maxHeight
                viewModel.pianoGrid.recalculateWidths()
            }
        }

        Column(Modifier.fillMaxSize()) {
            for(row in listOf(viewModel.pianoGrid.topRow, viewModel.pianoGrid.bottomRow) ){
                Row(
                    modifier = Modifier
                        .size(viewModel.width.value, viewModel.height.value/2),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for ((note, width) in row){
                        Box(
                            modifier = Modifier
                                .size(width, viewModel.height.value / 2)
                                .background(note.color(note in viewModel.pressedNotes))
//                                .border(2.dp, Color.Black)
                        )
                    }
                }
            }
        }
        Row(Modifier.fillMaxSize()) {
            for((note, width) in viewModel.pianoGrid.bottomRow){
                Box(Modifier
                    .size(width, viewModel.height.value)
                    .border(2.dp, Color.Black)
                )
            }
        }
    }
}

@Composable
fun SignalPlot(
    modifier: Modifier,
    viewModel: SignalPlotViewModel,
    color: Color = Color.Green,
    strokeWidth: Float = 3f
){
    XYPlot(
        modifier = modifier,
        data = viewModel.plotData,
        color = color,
        strokeWidth = strokeWidth
    )
}

@Composable
fun XYPlot(
    modifier: Modifier = Modifier,
    data: List<Float>,
    color: Color = Color.Green,
    strokeWidth: Float = 3f,
) {
    Canvas(modifier = modifier) {
        for(i in 0..data.size-2){
            drawLine(
                start = Offset(
                    x = i * size.width / (data.size-1),
                    y = (data[i] * size.height/2) + (size.height/2)
                ),
                end = Offset(
                    x = (i+1) * size.width / (data.size-1),
                    y = (data[i+1] * size.height/2) + (size.height/2)
                ),
                color = color,
                strokeWidth = strokeWidth
            )
        }
    }
}

@Composable
fun PitchBend(
    modifier: Modifier = Modifier,
    viewModel: PitchBendViewModel
){
    VerticalSlider(
        modifier = modifier,
        value = viewModel.sliderState,
        valueRange = 0.8f..1.2f,
        onValueChange = {
            viewModel.oscillator.bend = it
        },
        onValueChangeFinished = {
            viewModel.oscillator.bend = 1f //snap back to 1f
        }
    )
}

@Composable
fun VolumeSlider(
    modifier: Modifier = Modifier,
    viewModel: VolumeSliderViewModel
){
    VerticalValueSlider(
        modifier = modifier,
        value = viewModel.sliderState,
        onValueChange = {
            viewModel.oscillator.amplitude = volumeToAmplitude(it)
        }
    )
}


@Composable
fun HarmonicSeriesEditor(
    modifier: Modifier = Modifier,
    viewModel: HarmonicSeriesViewModel
){
    RowOfVerticalValueSliders(
        modifier = modifier,
        numSliders = Constants.NUM_HARMONICS,
        value = { sliderIndex -> viewModel.sliderState[sliderIndex] },
        onValueChange = { sliderIndex, sliderValue ->
            val newSliderValue = if(sliderValue < 0.01f) 0f else sliderValue //snaps slider to 0
            viewModel.harmonicSeries[sliderIndex+1] = volumeToAmplitude(newSliderValue)
        }
    )
}

@Composable
fun RowOfVerticalValueSliders(
    modifier: Modifier = Modifier,
    numSliders: Int = 1,
    value: (Int) -> Float,
    onValueChange: (Int, Float) -> Unit
){
    BoxWithConstraints(modifier = modifier){
        val sliderWidth = this.maxWidth/numSliders
        val sliderHeight = this.maxHeight

        Row(modifier = Modifier) {
            for(sliderIndex in 0 until numSliders){
                VerticalValueSlider(
                    modifier = Modifier.size(sliderWidth, sliderHeight),
                    value = value(sliderIndex),
                    onValueChange = { onValueChange(sliderIndex, it) }
                )
            }
        }
    }
}

@Composable
fun VerticalValueSlider(
    modifier: Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f

){
    Column(
        modifier = modifier.border(width = 1.dp, color = Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ){
        VerticalSlider(
            modifier = Modifier
                .fillMaxHeight(0.9f)
                .fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            onValueChangeFinished = onValueChangeFinished
        )
        Text(
            text = (value * 100).toInt().toString(),
            color = Color.White
        )
    }
}

@Composable
fun VerticalSlider(
    modifier: Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
){
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        Slider(
            modifier = Modifier
                .requiredWidth(this.maxHeight)
                .requiredHeight(this.maxWidth)
                .rotate(-90f),
            value = value,
            valueRange = valueRange,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished
        )
    }
}