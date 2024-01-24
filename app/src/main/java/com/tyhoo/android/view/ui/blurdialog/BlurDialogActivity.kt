package com.tyhoo.android.view.ui.blurdialog

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.tyhoo.android.view.R
import com.tyhoo.android.view.util.BlurUtil
import com.tyhoo.android.view.widget.BlurDialog

class BlurDialogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blur_dialog)

        findViewById<Button>(R.id.btn_blur_dialog).setOnClickListener {
            val dialog = BlurDialog(
                context = this,
                bitmap = BlurUtil.getBlurBackgroundDrawer(this),
                R.layout.dialog_blur,
                R.style.BlurDialogStyle
            )
            dialog.setDismissOnOutsideClick(true)
            dialog.show()

            val dialogView = dialog.findViewById<View>(R.id.dialog_blur_view)
            val button = dialogView?.findViewById<Button>(R.id.btn_test)
            button?.setOnClickListener {
                Log.d(TAG, "Blur Dialog 按钮点击")
            }
        }
    }

    companion object {
        private const val TAG = "Tyhoo"
    }
}