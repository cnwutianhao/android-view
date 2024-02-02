package com.tyhoo.android.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.graphics.Region
import android.graphics.SweepGradient
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import com.tyhoo.android.view.R
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * 圆弧形 SeekBar
 */
class ArcSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 可配置数据
    private var mArcColors: IntArray? = null            // Seek 颜色
    private var mArcWidth = 0f                          // Seek 宽度
    private var mOpenAngle = 0f                         // 开口的角度大小 0 - 360
    private var mRotateAngle = 0f                       // 旋转角度
    private var mBorderWidth = 0                        // 描边宽度
    private var mBorderColor = 0                        // 描边颜色
    private var mThumbColor = 0                         // 拖动按钮颜色
    private var mThumbWidth = 0f                        // 拖动按钮宽度
    private var mThumbRadius = 0f                       // 拖动按钮半径
    private var mThumbShadowRadius = 0f                 // 拖动按钮阴影半径
    private var mThumbShadowColor = 0                   // 拖动按钮阴影颜色
    private var mThumbMode = 0                          // 拖动按钮模式
    private var mShadowRadius = 0                       // 阴影半径
    private var mMaxValue = 0                           // 最大数值
    private var mMinValue = 0                           // 最小数值
    private var mCenterX = 0f                           // 圆弧 SeekBar 中心点 X
    private var mCenterY = 0f                           // 圆弧 SeekBar 中心点 Y
    private var mThumbX = 0f                            // 拖动按钮 中心点 X
    private var mThumbY = 0f                            // 拖动按钮 中心点 Y
    private var mSeekPath: Path? = null
    private var mBorderPath: Path? = null
    private var mArcPaint: Paint? = null
    private var mThumbPaint: Paint? = null
    private var mBorderPaint: Paint? = null
    private var mShadowPaint: Paint? = null
    private var mTempPos: FloatArray? = null
    private var mTempTan: FloatArray? = null
    private var mSeekPathMeasure: PathMeasure? = null
    private var mProgressPresent = 0f                   // 当前进度百分比
    private var mCanDrag = false                        // 是否允许拖动
    private val mAllowTouchSkip = false                 // 是否允许越过边界
    private var mDetector: GestureDetector? = null
    private var mInvertMatrix: Matrix? = null           // 逆向 Matrix, 用于计算触摸坐标和绘制坐标的转换
    private var mArcRegion: Region? = null              // ArcPath的实际区域大小,用于判定单击事件

    //--- 初始化 -----------------------------------------------------------------------------------
    // 初始化各种属性
    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ArcSeekBar)
        mArcColors = getArcColors(context, ta)
        mArcWidth = ta.getDimensionPixelSize(
            R.styleable.ArcSeekBar_arc_width, dp2px(DEFAULT_ARC_WIDTH)
        ).toFloat()
        mOpenAngle = ta.getFloat(R.styleable.ArcSeekBar_arc_open_angle, DEFAULT_OPEN_ANGLE)
        mRotateAngle = ta.getFloat(R.styleable.ArcSeekBar_arc_rotate_angle, DEFAULT_ROTATE_ANGLE)
        mMaxValue = ta.getInt(R.styleable.ArcSeekBar_arc_max, DEFAULT_MAX_VALUE)
        mMinValue = ta.getInt(R.styleable.ArcSeekBar_arc_min, DEFAULT_MIN_VALUE)
        // 如果用户设置的最大值和最小值不合理，则直接按照默认进行处理
        if (mMaxValue <= mMinValue) {
            mMaxValue = DEFAULT_MAX_VALUE
            mMinValue = DEFAULT_MIN_VALUE
        }
        val progress = ta.getInt(R.styleable.ArcSeekBar_arc_progress, mMinValue)
        this.progress = progress
        mBorderWidth = ta.getDimensionPixelSize(
            R.styleable.ArcSeekBar_arc_border_width, dp2px(DEFAULT_BORDER_WIDTH)
        )
        mBorderColor = ta.getColor(R.styleable.ArcSeekBar_arc_border_color, DEFAULT_BORDER_COLOR)
        mThumbColor = ta.getColor(R.styleable.ArcSeekBar_arc_thumb_color, DEFAULT_THUMB_COLOR)
        mThumbRadius = ta.getDimensionPixelSize(
            R.styleable.ArcSeekBar_arc_thumb_radius, dp2px(DEFAULT_THUMB_RADIUS)
        ).toFloat()
        mThumbShadowRadius = ta.getDimensionPixelSize(
            R.styleable.ArcSeekBar_arc_thumb_shadow_radius, dp2px(DEFAULT_THUMB_SHADOW_RADIUS)
        ).toFloat()
        mThumbShadowColor =
            ta.getColor(R.styleable.ArcSeekBar_arc_thumb_shadow_color, DEFAULT_THUMB_SHADOW_COLOR)
        mThumbWidth = ta.getDimensionPixelSize(
            R.styleable.ArcSeekBar_arc_thumb_width, dp2px(DEFAULT_THUMB_WIDTH)
        ).toFloat()
        mThumbMode = ta.getInt(R.styleable.ArcSeekBar_arc_thumb_mode, THUMB_MODE_STROKE)
        mShadowRadius = ta.getDimensionPixelSize(
            R.styleable.ArcSeekBar_arc_shadow_radius, dp2px(DEFAULT_SHADOW_RADIUS)
        )
        ta.recycle()
    }

    // 获取 Arc 颜色数组
    private fun getArcColors(context: Context, ta: TypedArray): IntArray {
        val ret: IntArray
        var resId = ta.getResourceId(R.styleable.ArcSeekBar_arc_colors, 0)
        if (0 == resId) {
            resId = R.array.arc_colors_default
        }
        ret = getColorsByArrayResId(context, resId)
        return ret
    }

    // 根据 resId 获取颜色数组
    private fun getColorsByArrayResId(context: Context, resId: Int): IntArray {
        val ret: IntArray
        val colorArray = context.resources.obtainTypedArray(resId)
        ret = IntArray(colorArray.length())
        for (i in 0 until colorArray.length()) {
            ret[i] = colorArray.getColor(i, 0)
        }
        return ret
    }

    // 初始化数据
    private fun initData() {
        mSeekPath = Path()
        mBorderPath = Path()
        mSeekPathMeasure = PathMeasure()
        mTempPos = FloatArray(2)
        mTempTan = FloatArray(2)
        mDetector = GestureDetector(context, OnClickListener())
        mInvertMatrix = Matrix()
        mArcRegion = Region()
    }

    // 初始化画笔
    private fun initPaint() {
        initArcPaint()
        initThumbPaint()
        initBorderPaint()
        initShadowPaint()
    }

    // 初始化圆弧画笔
    private fun initArcPaint() {
        mArcPaint = Paint()
        mArcPaint!!.isAntiAlias = true
        mArcPaint!!.strokeWidth = mArcWidth
        mArcPaint!!.style = Paint.Style.STROKE
        mArcPaint!!.strokeCap = Paint.Cap.ROUND
    }

    // 初始化拖动按钮画笔
    private fun initThumbPaint() {
        mThumbPaint = Paint()
        mThumbPaint!!.isAntiAlias = true
        mThumbPaint!!.color = mThumbColor
        mThumbPaint!!.strokeWidth = mThumbWidth
        mThumbPaint!!.strokeCap = Paint.Cap.ROUND
        when (mThumbMode) {
            THUMB_MODE_FILL -> mThumbPaint!!.style = Paint.Style.FILL_AND_STROKE
            THUMB_MODE_FILL_STROKE -> mThumbPaint!!.style = Paint.Style.FILL_AND_STROKE
            else -> mThumbPaint!!.style = Paint.Style.STROKE
        }
        mThumbPaint!!.textSize = 56f
    }

    // 初始化拖动按钮画笔
    private fun initBorderPaint() {
        mBorderPaint = Paint()
        mBorderPaint!!.isAntiAlias = true
        mBorderPaint!!.color = mBorderColor
        mBorderPaint!!.strokeWidth = mBorderWidth.toFloat()
        mBorderPaint!!.style = Paint.Style.STROKE
    }

    // 初始化阴影画笔
    private fun initShadowPaint() {
        mShadowPaint = Paint()
        mShadowPaint!!.isAntiAlias = true
        mShadowPaint!!.strokeWidth = mBorderWidth.toFloat()
        mShadowPaint!!.style = Paint.Style.FILL_AND_STROKE
    }

    //--- 初始化结束 -------------------------------------------------------------------------------
    //--- 状态存储 ---------------------------------------------------------------------------------
    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putFloat(KEY_PROGRESS_PRESENT, mProgressPresent)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        var state: Parcelable? = state
        if (state is Bundle) {
            val bundle = state
            mProgressPresent = bundle.getFloat(KEY_PROGRESS_PRESENT)
            state = bundle.getParcelable("superState")
        }
        if (null != mOnProgressChangeListener) {
            mOnProgressChangeListener!!.onProgressChanged(this, progress, false)
        }
        super.onRestoreInstanceState(state)
    }

    //--- 状态存储结束 -----------------------------------------------------------------------------
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var ws = MeasureSpec.getSize(widthMeasureSpec)  // 取出宽度的确切数值
        var wm = MeasureSpec.getMode(widthMeasureSpec)  // 取出宽度的测量模式
        var hs = MeasureSpec.getSize(heightMeasureSpec) // 取出高度的确切数值
        var hm = MeasureSpec.getMode(heightMeasureSpec) // 取出高度的测量模
        if (wm == MeasureSpec.UNSPECIFIED) {
            wm = MeasureSpec.EXACTLY
            ws = dp2px(DEFAULT_EDGE_LENGTH)
        } else if (wm == MeasureSpec.AT_MOST) {
            wm = MeasureSpec.EXACTLY
            ws = Math.min(dp2px(DEFAULT_EDGE_LENGTH), ws)
        }
        if (hm == MeasureSpec.UNSPECIFIED) {
            hm = MeasureSpec.EXACTLY
            hs = dp2px(DEFAULT_EDGE_LENGTH)
        } else if (hm == MeasureSpec.AT_MOST) {
            hm = MeasureSpec.EXACTLY
            hs = Math.min(dp2px(DEFAULT_EDGE_LENGTH), hs)
        }
        setMeasuredDimension(
            MeasureSpec.makeMeasureSpec(ws, wm),
            MeasureSpec.makeMeasureSpec(hs, hm)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // 计算在当前大小下,内容应该显示的大小和起始位置
        val safeW = w - paddingLeft - paddingRight
        val safeH = h - paddingTop - paddingBottom
        val edgeLength: Float
        val startX: Float
        val startY: Float
        val fix = mArcWidth / 2 + mBorderWidth + mShadowRadius * 2 // 修正距离,画笔宽度的修正
        if (safeW < safeH) {
            // 宽度小于高度,以宽度为准
            edgeLength = safeW - fix
            startX = paddingLeft.toFloat()
            startY = (safeH - safeW) / 2.0f + paddingTop
        } else {
            // 宽度大于高度,以高度为准
            edgeLength = safeH - fix
            startX = (safeW - safeH) / 2.0f + paddingLeft
            startY = paddingTop.toFloat()
        }

        // 得到显示区域和中心的
        val content = RectF(startX + fix, startY + fix, startX + edgeLength, startY + edgeLength)
        mCenterX = content.centerX()
        mCenterY = content.centerY()

        // 得到路径
        mSeekPath!!.reset()
        mSeekPath!!.addArc(content, mOpenAngle / 2, CIRCLE_ANGLE - mOpenAngle)
        mSeekPathMeasure!!.setPath(mSeekPath, false)
        computeThumbPos(mProgressPresent)
        resetShaderColor()
        mInvertMatrix!!.reset()
        mInvertMatrix!!.preRotate(-mRotateAngle, mCenterX, mCenterY)
        mArcPaint!!.getFillPath(mSeekPath, mBorderPath)
        mBorderPath!!.close()
        mArcRegion!!.setPath(mBorderPath!!, Region(0, 0, w, h))
    }

    // 重置 shader 颜色
    private fun resetShaderColor() {
        // 计算渐变数组
        val startPos = mOpenAngle / 2 / CIRCLE_ANGLE
        val stopPos = (CIRCLE_ANGLE - mOpenAngle / 2) / CIRCLE_ANGLE
        val len = mArcColors!!.size - 1
        val distance = (stopPos - startPos) / len
        val pos = FloatArray(mArcColors!!.size)
        for (i in mArcColors!!.indices) {
            pos[i] = startPos + distance * i
        }
        val gradient = SweepGradient(mCenterX, mCenterY, mArcColors!!, pos)
        mArcPaint!!.setShader(gradient)
    }

    // 具体绘制
    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.rotate(mRotateAngle, mCenterX, mCenterY)
        mShadowPaint!!.setShadowLayer((mShadowRadius * 2).toFloat(), 0f, 0f, color)
        canvas.drawPath(mBorderPath!!, mShadowPaint!!)
        canvas.drawPath(mSeekPath!!, mArcPaint!!)
        if (mBorderWidth > 0) {
            canvas.drawPath(mBorderPath!!, mBorderPaint!!)
        }
        if (mThumbShadowRadius > 0) {
            mThumbPaint!!.setShadowLayer(mThumbShadowRadius, 0f, 0f, mThumbShadowColor)
            canvas.drawCircle(mThumbX, mThumbY, mThumbRadius, mThumbPaint!!)
            mThumbPaint!!.clearShadowLayer()
        }
        canvas.drawCircle(mThumbX, mThumbY, mThumbRadius, mThumbPaint!!)
        canvas.restore()
    }

    private var moved = false
    private var lastProgress = -1

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        val action = event.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                moved = false
                judgeCanDrag(event)
                if (null != mOnProgressChangeListener) {
                    mOnProgressChangeListener!!.onStartTrackingTouch(this)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (!mCanDrag) {
                    return true
                }
                val tempProgressPresent = getCurrentProgress(event.x, event.y)
                if (!mAllowTouchSkip) {
                    // 不允许突变
                    if (abs(tempProgressPresent - mProgressPresent) > 0.5f) {
                        return true
                    }
                }
                // 允许突变 或者非突变
                mProgressPresent = tempProgressPresent
                computeThumbPos(mProgressPresent)
                // 事件回调
                if (null != mOnProgressChangeListener && progress != lastProgress) {
                    mOnProgressChangeListener!!.onProgressChanged(this, progress, true)
                    lastProgress = progress
                }
                moved = true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> if (null != mOnProgressChangeListener && moved) {
                mOnProgressChangeListener!!.onStopTrackingTouch(this)
            }
        }
        mDetector!!.onTouchEvent(event)
        invalidate()
        return true
    }

    // 判断是否允许拖动
    private fun judgeCanDrag(event: MotionEvent) {
        val pos = floatArrayOf(event.x, event.y)
        mInvertMatrix!!.mapPoints(pos)
        mCanDrag = getDistance(pos[0], pos[1]) <= mThumbRadius * 1.5
    }

    private inner class OnClickListener : SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            // 判断是否点击在了进度区域
            if (!isInArcProgress(e.x, e.y)) return false
            // 点击允许突变
            mProgressPresent = getCurrentProgress(e.x, e.y)
            computeThumbPos(mProgressPresent)
            // 事件回调
            if (null != mOnProgressChangeListener) {
                mOnProgressChangeListener!!.onProgressChanged(this@ArcSeekBar, progress, true)
                mOnProgressChangeListener!!.onStopTrackingTouch(this@ArcSeekBar)
            }
            return true
        }
    }

    // 判断该点是否在进度条上面
    private fun isInArcProgress(px: Float, py: Float): Boolean {
        val pos = floatArrayOf(px, py)
        mInvertMatrix!!.mapPoints(pos)
        return mArcRegion!!.contains(pos[0].toInt(), pos[1].toInt())
    }

    // 获取当前进度理论进度数值
    private fun getCurrentProgress(px: Float, py: Float): Float {
        val diffAngle = getDiffAngle(px, py)
        var progress = diffAngle / (CIRCLE_ANGLE - mOpenAngle)
        if (progress < 0) progress = 0f
        if (progress > 1) progress = 1f
        return progress
    }

    // 获得当前点击位置所成角度与开始角度之间的数值差
    private fun getDiffAngle(px: Float, py: Float): Float {
        val angle = getAngle(px, py)
        var diffAngle: Float
        diffAngle = angle - mRotateAngle
        if (diffAngle < 0) {
            diffAngle = (diffAngle + CIRCLE_ANGLE) % CIRCLE_ANGLE
        }
        diffAngle -= mOpenAngle / 2
        return diffAngle
    }

    // 计算指定位置与内容区域中心点的夹角
    private fun getAngle(px: Float, py: Float): Float {
        var angle =
            (atan2((py - mCenterY).toDouble(), (px - mCenterX).toDouble()) * 180 / 3.14f).toFloat()
        if (angle < 0) {
            angle += 360f
        }
        return angle
    }

    // 计算指定位置与上次位置的距离
    private fun getDistance(px: Float, py: Float): Float {
        return sqrt(((px - mThumbX) * (px - mThumbX) + (py - mThumbY) * (py - mThumbY)).toDouble()).toFloat()
    }

    private fun dp2px(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    // 计算拖动块应该显示的位置
    private fun computeThumbPos(present: Float) {
        var present = present
        if (present < 0) present = 0f
        if (present > 1) present = 1f
        if (null == mSeekPathMeasure) return
        val distance = mSeekPathMeasure!!.length * present
        mSeekPathMeasure!!.getPosTan(distance, mTempPos, mTempTan)
        mThumbX = mTempPos!![0]
        mThumbY = mTempPos!![1]
    }

    //--- 线性取色 ---------------------------------------------------------------------------------
    val color: Int
        /**
         * 获取当前进度的具体颜色
         *
         * @return 当前进度在渐变中的颜色
         */
        get() = getColor(mProgressPresent)

    /**
     * 获取某个百分比位置的颜色
     *
     * @param radio 取值[0,1]
     * @return 最终颜色
     */
    private fun getColor(radio: Float): Int {
        val diatance = 1.0f / (mArcColors!!.size - 1)
        val startColor: Int
        val endColor: Int
        if (radio >= 1) {
            return mArcColors!![mArcColors!!.size - 1]
        }
        for (i in mArcColors!!.indices) {
            if (radio <= i * diatance) {
                if (i == 0) {
                    return mArcColors!![0]
                }
                startColor = mArcColors!![i - 1]
                endColor = mArcColors!![i]
                val areaRadio = getAreaRadio(radio, diatance * (i - 1), diatance * i)
                return getColorFrom(startColor, endColor, areaRadio)
            }
        }
        return -1
    }

    /**
     * 计算当前比例在子区间的比例
     *
     * @param radio         总比例
     * @param startPosition 子区间开始位置
     * @param endPosition   子区间结束位置
     * @return 自区间比例[0, 1]
     */
    private fun getAreaRadio(radio: Float, startPosition: Float, endPosition: Float): Float {
        return (radio - startPosition) / (endPosition - startPosition)
    }

    /**
     * 取两个颜色间的渐变区间 中的某一点的颜色
     *
     * @param startColor 开始的颜色
     * @param endColor   结束的颜色
     * @param radio      比例 [0, 1]
     * @return 选中点的颜色
     */
    private fun getColorFrom(startColor: Int, endColor: Int, radio: Float): Int {
        val redStart = Color.red(startColor)
        val blueStart = Color.blue(startColor)
        val greenStart = Color.green(startColor)
        val redEnd = Color.red(endColor)
        val blueEnd = Color.blue(endColor)
        val greenEnd = Color.green(endColor)
        val red = (redStart + ((redEnd - redStart) * radio + 0.5)).toInt()
        val greed = (greenStart + ((greenEnd - greenStart) * radio + 0.5)).toInt()
        val blue = (blueStart + ((blueEnd - blueStart) * radio + 0.5)).toInt()
        return Color.argb(255, red, greed, blue)
    }

    //region 对外接口 -------------------------------------------------------------------------------
    var progress: Int
        /**
         * 获取当前进度数值
         *
         * @return 当前进度数值
         */
        get() = (mProgressPresent * (mMaxValue - mMinValue)).toInt() + mMinValue
        /**
         * 设置进度
         *
         * @param progress 进度值
         */
        set(progress) {
            var progress = progress
            println("setProgress = $progress")
            if (progress > mMaxValue) progress = mMaxValue
            if (progress < mMinValue) progress = mMinValue
            mProgressPresent = (progress - mMinValue) * 1.0f / (mMaxValue - mMinValue)
            println("setProgress present = $mProgressPresent")
            if (null != mOnProgressChangeListener) {
                mOnProgressChangeListener!!.onProgressChanged(this, progress, false)
            }
            computeThumbPos(mProgressPresent)
            postInvalidate()
        }

    /**
     * 设置颜色
     *
     * @param colors 颜色
     */
    fun setArcColors(colors: IntArray) {
        mArcColors = colors
        resetShaderColor()
        postInvalidate()
    }

    /**
     * 设置最大数值
     *
     * @param max 最大数值
     */
    fun setMaxValue(max: Int) {
        mMaxValue = max
    }

    /**
     * 设置最小数值
     *
     * @param min 最小数值
     */
    fun setMinValue(min: Int) {
        mMinValue = min
    }

    /**
     * 设置颜色
     *
     * @param colorArrayRes 颜色资源 R.array.arc_color
     */
    fun setArcColors(colorArrayRes: Int) {
        setArcColors(getColorsByArrayResId(context, colorArrayRes))
    }

    // endregion -----------------------------------------------------------------------------------
    // region 状态回调 ------------------------------------------------------------------------------
    private var mOnProgressChangeListener: OnProgressChangeListener? = null

    init {
        isSaveEnabled = true
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        initAttrs(context, attrs)
        initData()
        initPaint()
    }

    fun setOnProgressChangeListener(onProgressChangeListener: OnProgressChangeListener?) {
        mOnProgressChangeListener = onProgressChangeListener
    }

    interface OnProgressChangeListener {
        /**
         * 进度发生变化
         *
         * @param seekBar  拖动条
         * @param progress 当前进度数值
         * @param isUser   是否是用户操作, true 表示用户拖动, false 表示通过代码设置
         */
        fun onProgressChanged(seekBar: ArcSeekBar?, progress: Int, isUser: Boolean)

        /**
         * 用户开始拖动
         *
         * @param seekBar 拖动条
         */
        fun onStartTrackingTouch(seekBar: ArcSeekBar?)

        /**
         * 用户结束拖动
         *
         * @param seekBar 拖动条
         */
        fun onStopTrackingTouch(seekBar: ArcSeekBar?)
    }
    // endregion -----------------------------------------------------------------------------------

    companion object {
        private const val DEFAULT_EDGE_LENGTH = 260                 // 默认宽高
        private const val CIRCLE_ANGLE = 360F                       // 圆周角
        private const val DEFAULT_ARC_WIDTH = 40                    // 默认宽度 dp
        private const val DEFAULT_OPEN_ANGLE = 120F                 // 开口角度
        private const val DEFAULT_ROTATE_ANGLE = 90F                // 旋转角度
        private const val DEFAULT_BORDER_WIDTH = 0                  // 默认描边宽度
        private const val DEFAULT_BORDER_COLOR = -0x1               // 默认描边颜色
        private const val DEFAULT_THUMB_COLOR = -0x1                // 拖动按钮颜色
        private const val DEFAULT_THUMB_WIDTH = 2                   // 拖动按钮描边宽度 dp
        private const val DEFAULT_THUMB_RADIUS = 15                 // 拖动按钮半径 dp
        private const val DEFAULT_THUMB_SHADOW_RADIUS = 0           // 拖动按钮阴影半径 dp
        private const val DEFAULT_THUMB_SHADOW_COLOR = -0x1000000   // 拖动按钮阴影颜色
        private const val DEFAULT_SHADOW_RADIUS = 0                 // 默认阴影半径 dp
        private const val THUMB_MODE_STROKE = 0                     // 拖动按钮模式 - 描边
        private const val THUMB_MODE_FILL = 1                       // 拖动按钮模式 - 填充
        private const val THUMB_MODE_FILL_STROKE = 2                // 拖动按钮模式 - 填充+描边
        private const val DEFAULT_MAX_VALUE = 100                   // 默认最大数值
        private const val DEFAULT_MIN_VALUE = 0                     // 默认最小数值
        private const val KEY_PROGRESS_PRESENT = "PRESENT"          // 用于存储和获取当前百分比
    }
}
