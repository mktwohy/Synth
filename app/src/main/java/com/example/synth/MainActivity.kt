package com.example.synth

import android.media.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import com.example.synth.databinding.ActivityMainBinding

/**
 * Defines constants SAMPLE_RATE and BUFFER_SIZE. It is also responsible for running the main loop,
 * which reads the current pcmOutput of the PianoView and plays it with AudioTrack
 */
class MainActivity : AppCompatActivity() {
    lateinit var bind: ActivityMainBinding

    companion object{
        const val SAMPLE_RATE = 44100
        const val BUFFER_SIZE = 512
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.supportActionBar?.hide()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.piano.audioEngine.start()
    }

    override fun onResume() {
        super.onResume()
        bind.piano.audioEngine.start()
    }

    override fun onPause() {
        super.onPause()
        bind.piano.audioEngine.stop()
    }


}