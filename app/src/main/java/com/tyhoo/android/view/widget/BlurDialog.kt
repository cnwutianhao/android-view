package com.tyhoo.android.view.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog

class BlurDialog(
    context: Context,
    private val bitmap: Bitmap,
    private val layoutResId: Int,
    themeResId: Int
) : AlertDialog(context, themeResId) {

    private var shouldDismissOnOutsideClick = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.let {
            it.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            it.setBackgroundDrawable(BitmapDrawable(context.resources, bitmap))
        }

        val rootView = layoutInflater.inflate(layoutResId, null) as ViewGroup
        setContentView(rootView)

        rootView.setOnClickListener {
            if (shouldDismissOnOutsideClick) {
                dismiss()
            }
        }

        val layoutParams = WindowManager.LayoutParams()
        window?.let {
            layoutParams.copyFrom(it.attributes)
            layoutParams.gravity = Gravity.CENTER
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
            it.attributes = layoutParams
        }
    }

    fun setDismissOnOutsideClick(dismissOnOutsideClick: Boolean) {
        shouldDismissOnOutsideClick = dismissOnOutsideClick
    }
}