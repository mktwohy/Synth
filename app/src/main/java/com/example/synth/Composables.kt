package com.example.synth

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.synth.Note.Companion.color
import com.example.synth.Note.Companion.minus
import com.example.synth.Note.Companion.plus
import kotlin.math.pow

fun log(text: String){ Log.d("m_tag",text) }

fun topRowNoteRatios(whiteNote: Note) =
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

class PianoGrid(
    val width: MutableState<Dp>,
    val height: MutableState<Dp>,
    var notes: List<Note>){
    val topRow = mutableListOf<Pair<Note, Dp>>()
    val bottomRow = mutableListOf<Pair<Note, Dp>>()

    fun recalculateWidths(){
        topRow.clear()
        bottomRow.clear()
        val whiteNotes = notes.filter { it.name[1] == '_' }
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
                log("$x in $cumSum..${cumSum+width}?")
                if (x in cumSum..cumSum+width) return note
                else cumSum += width
            }
            return null
        }
        return if (y < height.value / 2)
            searchRow(topRow)
        else
            searchRow(bottomRow)
    }
}

class PianoViewModel(
        notes: List<Note>
): ViewModel(){
    val notes = notes.toMutableStateList()
    val pressedNotes = mutableStateListOf<Note>()
    var width = mutableStateOf(0.dp)
    var height = mutableStateOf(0.dp)
    val pianoGrid = PianoGrid(width, height, notes)
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
                    with(density) {
                        log(viewModel.pianoGrid.findKeyAt(it.x.toDp(), it.y.toDp()).toString())
                    }
                    true
                }
            )
    ){
        if(viewModel.width.value != this.maxWidth
            || viewModel.height.value != this.maxHeight
        ){
            viewModel.width.value = this.maxWidth
            viewModel.height.value = this.maxHeight
            viewModel.pianoGrid.recalculateWidths()
            log(viewModel.pianoGrid.topRow.toString())
        }

        Column(Modifier.fillMaxSize()) {
            for(row in listOf(viewModel.pianoGrid.topRow, viewModel.pianoGrid.bottomRow) ){
                Row(Modifier.size(viewModel.width.value, viewModel.height.value/2)) {
                    for ((note, width) in row){
                        Box(
                            modifier = Modifier
                                .size(width, viewModel.height.value / 2)
                                .background(note.color(note in viewModel.pressedNotes))
//                                .border(1.dp, Black)
                        ){
                            Text(text = "$note", color = Blue)
                        }
                    }
                }
            }
        }

    }
}



//@Composable
//fun PianoKey(
//        modifier: Modifier,
//        whiteNote: Note,
//        viewModel: PianoViewModel
//){
//    val topRowNotes = when(whiteNote.toString()[0]){
//        'C'  -> listOf(whiteNote to 3/4f, (whiteNote+1) to 1/2f)
//        'D'  -> listOf((whiteNote-1) to 1/4f, whiteNote to 1/2f, (whiteNote+1) to 1/4f)
//        'E'  -> listOf((whiteNote-1) to 1/4f, whiteNote to 3/4f)
//        'F'  -> listOf(whiteNote to 3/4f, (whiteNote+1) to 1/4f)
//        'G'  -> listOf((whiteNote-1) to 1/4f, whiteNote to 2/4f, (whiteNote+1) to 1/4f)
//        'A'  -> listOf((whiteNote-1) to 1/4f, whiteNote to 2/4f, (whiteNote+1) to 1/4f)
//        'B'  -> listOf((whiteNote-1) to 1/4f, whiteNote to 3/4f)
//        else -> listOf()
//    }
//    BoxWithConstraints(modifier = modifier.border(1.dp, Black)){
//        val boxWidth = this.maxWidth
//        Column(modifier = Modifier.fillMaxSize()) {
//            Row(
//                Modifier
//                    .fillMaxHeight(0.5f)
//                    .fillMaxWidth()
//            ) {
//                for((note, multiplier) in topRowNotes){
//                    viewModel.pianoGridTop.add(note to multiplier)
//                    Box(
//                        Modifier
//                            .fillMaxHeight()
//                            .width(boxWidth * multiplier)
//                            .background(note.color(note in viewModel.pressedNotes))
//                    )
//                }
//            }
//            Box(
//                Modifier
//                    .fillMaxSize()
//                    .background(whiteNote.color(whiteNote in viewModel.pressedNotes))
//            ){
//                viewModel.pianoGridTop.add(whiteNote to 1/7f)
//            }
//        }
//    }
//}

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
    plotBuffer: FloatArray
) : ViewModel(){
    val signal: MutableState<HarmonicSignal> = mutableStateOf(signal)
    var plotBuffer: MutableState<FloatArray> = mutableStateOf(plotBuffer)
    var bendAmount: MutableState<Float> = mutableStateOf(1f)
    var volume: MutableState<Float> = mutableStateOf(1f)
    var harmonicSliders = mutableStateListOf<Float>().apply {
        repeat(Constants.NUM_HARMONICS){ this.add(0f) }
    }
}

@Composable
fun PitchBend(modifier: Modifier = Modifier, viewModel: HarmonicSignalViewModel){
    VerticalSlider(
        modifier = modifier,
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
}

@Composable
fun Volume(
    modifier: Modifier = Modifier,
    viewModel: HarmonicSignalViewModel
){
    Column(modifier = modifier) {
        VerticalSlider(
            modifier = Modifier
                .fillMaxHeight(0.8f)
                .fillMaxWidth(),
            value = viewModel.volume.value,
            onValueChange = {
                viewModel.volume.value = it
                viewModel.signal.value.amp = it.pow(2)
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

@Composable
fun HarmonicSeriesEditor(
    modifier: Modifier = Modifier,
    viewModel: HarmonicSignalViewModel
){
    RowOfVolumeSliders(
        modifier = modifier,
        numSliders = Constants.NUM_HARMONICS,
        value = { sliderIndex -> viewModel.harmonicSliders[sliderIndex] },
        onValueChange = { sliderIndex, sliderValue ->
            val newSliderValue = if(sliderValue < 0.01f) 0f else sliderValue
            viewModel.harmonicSliders[sliderIndex] = newSliderValue
            viewModel.signal.value.harmonicSeries[sliderIndex+1] = newSliderValue.pow(3)
        }
    )
}

@Composable
fun Main(
    modifier: Modifier = Modifier,
    viewModel: HarmonicSignalViewModel
){
    Column(modifier) {
        HarmonicSeriesEditor(
            modifier = modifier.fillMaxHeight(0.50f),
            viewModel = viewModel
        )
        Row(Modifier.border(1.dp, Color.White),) {
            XYPlot(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.8f)
                    .background(Black)
                    .border(1.dp, Color.White),
                color = Color(0.4f, 0.0f, 1f, 1f),
                strokeWidth = 5f,
                data = viewModel.plotBuffer.value,
            )
            PitchBend(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f),
                viewModel = viewModel
            )
            Volume(viewModel = viewModel)
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