package com.tyhoo.android.view.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ComposeShader
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.Shader
import android.util.AttributeSet
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.tyhoo.android.view.R
import kotlin.math.cos
import kotlin.math.sin

class ShimmerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    companion object {
        private const val DEFAULT_ANIMATION_DURATION = 1500
        private const val DEFAULT_ANGLE = 20
        private const val MIN_ANGLE_VALUE = -45
        private const val MAX_ANGLE_VALUE = 45
        private const val MIN_MASK_WIDTH_VALUE = 0
        private const val MAX_MASK_WIDTH_VALUE = 1
        private const val MIN_GRADIENT_CENTER_COLOR_WIDTH_VALUE = 0
        private const val MAX_GRADIENT_CENTER_COLOR_WIDTH_VALUE = 1
    }

    private var maskOffsetX: Int = 0
    private var maskRect: Rect? = null
    private var gradientTexturePaint: Paint? = null
    private var maskAnimator: ValueAnimator? = null

    private var localMaskBitmap: Bitmap? = null
    private var maskBitmap: Bitmap? = null
    private var canvasForShimmerMask: Canvas? = null

    private var isAnimationReversed: Boolean = false
    private var isAnimationStarted: Boolean = false
    private var autoStart: Boolean = false
    private var shimmerAnimationDuration: Int = 0
    private var shimmerColor: Int = 0
    private var shimmerAngle: Int = 0
    private var maskWidth: Float = 0.5F
    private var gradientCenterColorWidth: Float = 0.1F

    private var startAnimationPreDrawListener: ViewTreeObserver.OnPreDrawListener? = null

    init {
        setWillNotDraw(false)
        val a: TypedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.ShimmerLayout, 0, 0
        )

        try {
            shimmerAngle = a.getInteger(R.styleable.ShimmerLayout_shimmer_angle, DEFAULT_ANGLE)
            shimmerAnimationDuration = a.getInteger(
                R.styleable.ShimmerLayout_shimmer_animation_duration,
                DEFAULT_ANIMATION_DURATION
            )
            shimmerColor = a.getColor(
                R.styleable.ShimmerLayout_shimmer_color,
                ContextCompat.getColor(context, R.color.skeleton_dark_transparent)
            )
            autoStart = a.getBoolean(R.styleable.ShimmerLayout_shimmer_auto_start, false)
            maskWidth = a.getFloat(R.styleable.ShimmerLayout_shimmer_mask_width, 0.5F)
            gradientCenterColorWidth = a.getFloat(
                R.styleable.ShimmerLayout_shimmer_gradient_center_color_width,
                0.1F
            )
            isAnimationReversed =
                a.getBoolean(R.styleable.ShimmerLayout_shimmer_reverse_animation, false)
        } finally {
            a.recycle()
        }

        setMaskWidth(maskWidth)
        setGradientCenterColorWidth(gradientCenterColorWidth)
        setShimmerAngle(shimmerAngle)

        if (autoStart && visibility == VISIBLE) {
            startShimmerAnimation()
        }
    }

    override fun onDetachedFromWindow() {
        resetShimmering()
        super.onDetachedFromWindow()
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (!isAnimationStarted || width <= 0 || height <= 0) {
            super.dispatchDraw(canvas)
        } else {
            dispatchDrawShimmer(canvas)
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == VISIBLE) {
            if (autoStart) {
                startShimmerAnimation()
            }
        } else {
            stopShimmerAnimation()
        }
    }

    fun startShimmerAnimation() {
        if (isAnimationStarted) {
            return
        }

        if (width == 0) {
            startAnimationPreDrawListener = ViewTreeObserver.OnPreDrawListener {
                viewTreeObserver.removeOnPreDrawListener(startAnimationPreDrawListener)
                startShimmerAnimation()
                true
            }

            viewTreeObserver.addOnPreDrawListener(startAnimationPreDrawListener)
            return
        }

        val animator: Animator = getShimmerAnimation()
        animator.start()
        isAnimationStarted = true
    }

    fun stopShimmerAnimation() {
        startAnimationPreDrawListener?.let {
            viewTreeObserver.removeOnPreDrawListener(it)
        }

        resetShimmering()
    }

    fun isAnimationStarted(): Boolean {
        return isAnimationStarted
    }

    fun setShimmerColor(shimmerColor: Int) {
        this.shimmerColor = shimmerColor
        resetIfStarted()
    }

    fun setShimmerAnimationDuration(durationMillis: Int) {
        shimmerAnimationDuration = durationMillis
        resetIfStarted()
    }

    fun setAnimationReversed(animationReversed: Boolean) {
        isAnimationReversed = animationReversed
        resetIfStarted()
    }

    /**
     * Set the angle of the shimmer effect in clockwise direction in degrees.
     * The angle must be between [MIN_ANGLE_VALUE] and [MAX_ANGLE_VALUE].
     *
     * @param angle The angle to be set
     */
    fun setShimmerAngle(angle: Int) {
        require(!(angle < MIN_ANGLE_VALUE || angle > MAX_ANGLE_VALUE)) {
            "shimmerAngle value must be between $MIN_ANGLE_VALUE and $MAX_ANGLE_VALUE"
        }
        shimmerAngle = angle
        resetIfStarted()
    }

    /**
     * Sets the width of the shimmer line to a value higher than 0 to less or equal to 1.
     * 1 means the width of the shimmer line is equal to half of the width of the ShimmerLayout.
     * The default value is 0.5.
     *
     * @param maskWidth The width of the shimmer line.
     */
    fun setMaskWidth(maskWidth: Float) {
        require(!(maskWidth <= MIN_MASK_WIDTH_VALUE || maskWidth > MAX_MASK_WIDTH_VALUE)) {
            "maskWidth value must be higher than $MIN_MASK_WIDTH_VALUE and less or equal to $MAX_MASK_WIDTH_VALUE"
        }
        this.maskWidth = maskWidth
        resetIfStarted()
    }

    /**
     * Sets the width of the center gradient color to a value higher than 0 to less than 1.
     * 0.99 means that the whole shimmer line will have this color with a little transparent edges.
     * The default value is 0.1.
     *
     * @param gradientCenterColorWidth The width of the center gradient color.
     */
    fun setGradientCenterColorWidth(gradientCenterColorWidth: Float) {
        require(
            !(gradientCenterColorWidth <= MIN_GRADIENT_CENTER_COLOR_WIDTH_VALUE
                    || gradientCenterColorWidth >= MAX_GRADIENT_CENTER_COLOR_WIDTH_VALUE)
        ) {
            "gradientCenterColorWidth value must be higher than $MIN_GRADIENT_CENTER_COLOR_WIDTH_VALUE and less than $MAX_GRADIENT_CENTER_COLOR_WIDTH_VALUE"
        }
        this.gradientCenterColorWidth = gradientCenterColorWidth
        resetIfStarted()
    }

    private fun resetIfStarted() {
        if (isAnimationStarted) {
            resetShimmering()
            startShimmerAnimation()
        }
    }

    private fun dispatchDrawShimmer(canvas: Canvas) {
        super.dispatchDraw(canvas)

        localMaskBitmap = getMaskBitmap()
        if (localMaskBitmap == null) {
            return
        }

        canvasForShimmerMask = canvasForShimmerMask ?: Canvas(localMaskBitmap!!)

        canvasForShimmerMask?.apply {
            drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            save()
            translate(-maskOffsetX.toFloat(), 0f)

            super.dispatchDraw(this)

            restore()
        }

        drawShimmer(canvas)

        localMaskBitmap = null
    }

    private fun drawShimmer(destinationCanvas: Canvas) {
        createShimmerPaint()

        destinationCanvas.save()

        destinationCanvas.translate(maskOffsetX.toFloat(), 0f)
        maskRect?.let {
            destinationCanvas.drawRect(
                it.left.toFloat(),
                0f,
                it.width().toFloat(),
                it.height().toFloat(),
                gradientTexturePaint!!
            )
        }

        destinationCanvas.restore()
    }

    private fun resetShimmering() {
        maskAnimator?.apply {
            end()
            removeAllUpdateListeners()
        }

        maskAnimator = null
        gradientTexturePaint = null
        isAnimationStarted = false

        releaseBitMaps()
    }

    private fun releaseBitMaps() {
        canvasForShimmerMask = null

        maskBitmap?.apply {
            recycle()
            maskBitmap = null
        }
    }

    private fun getMaskBitmap(): Bitmap? {
        if (maskBitmap == null) {
            maskBitmap = createBitmap(maskRect?.width() ?: 0, height)
        }

        return maskBitmap
    }

    private fun createShimmerPaint() {
        if (gradientTexturePaint != null) {
            return
        }

        val edgeColor = reduceColorAlphaValueToZero(shimmerColor)
        val shimmerLineWidth = width / 2 * maskWidth
        val yPosition = if (0 <= shimmerAngle) height.toFloat() else 0f

        val gradient = LinearGradient(
            0f,
            yPosition,
            (cos(Math.toRadians(shimmerAngle.toDouble())) * shimmerLineWidth).toFloat(),
            (yPosition + sin(Math.toRadians(shimmerAngle.toDouble())) * shimmerLineWidth).toFloat(),
            intArrayOf(edgeColor, shimmerColor, shimmerColor, edgeColor),
            getGradientColorDistribution(),
            Shader.TileMode.CLAMP
        )

        val maskBitmapShader = localMaskBitmap?.let {
            BitmapShader(it, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }

        val composeShader = maskBitmapShader?.let {
            ComposeShader(gradient, it, PorterDuff.Mode.DST_IN)
        }

        gradientTexturePaint = Paint().apply {
            isAntiAlias = true
            isDither = true
            isFilterBitmap = true
            shader = composeShader
        }
    }

    private fun getShimmerAnimation(): Animator {
        if (maskAnimator != null) {
            return maskAnimator!!
        }

        if (maskRect == null) {
            maskRect = calculateBitmapMaskRect()
        }

        val animationToX = width
        val animationFromX: Int

        animationFromX = if (width > maskRect!!.width()) {
            -animationToX
        } else {
            -maskRect!!.width()
        }

        val shimmerBitmapWidth = maskRect!!.width()
        val shimmerAnimationFullLength = animationToX - animationFromX

        maskAnimator = if (isAnimationReversed) {
            ValueAnimator.ofInt(shimmerAnimationFullLength, 0)
        } else {
            ValueAnimator.ofInt(0, shimmerAnimationFullLength)
        }

        maskAnimator?.apply {
            duration = shimmerAnimationDuration.toLong()
            repeatCount = ObjectAnimator.INFINITE

            addUpdateListener { animation ->
                maskOffsetX = animationFromX + animation.animatedValue as Int

                if (maskOffsetX + shimmerBitmapWidth >= 0) {
                    invalidate()
                }
            }
        }

        return maskAnimator!!
    }

    private fun createBitmap(width: Int, height: Int): Bitmap? {
        return try {
            Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
        } catch (e: OutOfMemoryError) {
            System.gc()
            null
        }
    }

    private fun reduceColorAlphaValueToZero(actualColor: Int): Int {
        return Color.argb(
            0,
            Color.red(actualColor),
            Color.green(actualColor),
            Color.blue(actualColor)
        )
    }

    private fun calculateBitmapMaskRect(): Rect {
        return Rect(0, 0, calculateMaskWidth(), height)
    }

    private fun calculateMaskWidth(): Int {
        val shimmerLineBottomWidth =
            (width / 2 * maskWidth) / Math.cos(Math.toRadians(Math.abs(shimmerAngle.toDouble())))
        val shimmerLineRemainingTopWidth =
            height * Math.tan(Math.toRadians(Math.abs(shimmerAngle.toDouble())))

        return (shimmerLineBottomWidth + shimmerLineRemainingTopWidth).toInt()
    }

    private fun getGradientColorDistribution(): FloatArray {
        val colorDistribution = FloatArray(4)

        colorDistribution[0] = 0f
        colorDistribution[3] = 1f

        colorDistribution[1] = 0.5F - gradientCenterColorWidth / 2F
        colorDistribution[2] = 0.5F + gradientCenterColorWidth / 2F

        return colorDistribution
    }
}
