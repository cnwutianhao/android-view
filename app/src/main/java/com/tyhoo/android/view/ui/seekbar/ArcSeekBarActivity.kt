package com.tyhoo.android.view.ui.seekbar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tyhoo.android.view.R
import com.tyhoo.android.view.widget.ArcSeekBar

class ArcSeekBarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arc_seek_bar)

        val arcSeekBar = findViewById<ArcSeekBar>(R.id.arc_seek_bar)
        arcSeekBar.setOnProgressChangeListener(object : ArcSeekBar.OnProgressChangeListener {
            override fun onProgressChanged(seekBar: ArcSeekBar?, progress: Int, isUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: ArcSeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: ArcSeekBar?) {
            }
        })
    }
}