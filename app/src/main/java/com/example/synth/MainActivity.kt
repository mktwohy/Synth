package com.example.synth

import android.media.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.synth.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding
    private val buffer = mutableSetOf<Signal>()
    private val audioTracks = mutableListOf<AudioTrack>()


    companion object{
        const val SAMPLE_RATE = 44100
        const val BUFFER_DURATION = 20 //ms
        const val BUFFER_SIZE = SAMPLE_RATE * BUFFER_DURATION / 1000

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
        Log.d("m_debug","created")
        mainLoop()
    }

    fun playButton(view: View){
        when (view.tag) {
            bind.C4.tag -> buffer.add(C_4_SIGNAL)
            bind.D4.tag -> buffer.add(D_4_SIGNAL)
            bind.E4.tag -> buffer.add(E_4_SIGNAL)
            bind.F4.tag -> buffer.add(F_4_SIGNAL)
            bind.G4.tag -> buffer.add(G_4_SIGNAL)
            bind.A4.tag -> buffer.add(A_4_SIGNAL)
            bind.B4.tag -> buffer.add(B_4_SIGNAL)
            bind.C5.tag -> buffer.add(C_5_SIGNAL)
            bind.RandomNote.tag -> buffer.add(SinSignal(Tone.values().slice(60..80).random().freq))
            bind.Chord.tag -> listOf(C_4_SIGNAL, E_4_SIGNAL, G_4_SIGNAL).forEach{buffer.add(it)}
            bind.TwoNotes.tag -> listOf(C_4_SIGNAL, E_4_SIGNAL).forEach{buffer.add(it)}
        }
    }

    private fun clearAudioTrackMemory(){
        while(audioTracks.size > 5){
            audioTracks.removeFirst().release()
        }
        Log.d("m_debug","Audio CLEARED")
    }

    private fun playAudioInBuffer(){
        val signalSum = buffer.toList().sum()
        Log.d("m_debug", "${buffer.size} Notes: $signalSum")
        audioTracks.add(signalSum.play()) //play signal and save in queue to be removed from memory
        buffer.clear() //clear buffer since it's been used now
    }

    private fun mainLoop(){
        Thread {
            while (true) {
                if(audioTracks.size > 20) clearAudioTrackMemory()
                if(buffer.isNotEmpty()) playAudioInBuffer()
                Thread.sleep(BUFFER_DURATION.toLong())
            }
        }.start()
    }
}