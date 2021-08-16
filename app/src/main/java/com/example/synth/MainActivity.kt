package com.example.synth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VolumeSliderScreen(modifier = Modifier.fillMaxSize(0.5f))

//            Row {
//                repeat(5){
//                    VolumeSliderScreen(modifier = Modifier.fillMaxHeight().width(50.dp))
//                }
//
//            }

        }
    }
}
