package com.example.synth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import com.example.synth.databinding.ActivityMainBinding

/** A full-screen PianoView activity. Also manages the AudioEngine */
class MainActivity : AppCompatActivity(), PianoKeyEventListener {
    private lateinit var bind: ActivityMainBinding
    private val audioEngine = AudioEngine()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Make the app fullscreen. TODO: works for now, but shouldn't use deprecated methods.
        this.supportActionBar?.hide()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        //Set up data binding with views
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.piano.pressedKeys.addPianoKeyListener(this)
        bind.currentOctave.text = bind.piano.octave.toString()
        audioEngine.start()
    }

    override fun onResume() {
        super.onResume()
        audioEngine.start()
    }

    override fun onPause() {
        super.onPause()
        audioEngine.stop()
    }

    override fun onKeyUpdatedEvent(pressedPianoKeys: Set<PianoKey>) {
        audioEngine.currentAudio = pressedPianoKeys.map { it.audio }.toSet()
    }

    fun octaveDown(view: View){
        bind.piano.octave = (bind.piano.octave - 1)
        bind.currentOctave.text = bind.piano.octave.toString()
    }

    fun octaveUp(view: View){
        bind.piano.octave = (bind.piano.octave + 1)
        bind.currentOctave.text = bind.piano.octave.toString()
    }

    fun noiseDown(view: View){
        bind.piano.noise -= 1
        bind.noiseLevel.text = bind.piano.noise.toString()

    }

    fun noiseUp(view: View){
        bind.piano.noise += 1
        bind.noiseLevel.text = bind.piano.noise.toString()
    }


}