package com.example.synth.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.synth.plus

@Composable
fun ToggleButton(
    modifier: Modifier,
    toggled: Boolean,
    onToggle: (Boolean) -> Unit,
    color: Color,
    offOverlay: Color = Color.Black,
    content: @Composable () -> Unit,
){
    Box(
        modifier = modifier
            .background(if (toggled) color else color + offOverlay)
            .clickable { onToggle(!toggled) },
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
        onToggle = { toggleState = it },
        color = Color.White,
    ) {
        Text(text = if (toggleState) "ON" else "OFF")
    }
}