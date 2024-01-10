package com.tyhoo.android.view.widget

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.View
import com.tyhoo.android.view.R
import java.util.*
import kotlin.math.pow

/**
 * 自定义 View，绘制三阶贝塞尔曲线音符动画
 */
class FloatNoteView(context: Context) : View(context) {

    // 贝塞尔曲线
    private lateinit var startPoint: Point      // 起点
    private lateinit var endPoint: Point        // 终点
    private lateinit var conLeftPoint: Point    // 左控制点
    private lateinit var conRightPoint: Point   // 右控制点
    private lateinit var value: Point           // 动态改变的点

    // 绘制贝塞尔曲线
    private lateinit var routePaint: Paint      // 画笔
    private lateinit var path: Path             // 路径

    private var width = 0                       // View 宽度
    private var height = 0                      // View 高度

    // 生成随机数的实例
    private var random: Random? = null

    // 偏移量值
    private var offset = 0

    // 偏移量的正负值
    private var offsetPositiveOrNegative = 0

    // 绘制图像的透明度
    private var alpha = 255F

    // 图片资源
    private var bitmap: Bitmap? = null

    // 图片
    private var bapWidth = 0                    // 宽度
    private var bapHeight = 0                   // 高度

    // 动画实例
    private var valueAnimator: ValueAnimator? = null

    init {
        initAttr()
        initPatin()
    }

    /**
     * 重写 onMeasure 函数，用于测量 View 的宽度和高度
     */
    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 获取测量的宽度和高度值
        width = MeasureSpec.getSize(widthMeasureSpec)
        height = MeasureSpec.getSize(heightMeasureSpec)

        // 设置 View 的宽度和高度
        setMeasuredDimension(width, height)

        // 初始化随机数生成器
        random = Random()

        // 设置起点和终点坐标
        startPoint.x = width / 2
        startPoint.y = height
        endPoint.x = width / 2
        endPoint.y = 0

        // 设置控制点坐标
        conLeftPoint.x = width
        conLeftPoint.y = height * 3 / 4
        conRightPoint.x = 0
        conRightPoint.y = height / 4

        // 随机数控制音符出现的位置，offsetPositiveOrNegative 取值为 0 或 1
        offsetPositiveOrNegative = random?.nextInt(2) ?: 0
        offset = if (offsetPositiveOrNegative == 1) {
            // 位移为正
            random?.nextInt(200) ?: 0
        } else {
            // 位移为负
            val negative = random?.nextInt(200) ?: 0
            -negative
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawNote(canvas)
    }

    private fun initAttr() {
        startPoint = Point()
        endPoint = Point()
        conLeftPoint = Point()
        conRightPoint = Point()
        value = Point()
    }

    private fun initPatin() {
        // 初始化路径
        path = Path()
        // 初始化画笔
        routePaint = Paint().apply {
            isAntiAlias = true          // 抗锯齿
            color = Color.RED           // 画笔颜色
            alpha = 0                   // 画笔透明度
            strokeWidth = 20F           // 画笔宽度
            style = Paint.Style.STROKE  // 画笔样式，描边
        }
    }

    /**
     * 绘制音符
     */
    private fun drawNote(canvas: Canvas?) {
        if (canvas == null) return

        // 设置画笔透明度
        routePaint.alpha = alpha.toInt()

        // 加载音符图片资源
        bitmap = BitmapFactory.decodeResource(resources, R.drawable.note)
        bapWidth = bitmap?.width ?: 0
        bapHeight = bitmap?.height ?: 0

        // 绘制音符图片
        val matrix = Matrix()
        matrix.postTranslate((width - value.x - offset).toFloat(), (height - value.y).toFloat())
        canvas.drawBitmap(bitmap ?: return, matrix, routePaint)

        // 绘制贝塞尔曲线
        path.reset()
        path.moveTo((endPoint.x - offset).toFloat(), endPoint.y.toFloat())
        path.cubicTo(
            (conLeftPoint.x - offset).toFloat(), conLeftPoint.y.toFloat(),
            (conRightPoint.x - offset).toFloat(), conRightPoint.y.toFloat(),
            (startPoint.x - offset).toFloat(), startPoint.y.toFloat()
        )
        canvas.save()
    }

    fun addNote() {
        // 设置属性动画
        valueAnimator = ValueAnimator.ofObject(
            NoteTypeEvaluator(conLeftPoint, conRightPoint), endPoint, startPoint
        )
        valueAnimator?.duration = 6000
        valueAnimator?.addUpdateListener { animator ->
            val point = animator.animatedValue as Point
            value.x = point.x
            value.y = point.y
            if (point.y >= height / 2) {
                // 透明度不断减少
                alpha -= 4F
            }
            if (alpha < 0F) {
                alpha = 0F
            }
            postInvalidate()
        }
        // 开始动画
        valueAnimator?.start()
    }

    /**
     * 自定义的 TypeEvaluator，用于计算三阶贝塞尔曲线上的中间点坐标
     *
     * @param conLeftPoint  贝塞尔曲线左侧控制点
     * @param conRightPoint 贝塞尔曲线右侧控制点
     */
    inner class NoteTypeEvaluator(
        conLeftPoint: Point, conRightPoint: Point
    ) : TypeEvaluator<Point> {

        private val conLeftPoint: Point
        private val conRightPoint: Point

        init {
            // 初始化两个控制点坐标
            this.conLeftPoint = conLeftPoint
            this.conRightPoint = conRightPoint
        }

        // 计算贝塞尔曲线上的中间点坐标
        override fun evaluate(fraction: Float, startValue: Point?, endValue: Point?): Point {
            // 如果起点为空则初始化为 (0, 0)
            val start = startValue ?: Point(0, 0)
            // 如果终点为空则初始化为 (0, 0)
            val end = endValue ?: Point(0, 0)

            // 利用三阶贝塞尔曲线公式算出中间点坐标
            val x = (start.x * (1 - fraction).toDouble().pow(3.0) +
                    3 * conLeftPoint.x * fraction * (1 - fraction).toDouble().pow(2.0) +
                    3 * conRightPoint.x * fraction.toDouble().pow(2.0) * (1 - fraction) +
                    end.x * fraction.toDouble().pow(3.0)).toInt()
            val y = (start.y * (1 - fraction).toDouble().pow(3.0) +
                    3 * conLeftPoint.y * fraction * (1 - fraction).toDouble().pow(2.0) +
                    3 * conRightPoint.y * fraction.toDouble().pow(2.0) * (1 - fraction) +
                    end.y * fraction.toDouble().pow(3.0)).toInt()
            return Point(x, y)
        }
    }
}