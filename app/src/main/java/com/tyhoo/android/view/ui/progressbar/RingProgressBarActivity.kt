package com.tyhoo.android.view.ui.progressbar

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.tyhoo.android.view.R
import com.tyhoo.android.view.widget.RingProgressBar

class RingProgressBarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ring_progress_bar)

        var progress = 0
        findViewById<Button>(R.id.btn_ring).setOnClickListener {
            progress++
            val ringProgressBar = findViewById<RingProgressBar>(R.id.ring_progress_bar)
            ringProgressBar.setProgress(progress)
        }
    }
}