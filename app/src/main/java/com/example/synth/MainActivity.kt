package com.example.synth

import android.media.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import com.example.synth.databinding.ActivityMainBinding
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding

    companion object{
        const val SAMPLE_RATE = 44100
        const val BUFFER_SIZE = 512
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.supportActionBar?.hide()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        mainLoop()
    }

    private fun mainLoop(){
        val audioTrack = AudioTrack.Builder()
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
            .apply { play() }

        Thread {
            while (true) {
                val pcm = bind.piano.pcmOutput.getNextChunk(BUFFER_SIZE)
                with(pcm.toList()){
                    Log.d("m_pcm", "$size: $this")
                }
                audioTrack.write(pcm, 0, pcm.size)
                bind.piano.postInvalidate()
            }
        }.start()
    }
}