package com.example.synth

import android.media.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import com.example.synth.databinding.ActivityMainBinding

/** A full-screen PianoView activity. Also manages the AudioEngine */
class MainActivity : AppCompatActivity() {
    lateinit var bind: ActivityMainBinding

    companion object{
        const val SAMPLE_RATE = 44100
        const val BUFFER_SIZE = 256
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Make the app fullscreen. TODO: works for now, but don't use deprecated methods.
        this.supportActionBar?.hide()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        //Set up data binding with views
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.octave.text = bind.piano.octave.toString()
        bind.piano.audioEngine.start()
    }

    fun octaveDown(view: View){
        bind.piano.changeOctave(bind.piano.octave - 1)
        bind.octave.text = bind.piano.octave.toString()
    }

    fun octaveUp(view: View){
        bind.piano.changeOctave(bind.piano.octave + 1)
        bind.octave.text = bind.piano.octave.toString()
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