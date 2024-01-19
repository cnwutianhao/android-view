package com.tyhoo.android.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.tyhoo.android.view.ui.floatnode.FloatNodeActivity
import com.tyhoo.android.view.ui.progressbar.RingProgressBarActivity
import com.tyhoo.android.view.ui.skeleton.SkeletonActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_skeleton).setOnClickListener {
            val intent = Intent(this, SkeletonActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn_node).setOnClickListener {
            val intent = Intent(this, FloatNodeActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn_ring_progress_bar).setOnClickListener {
            val intent = Intent(this, RingProgressBarActivity::class.java)
            startActivity(intent)
        }
    }
}