package com.tyhoo.android.view.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class RingProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mBackPaint: Paint = Paint()
    private val mFrontPaint: Paint = Paint()
    private val mTextPaint: Paint = Paint()
    private val mStrokeWidth = 50f
    private val mHalfStrokeWidth = mStrokeWidth / 2
    private val mRadius = 200f
    private var mRect: RectF? = null
    private var mProgress = 0
    private val mMax = 100
    private var mWidth = 0
    private var mHeight = 0

    init {
        mBackPaint.color = Color.WHITE
        mBackPaint.isAntiAlias = true
        mBackPaint.style = Paint.Style.STROKE
        mBackPaint.strokeWidth = mStrokeWidth

        mFrontPaint.color = Color.RED
        mFrontPaint.isAntiAlias = true
        mFrontPaint.style = Paint.Style.STROKE
        mFrontPaint.strokeWidth = mStrokeWidth

        mTextPaint.color = Color.MAGENTA
        mTextPaint.isAntiAlias = true
        mTextPaint.textSize = 80F
        mTextPaint.textAlign = Paint.Align.CENTER
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = getRealSize(widthMeasureSpec)
        mHeight = getRealSize(heightMeasureSpec)
        setMeasuredDimension(mWidth, mHeight)
    }

    override fun onDraw(canvas: Canvas) {
        initRect()
        val angle = mProgress.toFloat() / mMax * 360
        canvas.drawCircle(mWidth / 2.toFloat(), mHeight / 2.toFloat(), mRadius, mBackPaint)
        canvas.drawArc(mRect!!, -90F, angle, false, mFrontPaint)
        canvas.drawText(
            "$mProgress%",
            mWidth / 2 + mHalfStrokeWidth,
            mHeight / 2 + mHalfStrokeWidth,
            mTextPaint
        )
    }

    private fun getRealSize(measureSpec: Int): Int {
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)

        val result = if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.UNSPECIFIED) {
            (mRadius * 2 + mStrokeWidth).toInt()
        } else {
            size
        }

        return result
    }

    private fun initRect() {
        if (mRect == null) {
            val viewSize = (mRadius * 2).toInt()
            val left = (mWidth - viewSize) / 2
            val top = (mHeight - viewSize) / 2
            val right = left + viewSize
            val bottom = top + viewSize
            mRect = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
        }
    }

    fun setProgress(progress: Int) {
        mProgress = progress
        invalidate()
    }
}
