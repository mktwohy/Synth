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
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.synth.Note.Companion.color
import com.example.synth.Note.Companion.minus
import com.example.synth.Note.Companion.plus
import kotlin.math.pow

fun log(text: String){ Log.d("m_tag",text) }

class PianoViewModel(
        notes: List<Note>
): ViewModel(){
    val notes = notes.toMutableStateList()
    val pressedNotes = mutableStateListOf<Note>()
    var width = mutableStateOf(0.dp)
    var height = mutableStateOf(0.dp)
    val pianoGridTop = mutableStateListOf<Pair<Note, Float>>()
    val pianoGridBottom = mutableStateListOf<Pair<Note, Float>>()
}

@ExperimentalComposeUiApi
@Composable
fun Piano(
        modifier: Modifier,
        viewModel: PianoViewModel
){
    BoxWithConstraints{
        val boxWidth = this.maxWidth
        Row(Modifier.fillMaxSize()) {
            viewModel.notes.filter { it.toString()[1] == '_' }.forEach { whiteNote ->
                PianoKey(
                    modifier = Modifier
                            .width(boxWidth/7)
                            .fillMaxHeight(),
                    whiteNote = whiteNote,
                    viewModel = viewModel
                )
            }
        }
    }
}



@ExperimentalComposeUiApi
@Composable
fun PianoKey(
        modifier: Modifier,
        whiteNote: Note,
        viewModel: PianoViewModel
){
    val topRowNotes = when(whiteNote.toString()[0]){
        'C'  -> listOf(whiteNote to 3/4f, (whiteNote+1) to 1/2f)
        'D'  -> listOf((whiteNote-1) to 1/4f, whiteNote to 1/2f, (whiteNote+1) to 1/4f)
        'E'  -> listOf((whiteNote-1) to 1/4f, whiteNote to 3/4f)
        'F'  -> listOf(whiteNote to 3/4f, (whiteNote+1) to 1/4f)
        'G'  -> listOf((whiteNote-1) to 1/4f, whiteNote to 2/4f, (whiteNote+1) to 1/4f)
        'A'  -> listOf((whiteNote-1) to 1/4f, whiteNote to 2/4f, (whiteNote+1) to 1/4f)
        'B'  -> listOf((whiteNote-1) to 1/4f, whiteNote to 3/4f)
        else -> listOf()
    }
    BoxWithConstraints(modifier = modifier.border(1.dp, Black)){
        val boxWidth = this.maxWidth
        Column(modifier = Modifier.fillMaxSize()) {
            Row(Modifier
                    .fillMaxHeight(0.5f)
                    .fillMaxWidth()
            ) {
                for((note, multiplier) in topRowNotes){
                    viewModel.pianoGridTop.add(note to multiplier)
                    Box(Modifier
                            .fillMaxHeight()
                            .width(boxWidth * multiplier)
                            .background(note.color(note in viewModel.pressedNotes))
                            .pointerInteropFilter {
                                when(it.actionMasked){
                                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE
                                    -> if(note !in viewModel.pressedNotes)
                                        viewModel.pressedNotes.add(note)
                                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE
                                    -> viewModel.pressedNotes.remove(note)
                                }
                                true
                            }
                    )
                }
            }
            Box(Modifier
                    .fillMaxSize()
                    .background(whiteNote.color(whiteNote in viewModel.pressedNotes))
                    .pointerInteropFilter {
                        when(it.actionMasked){
                            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE
                            -> if(whiteNote !in viewModel.pressedNotes)
                                viewModel.pressedNotes.add(whiteNote)
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE
                            -> viewModel.pressedNotes.remove(whiteNote)
                        }
                        true
                    }
            ){
                viewModel.pianoGridTop.add(whiteNote to 1/7f)
            }
        }
    }
}

@Composable
fun XYPlot(
    modifier: Modifier = Modifier,
    data: FloatArray,
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

class HarmonicSignalViewModel(
    signal: HarmonicSignal,
    buffer: FloatArray
) : ViewModel(){
    val signal: MutableState<HarmonicSignal> = mutableStateOf(signal)
    var plotBuffer: MutableState<FloatArray> = mutableStateOf(buffer.copyOf())
    var bendAmount: MutableState<Float> = mutableStateOf(1f)
    var volume: MutableState<Float> = mutableStateOf(1f)
    var harmonicSliders = mutableStateListOf<Float>().apply {
        repeat(Constants.NUM_HARMONICS){ this.add(0f) }
    }
}

@Composable
fun HarmonicSignalEditor(
    modifier: Modifier = Modifier,
    viewModel: HarmonicSignalViewModel
){
    Column(modifier) {
        RowOfVolumeSliders(
            modifier = Modifier.fillMaxHeight(0.50f),
            numSliders = Constants.NUM_HARMONICS,
            value = { sliderIndex -> viewModel.harmonicSliders[sliderIndex] },
            onValueChange = { sliderIndex, sliderValue ->
                val newSliderValue = if(sliderValue < 0.01f) 0f else sliderValue
                viewModel.harmonicSliders[sliderIndex] = newSliderValue
                viewModel.signal.value.harmonicSeries[sliderIndex+1] = newSliderValue.pow(3)
            }
        )
        Row(Modifier.border(1.dp, Color.White),) {
            XYPlot(
                modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.8f)
                        .background(Color.Black)
                        .border(1.dp, Color.White),
                color = Color(0.4f, 0.0f, 1f, 1f),
                strokeWidth = 5f,
                data = viewModel.plotBuffer.value,
            )
            VerticalSlider(
                modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f),
                value = viewModel.bendAmount.value,
                valueRange = 0.5f..1.5f,
                onValueChange = {
                    viewModel.bendAmount.value = it
                    viewModel.signal.value.bend(it)
                },
                onValueChangeFinished = {
                    viewModel.signal.value.bend(1f)
                    viewModel.bendAmount.value = 1f
                }
            )
            Column {
                VerticalSlider(
                    modifier = Modifier
                            .fillMaxHeight(0.8f)
                            .fillMaxWidth(),
                    value = viewModel.volume.value,
                    onValueChange = {
                        viewModel.volume.value = it
                        viewModel.signal.value.amp = it.pow(3)
                    }
                )
                Button(
                    modifier = Modifier.fillMaxSize(),
                    onClick = {
                        viewModel.signal.value.reset()
                        viewModel.signal.value.harmonicSeries.reset()
                        for(i in viewModel.harmonicSliders.indices){
                            viewModel.harmonicSliders[i] = 0f
                        }
                        for(i in viewModel.plotBuffer.value.indices){
                            viewModel.plotBuffer.value[i] = 0f
                        }
                    }
                ) { Text("Reset") }
            }
        }
    }
}



@Composable
fun RowOfVolumeSliders(
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
fun RowOfVolumeSlidersScreen(
    modifier: Modifier = Modifier,
    amplitudes: List<Float>,
    numSliders: Int,
){
    var amplitudeState = remember { mutableStateMapOf<Int,Float>() }

    RowOfVolumeSliders(
        modifier = Modifier.fillMaxHeight(0.9f),
        numSliders = numSliders,
        value = { sliderIndex -> amplitudeState[sliderIndex] ?: 0f },
        onValueChange = { sliderIndex, value -> amplitudeState[sliderIndex] = value }
    )
}

@Composable
fun VerticalValueSliderScreen(
    modifier: Modifier = Modifier,
    initialValue: Float = 0f,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
){
    var amplitude by remember { mutableStateOf(initialValue) }

    VerticalValueSlider(
        modifier = modifier,
        value = amplitude,
        valueRange = valueRange,
        onValueChange = {
            amplitude = it
            onValueChange(it)
        },
        onValueChangeFinished = onValueChangeFinished
    )
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