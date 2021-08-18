package com.example.synth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*


/** A full-screen PianoView activity. Also manages the AudioEngine */
class MainActivity_OLD : AppCompatActivity(), PianoKeyEventListener {
//    private lateinit var bind: ActivityMainBinding
    private val audioEngine = AudioEngine()
    private val noteToSignal = mutableMapOf<Note, Signal>()
    private val overtones = mutableMapOf<Int, Float>()
    private var overtoneRange = 1
        set(value){
            if(value in 1..24){
//                bind.overtoneRange.text = value.toString()
                overtones.clear()
                overtones.putAll(
                    Signal.harmonicSeries(1, value, 0.45f, 0f)
                    { i -> Signal.fundamental(i) || Signal.all(i) }
                )
                assignNotesToSignals()
                field = value
//                onKeyUpdatedEvent(bind.piano.pressedKeys.pianoKeys)
            }
        }
    private var octave = 4
        set(value){
            if(value in 0..8){
//                bind.piano.octave = value
//                bind.currentOctave.text = value.toString()
                field = value
                assignNotesToSignals()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Make the app fullscreen. TODO: works for now, but shouldn't use deprecated methods.
        this.supportActionBar?.hide()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        //Set up data binding with views
//        bind = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(bind.root)

//        bind.piano.pressedKeys.addPianoKeyListener(this)
//        bind.currentOctave.text = bind.piano.octave.toString()
        overtoneRange = 1
        assignNotesToSignals()
        audioEngine.start()
    }

    private fun assignNotesToSignals(){
        noteToSignal.clear()
//        Note.toList(octave).forEach { note ->
//            noteToSignal[note] = SumSignal(
//                Signal.signalsFromHarmonicSeries(
//                    overtones, note,
//                    Signal.sine
//                ),
//                1/8f
//            )
//        }
    }

    fun increaseOvertoneRange(view: View){ overtoneRange++ }
    fun decreaseOvertoneRange(view: View){ overtoneRange-- }

    fun octaveUp(view: View){ octave++ }
    fun octaveDown(view: View){ octave-- }

    fun updatePlot(buffer: FloatArray){
//        buffer.forEachIndexed{ i, value ->
//            bind.plot.buffer[i] = value
//        }
//        bind.plot.buffer = buffer
//        bind.plot.postInvalidate()
    }

    override fun onResume() {
        super.onResume()
        audioEngine.start()
    }

    override fun onPause() {
        super.onPause()
        audioEngine.stop()
    }

    override fun onKeyUpdatedEvent(pressedPianoKeys: Set<Note>) {
        audioEngine.signalBuffer
//            .also{
//                Log.d("m_signalBuffer","$pressedPianoKeys")
//            }
            .offer(
            if (pressedPianoKeys.isEmpty())
                setOf(SilentSignal)
            else
                pressedPianoKeys.map{ noteToSignal[it] ?: SilentSignal }.toSet()
        )
    }

}