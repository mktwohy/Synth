package com.example.synth

import android.media.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.synth.databinding.ActivityMainBinding
import kotlin.math.PI
import kotlin.math.sin

class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding



    companion object{
        private val C_4_SIGNAL = SinSignal(Tone.C_4.freq)
        private val D_4_SIGNAL = SinSignal(Tone.D_4.freq)
        private val E_4_SIGNAL = SinSignal(Tone.E_4.freq)
        private val F_4_SIGNAL = SinSignal(Tone.F_4.freq)
        private val G_4_SIGNAL = SinSignal(Tone.G_4.freq)
        private val A_4_SIGNAL = SinSignal(Tone.A_4.freq)
        private val B_4_SIGNAL = SinSignal(Tone.B_4.freq)
        private val C_5_SIGNAL = SinSignal(Tone.C_5.freq)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
        Log.d("Mike","created")

    }

    fun playButton(view: View){
        when (view.tag) {
            bind.C4.tag -> C_4_SIGNAL.play()
            bind.D4.tag -> D_4_SIGNAL.play()
            bind.E4.tag -> E_4_SIGNAL.play()
            bind.F4.tag -> F_4_SIGNAL.play()
            bind.G4.tag -> G_4_SIGNAL.play()
            bind.A4.tag -> A_4_SIGNAL.play()
            bind.B4.tag -> B_4_SIGNAL.play()
            bind.C5.tag -> C_5_SIGNAL.play()
        }
    }


}