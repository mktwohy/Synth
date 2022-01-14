package com.example.synth.ui.composables

import android.widget.ToggleButton
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonElevation
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.synth.plus

@Composable
fun ToggleButton(
    modifier: Modifier,
    toggled: Boolean,
    onToggle: (Boolean) -> Unit,
    onColor: Color,
    offColor: Color = onColor + Color.Black,
    content: @Composable () -> Unit,
){
    Box(
        modifier = modifier
            .background(if (toggled) onColor else offColor)
            .clickable { onToggle(toggled) },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun TestToggleButton(){
    var toggleState by remember{ mutableStateOf(true) }

    ToggleButton(
        modifier = Modifier.fillMaxSize(0.5f),
        toggled = toggleState,
        onToggle = { toggleState = !toggleState },
        onColor = Color.White,
    ) {
        Text(text = if (toggleState) "ON" else "OFF")
    }
}