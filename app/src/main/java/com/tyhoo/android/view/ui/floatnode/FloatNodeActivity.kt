package com.tyhoo.android.view.ui.floatnode

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.tyhoo.android.view.R
import com.tyhoo.android.view.widget.FloatNoteView

class FloatNodeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_float_node)

        val viewGroup = window.decorView as ViewGroup
        findViewById<Button>(R.id.btn_node).setOnClickListener {
            val floatNoteView = FloatNoteView(this)
            viewGroup.addView(floatNoteView)
            floatNoteView.addNote()
        }
    }
}