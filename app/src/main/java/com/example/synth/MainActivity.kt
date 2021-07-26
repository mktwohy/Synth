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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("m_activityLifecycle","create")

        //Make the app fullscreen.
        // TODO: works for now, but shouldn't use deprecated methods.
        this.supportActionBar?.hide()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        //Set up data binding with views
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.currentOctave.text = bind.piano.octave.toString()
        bind.piano.audioEngine.start()
    }

    fun octaveDown(view: View){
        bind.piano.changeOctave(bind.piano.octave - 1)
        bind.currentOctave.text = bind.piano.octave.toString()
    }

    fun octaveUp(view: View){
        bind.piano.changeOctave(bind.piano.octave + 1)
        bind.currentOctave.text = bind.piano.octave.toString()
    }

    fun noiseDown(view: View){
        with(bind.piano.audioEngine){
            if(this.noiseAmount > 0) {
                this.noiseAmount -= 1
                bind.noiseLevel.text = this.noiseAmount.toString()
            }
        }
    }

    fun noiseUp(view: View){
        with(bind.piano.audioEngine){
            this.noiseAmount += 1
            bind.noiseLevel.text = this.noiseAmount.toString()

        }
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