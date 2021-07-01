package com.example.synth

import android.media.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.synth.databinding.ActivityMainBinding
import kotlin.math.PI
import kotlin.math.sin

class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding

    companion object{
        private val C_4 = makeSignal(Tone.C_4.freq, 3)
        private val D_4 = makeSignal(Tone.D_4.freq, 3)
        private val E_4 = makeSignal(Tone.E_4.freq, 3)
        private val F_4 = makeSignal(Tone.F_4.freq, 3)
        private val G_4 = makeSignal(Tone.G_4.freq, 3)
        private val A_4 = makeSignal(Tone.A_4.freq, 3)
        private val B_4 = makeSignal(Tone.B_4.freq, 3)
        private val C_5 = makeSignal(Tone.C_5.freq, 3)


        private fun makeSignal(freq: Int, seconds: Int, sampleRate: Int = 44100): ByteArray{
            val numSamples  = seconds * sampleRate
            val interval    = sampleRate.toDouble() / freq
            val bytes       = ByteArray(numSamples)

            for (i in 0 until numSamples){
                val angle = 2.0* PI * i / interval
                bytes[i] = (sin(angle) * 127).toInt().toByte()
            }
            return bytes
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
    }

    fun playButton(view: View){
        when (view.tag) {
            bind.C4.tag -> play(C_4)
            bind.D4.tag -> play(D_4)
            bind.E4.tag -> play(E_4)
            bind.F4.tag -> play(F_4)
            bind.G4.tag -> play(G_4)
            bind.A4.tag -> play(A_4)
            bind.B4.tag -> play(B_4)
            bind.C5.tag -> play(C_5)
        }
    }

    private fun play(signal: ByteArray){
        val audio = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
            .setAudioFormat(AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(44100)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build())
            .setBufferSizeInBytes(signal.size)
            .build()

        audio.write(signal, 0, signal.size)
        audio.play()

    }
}