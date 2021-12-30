package com.example.synth

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.signallib.amplitudeToVolume
import com.example.signallib.enums.Note
import com.example.signallib.volumeToAmplitude


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

                    // loop through each finger/pointer
                    for (i in 0 until it.pointerCount) {

                        // find the note/key the finger is touching
                        val note = with(density) {
                            viewModel.pianoGrid.findKeyAt(
                                it
                                    .getX(i)
                                    .toDp(),
                                it
                                    .getY(i)
                                    .toDp()
                            )
                        }

                        // add or remove note from list
                        if (note != null) {
                            if (i == it.actionIndex) {
                                when (it.actionMasked) {
                                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE
                                    -> newPressedNotes.add(note)

                                    MotionEvent.ACTION_UP
                                    -> newPressedNotes.remove(note)
                                }
                            } else {
                                newPressedNotes.add(note)
                            }
                        }
                    }

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
            if(viewModel.width != this.maxWidth
                || viewModel.height != this.maxHeight
            ){
                viewModel.width = this.maxWidth
                viewModel.height = this.maxHeight
                viewModel.pianoGrid.recalculateWidths()
            }
        }

        Column(Modifier.fillMaxSize()) {
            for(row in listOf(viewModel.pianoGrid.topRow, viewModel.pianoGrid.bottomRow) ){
                Row(
                    modifier = Modifier
                        .size(viewModel.width, viewModel.height/2),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for ((note, width) in row){
                        Box(
                            modifier = Modifier
                                .size(width, viewModel.height / 2)
                                .background(color = note.color(note in viewModel.pressedNotes))
                        )
                    }
                }
            }
        }
        Row(Modifier.fillMaxSize()) {
            for((_, width) in viewModel.pianoGrid.bottomRow){
                Box(
                    Modifier
                        .size(width, viewModel.height)
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
    Box(modifier = modifier, contentAlignment = Alignment.Center){
        Canvas(modifier = Modifier
            .fillMaxHeight(0.9f)
            .fillMaxWidth()) {
            for(i in 0..data.size-2){
                drawLine(
                    start = Offset(
                        x = i * size.width / (data.size-1),
                        y = (data[i] * -1 * size.height/2) + (size.height/2)
                    ),
                    end = Offset(
                        x = (i+1) * size.width / (data.size-1),
                        y = (data[i+1] * -1 * size.height/2) + (size.height/2)
                    ),
                    color = color,
                    strokeWidth = strokeWidth
                )
            }
        }
    }
}

@Composable
fun WaveShapeSelector(
    modifier: Modifier,
    viewModel: WaveShapeSelectorViewModel
){
    Button(
        modifier = modifier,
        onClick = { viewModel.nextWaveShape() }
    ) {
        Text(text = viewModel.waveShapeName)
    }
}

@Composable
fun PitchBend(
    modifier: Modifier = Modifier,
){
    var sliderState by remember { mutableStateOf( 0.0f) }
    VerticalSlider(
        modifier = modifier,
        value = sliderState,
        valueRange = -1f..1f,
        onValueChange = {
            sliderState = it
            AppModel.signalEngine.updatePitchBend(it)
        },
        onValueChangeFinished = {
            sliderState = 0f
            AppModel.signalEngine.updatePitchBend(0f) //snap back to 0f
        },
    )
}

@Composable
fun VolumeSlider(
    modifier: Modifier = Modifier,
    onVolumeChange: (Float) -> Unit = {}
){
    var sliderState by remember { mutableStateOf( 0.0f) }
    LabeledVerticalSlider(
        modifier = modifier,
        value = sliderState,
        onValueChange = {
            sliderState = it
            onVolumeChange.invoke(it)
        },
        showValue = true
    )
}


@Composable
fun HarmonicSeriesEditor(
    modifier: Modifier = Modifier,
    viewModel: HarmonicSeriesViewModel
){
    val harmonicSeries = viewModel.signalSettings.harmonicSeries
    Row(modifier){
        RowOfVerticalSliders(
            modifier = Modifier.fillMaxWidth(0.9f),
            numSliders = viewModel.signalSettings.harmonicSeries.numHarmonics,
            value = { sliderIndex -> viewModel.sliderState[sliderIndex] },
            onValueChange = { sliderIndex, sliderValue ->
                var newSliderValue = if(sliderValue < 0.01f) 0f else sliderValue //snaps slider to 0
                viewModel.harmonicSeriesUpdateQueue.offer(sliderIndex to newSliderValue)
                viewModel.sliderState[sliderIndex] = newSliderValue
            },
            label = { index -> if(index == 0) "f" else "${index+1}" },
            showValue = false
        )
        Column(
            Modifier
                .fillMaxSize()
                .border(1.dp, Color.White)) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f),
                onClick = { viewModel.reset() }
            ) {
                Text(
                    text = "RST",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
            Button(
                modifier = Modifier.fillMaxSize(),
                onClick = { viewModel.random() }
            ) {
                Text(
                    text = "RNDM",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun RowOfVerticalSliders(
    modifier: Modifier = Modifier,
    numSliders: Int = 1,
    value: (Int) -> Float,
    onValueChange: (Int, Float) -> Unit,
    label: (Int) -> String,
    showValue: Boolean
){
    BoxWithConstraints(modifier = modifier){
        val sliderWidth = this.maxWidth/numSliders
        val sliderHeight = this.maxHeight

        Row(modifier = Modifier) {
            for(sliderIndex in 0 until numSliders){
                LabeledVerticalSlider(
                    modifier = Modifier.size(sliderWidth, sliderHeight),
                    value = value(sliderIndex),
                    onValueChange = { onValueChange(sliderIndex, it) },
                    label = label(sliderIndex),
                    showValue = showValue
                )
            }
        }
    }
}

@Composable
fun LabeledVerticalSlider(
    modifier: Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    label: String = "",
    showValue: Boolean = true
){
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ){
        val showLabel = label != ""
        VerticalSlider(
            modifier = Modifier
                .fillMaxHeight(
                    when {
                        showLabel && showValue -> 0.75f
                        showLabel xor showValue -> 0.85f
                        else -> 1f
                    }
                )
                .fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            onValueChangeFinished = onValueChangeFinished
        )
        if(showValue){
            Box(
                modifier = Modifier
                    .fillMaxHeight(if (showLabel) 0.5f else 1f)
                    .fillMaxWidth()
                    .border(1.dp, Color.White),
                contentAlignment = Alignment.Center
            ){
                Text(
                    modifier = Modifier.fillMaxSize(),
                    text = (value * 100).toInt().toString(),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
        if(showLabel){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Color.White),
                contentAlignment = Alignment.Center
            ){
                Text(
                    modifier = Modifier.fillMaxSize(),
                    text = label,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun VerticalSlider(
    modifier: Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
){
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        Slider(
            modifier = Modifier
                .requiredWidth(maxHeight)
                .requiredHeight(maxWidth)
                .rotate(-90f),
            value = value,
            valueRange = valueRange,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished
        )


    }
}