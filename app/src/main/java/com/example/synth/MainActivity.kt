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
    private lateinit var bind: ActivityMainBinding
    private var runMainLoop = false
    private val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build())
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

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

        runMainLoop = true
        Thread { mainLoop() }.start()

    }

    override fun onResume() {
        super.onResume()
        if (!runMainLoop){
            runMainLoop = true
            mainLoop()
        }
    }

    override fun onPause() {
        super.onPause()
        runMainLoop = false
    }

    private fun mainLoop(){
            audioTrack.play()
            var pcm: ShortArray
            while (runMainLoop) {
                pcm = bind.piano.pcmOutput.nextChunk(BUFFER_SIZE)
                Log.d("m_pcm", pcm.toList().toString())
                audioTrack.write(pcm, 0, pcm.size)
                bind.piano.postInvalidate()
            }
            audioTrack.stop()
    }
}