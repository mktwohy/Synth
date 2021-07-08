package com.example.synth

import android.media.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import com.example.synth.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding

    companion object{
        const val SAMPLE_RATE = 44100
        const val BUFFER_DURATION = 20 //ms
        const val BUFFER_SIZE = SAMPLE_RATE * BUFFER_DURATION / 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.supportActionBar?.hide()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        Log.d("m_debug","created")
        mainLoop()
    }

    private fun mainLoop(){
        val at = AudioTrack.Builder()
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


        at.play()
        Thread {
            while (true) {
                val pk = bind.piano.pressedKeys.map { it.signal }.sum().pcmData
                at.write(pk, 0, pk.size)
                bind.piano.postInvalidate()
            }
        }.start()
    }
}