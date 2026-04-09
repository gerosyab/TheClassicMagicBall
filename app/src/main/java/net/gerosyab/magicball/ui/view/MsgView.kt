/*
 * TheClassicMagicBall - Android Magic 8 Ball Simulator
 * Copyright (C) 2014 DAISSUE
 */
package net.gerosyab.magicball.ui.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.ArrayList
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import net.gerosyab.magicball.data.Const
import net.gerosyab.magicball.ui.MainActivity
import net.gerosyab.magicball.util.MyLog

class MsgView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
    ) : View(context, attrs, defStyle) {
        private var shaker: net.gerosyab.magicball.util.Shaker? = null
        private var widthPx = 0
        private var heightPx = 0
        var cx: Float = 0f
            private set
        var cy: Float = 0f
            private set
        private var bcx: Float = 0f
        private var bcy: Float = 0f
        private var x: Float = 0f
        private var y: Float = 0f
        private var bcxCon: Float = 0f
        private var bcyCon: Float = 0f
        private var outerRadius = 0f
        private var reflectRadius = 0f
        private val reflectRectF = RectF()
        private var innerOuterRadius = 0f
        private var innerInnerRadius = 0f
        private val msgPaint = Paint()
        private val debugPaint = Paint()
        private val debugTextPaint = Paint()
        private val debugCenterTracePaint = Paint()
        private val debugCirclePaint = Paint()
        private val blackPaint = Paint()
        private val reflectPaint = Paint()
        private val innerOuterPaint = Paint()
        private val innerInnerPaint = Paint()
        private val points = ArrayList<Point>()
        private var cxcyBoundaryRadius = 0f
        private var cxcyBoundaryRadiusSquare = 0f
        private val opts = BitmapFactory.Options()
        private val reflectionBoundary = 15f
        private var reflectionDegree = 0f
        private var reflectionIncrease = true
        private var bitmap: Bitmap? = null
        private var resized: Bitmap? = null
        private var nBitmapHalfWidth = 0f
        private var nBitmapHalfHeight = 0f
        private var nMsgTriangleWidth = 0f
        private var nMsgTriangleHeight = 0f
        private var rIncrease = false
        private var isBoundaryOut = false
        private var mSensorX = 0f
        private var mSensorY = 0f
        private var radian = 0f
        private val rBoundary = 5f
        private var degree = 0f
        private var appearFlag = false
        private val alphaValueTable =
            intArrayOf(
                0, 1, 3, 6, 10, 16, 24, 34, 46, 60,
                76, 94, 114, 127, 141, 161, 179, 195, 209, 221,
                231, 239, 245, 249, 252, 254, 255,
            )
        private var alphaIndex = 0
        private val scaleValueTable =
            floatArrayOf(
                0.855192408f, 0.861081942f, 0.866947531f, 0.872789368f, 0.878607646f,
                0.884402553f, 0.890174277f, 0.895923002f, 0.901648911f, 0.907352184f,
                0.913033f, 0.918691534f, 0.924327962f, 0.929942454f, 0.935535181f,
                0.941106311f, 0.94665601f, 0.952184443f, 0.957691771f, 0.963178156f,
                0.968643756f, 0.974088728f, 0.979513226f, 0.984917405f, 0.990301417f,
                0.99566541f, 1f,
            )
        private var scaleIndex = 0
        private var touchCount = 0
        private val touchCountMax = 10
        private val touchArea = 250
        private var touchAreaCheck = false

        @JvmField
        var msgIdx: Int = 0

        /** 1f = phone (large ball); &lt;1 shrinks ball on tablet side panel. */
        var tabletScaleFactor: Float = 1f
            set(value) {
                field = value.coerceIn(0.35f, 1f)
                requestLayout()
            }

        init {
            setLayerType(LAYER_TYPE_HARDWARE, null)
        }

        override fun onSizeChanged(
            w: Int,
            h: Int,
            oldw: Int,
            oldh: Int,
        ) {
            super.onSizeChanged(w, h, oldw, oldh)
            widthPx = w
            heightPx = h
            if (w <= 0 || h <= 0) return
            shaker = MainActivity.getShakerInstance()
            val scale = tabletScaleFactor.coerceIn(0.35f, 1f)
            val maxDiamPx = resources.getDimension(R.dimen.magic_ball_max_diameter)
            val maxRadiusCap = maxDiamPx / 2f
            val radiusFromWidth = w * 0.75f * scale
            val maxRadiusFromHeight = h * 0.42f
            outerRadius = min(min(radiusFromWidth, maxRadiusCap), maxRadiusFromHeight)
            reflectRadius = outerRadius * 0.95f
            innerOuterRadius = outerRadius * 0.5f
            innerInnerRadius = outerRadius * 0.45f
            cxcyBoundaryRadius = innerInnerRadius * 0.2f
            cxcyBoundaryRadiusSquare = cxcyBoundaryRadius * cxcyBoundaryRadius
            nMsgTriangleWidth = innerOuterRadius * 1.2f
            nMsgTriangleHeight = innerOuterRadius * 1.2f
            nBitmapHalfWidth = nMsgTriangleWidth / 2f
            nBitmapHalfHeight = nMsgTriangleHeight / 2f
            cx = w / 2f
            cy = h / 2f
            x = cx - nBitmapHalfWidth
            y = cy - nBitmapHalfHeight
            bcx = cx
            bcy = cy
            bcxCon = 0f
            bcyCon = 0f
            debugPaint.color = Color.YELLOW
            debugPaint.isAntiAlias = true
            debugPaint.strokeWidth = 2f
            debugPaint.style = Paint.Style.STROKE
            debugTextPaint.color = Color.WHITE
            debugTextPaint.isAntiAlias = true
            debugTextPaint.strokeWidth = 2f
            debugTextPaint.style = Paint.Style.FILL_AND_STROKE
            debugTextPaint.textSize = 35f
            debugCenterTracePaint.color = Color.RED
            debugCenterTracePaint.isAntiAlias = true
            debugCenterTracePaint.strokeWidth = 5f
            debugCenterTracePaint.style = Paint.Style.STROKE
            debugCenterTracePaint.textSize = 35f
            debugCirclePaint.color = Color.GREEN
            debugCirclePaint.isAntiAlias = true
            debugCirclePaint.strokeWidth = 7f
            debugCirclePaint.style = Paint.Style.STROKE
            debugCirclePaint.textSize = 35f
            blackPaint.color = Color.BLACK
            blackPaint.isAntiAlias = true
            blackPaint.style = Paint.Style.FILL
            reflectPaint.color = Color.argb(45, 255, 255, 255)
            reflectPaint.isAntiAlias = true
            reflectPaint.style = Paint.Style.FILL
            reflectRectF.set(
                cx - reflectRadius,
                cy - reflectRadius,
                cx + reflectRadius,
                cy + reflectRadius,
            )
            innerOuterPaint.color = Color.rgb(20, 20, 20)
            innerOuterPaint.isAntiAlias = true
            innerInnerPaint.color = Color.rgb(5, 20, 60)
            innerInnerPaint.isAntiAlias = true
            msgPaint.isAntiAlias = true
            msgPaint.isFilterBitmap = true
            msgPaint.isDither = true
            notifyMsgChanged()
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (event.x > widthPx - touchArea && event.x < widthPx &&
                        event.y > 0 && event.y < touchArea
                    ) {
                        touchAreaCheck = true
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (touchAreaCheck) {
                        touchAreaCheck = false
                        touchCount++
                        if (touchCount > touchCountMax) {
                            touchCount = 0
                            Const.setViewDebuggingMode(!Const.VIEW_DEBUG)
                        }
                    }
                }
            }
            return true
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (isInEditMode || widthPx <= 0) return
            canvas.drawCircle(cx, cy, outerRadius, blackPaint)
            if (reflectionIncrease) {
                reflectionDegree += 0.1f
                if (reflectionDegree >= reflectionBoundary) {
                    reflectionIncrease = false
                }
            } else {
                reflectionDegree -= 0.1f
                if (reflectionDegree <= -reflectionBoundary) {
                    reflectionIncrease = true
                }
            }
            canvas.drawArc(reflectRectF, 135 + reflectionDegree, 180f, true, reflectPaint)
            canvas.drawCircle(cx, cy, innerOuterRadius, innerOuterPaint)
            canvas.drawCircle(cx, cy, innerInnerRadius, innerInnerPaint)
            if (Const.VIEW_DEBUG) {
                points.add(Point(bcx.toInt(), bcy.toInt()))
                if (points.size > 300) {
                    points.removeAt(0)
                }
            }
            if (rIncrease) {
                degree += 0.1f
                if (degree >= rBoundary) {
                    rIncrease = false
                }
            } else {
                degree -= 0.1f
                if (degree <= -rBoundary) {
                    rIncrease = true
                }
            }
            update()
            canvas.rotate(degree, bcx, bcy)
            if (Const.VIEW_DEBUG) {
                canvas.drawCircle(bcx, bcy, nBitmapHalfWidth, debugCirclePaint)
            }
            if (appearFlag) {
                msgPaint.alpha = alphaValueTable[alphaIndex]
                alphaIndex++
                canvas.scale(
                    scaleValueTable[scaleIndex],
                    scaleValueTable[scaleIndex],
                    bcx,
                    bcy,
                )
                scaleIndex++
                if (alphaIndex >= alphaValueTable.size) {
                    appearFlag = false
                }
            }
            resized?.let { canvas.drawBitmap(it, x, y, msgPaint) }
            if (Const.VIEW_DEBUG) {
                canvas.drawCircle(bcx, bcy, 10f, debugCenterTracePaint)
                canvas.drawCircle(cx, cy, cxcyBoundaryRadius, debugPaint)
            }
            canvas.rotate(-degree, bcx, bcy)
            if (Const.VIEW_DEBUG) {
                canvas.drawRect(
                    (widthPx - touchArea).toFloat(),
                    0f,
                    widthPx.toFloat(),
                    touchArea.toFloat(),
                    debugPaint,
                )
                canvas.drawText("mSensorX : $mSensorX", 50f, 100f, debugTextPaint)
                canvas.drawText("mSensorY : $mSensorY", 50f, 150f, debugTextPaint)
                canvas.drawText("mSensorZ : $mSensorY", 50f, 200f, debugTextPaint)
                canvas.drawText("x : $x", 50f, 250f, debugTextPaint)
                canvas.drawText(", y : $y", 400f, 250f, debugTextPaint)
                canvas.drawText("bcx : $bcx", 50f, 300f, debugTextPaint)
                canvas.drawText(", bcy : $bcy", 400f, 300f, debugTextPaint)
                canvas.drawText("bcxCon : $bcxCon", 50f, 350f, debugTextPaint)
                canvas.drawText(", bcyCon : $bcyCon", 400f, 350f, debugTextPaint)
                canvas.drawText(
                    "isBoundaryOut : $isBoundaryOut, cx : $cx, cy : $cy",
                    50f,
                    400f,
                    debugTextPaint,
                )
                val length = points.size
                var blue = 0
                var increase = true
                if (length > 1) {
                    var i = 1
                    var point = points[0]
                    var prevX = point.x
                    var prevY = point.y
                    debugCenterTracePaint.color = Color.rgb(255, 0, 0)
                    do {
                        point = points[i]
                        canvas.drawLine(
                            prevX.toFloat(),
                            prevY.toFloat(),
                            point.x.toFloat(),
                            point.y.toFloat(),
                            debugCenterTracePaint,
                        )
                        prevX = point.x
                        prevY = point.y
                        i++
                        if (increase) {
                            blue++
                            if (blue > 255) {
                                blue--
                                increase = false
                            }
                        } else {
                            blue--
                            if (blue < 0) {
                                blue++
                                increase = true
                            }
                        }
                        debugCenterTracePaint.color = Color.rgb(255, blue, 0)
                    } while (i < length)
                }
            }
            invalidate()
        }

        private fun update() {
            val s = shaker ?: return
            mSensorX = s.sx
            mSensorY = s.sy
            bcxCon += mSensorX
            bcyCon -= mSensorY
            if (bcxCon * bcxCon + bcyCon * bcyCon >= cxcyBoundaryRadiusSquare) {
                isBoundaryOut = true
                if (bcxCon != 0f && bcyCon != 0f) {
                    radian = atan2(bcyCon.toDouble(), bcxCon.toDouble()).toFloat()
                }
                bcxCon = (cos(radian.toDouble()) * cxcyBoundaryRadius).toFloat()
                bcyCon = (sin(radian.toDouble()) * cxcyBoundaryRadius).toFloat()
            } else {
                isBoundaryOut = false
            }
            bcx = bcxCon + cx
            bcy = bcyCon + cy
            x = bcx - nBitmapHalfWidth
            y = bcy - nBitmapHalfHeight
        }

        private fun setNewMsg(index: Int) {
            MyLog.d(
                "MsgView",
                "setNewMsg - nMsgTriangleWidth : $nMsgTriangleWidth, nMsgTriangleHeight : $nMsgTriangleHeight",
            )
            val resId = Const.MSG_IDS[index]
            bitmap?.recycle()
            bitmap = BitmapFactory.decodeResource(resources, resId, opts)
            resized?.recycle()
            val bmp = bitmap ?: return
            resized =
                Bitmap.createScaledBitmap(
                    bmp,
                    nMsgTriangleWidth.toInt(),
                    nMsgTriangleHeight.toInt(),
                    true,
                )
            appearFlag = true
            alphaIndex = 0
            scaleIndex = 0
        }

        fun notifyMsgChanged() {
            MyLog.d("MsgView", "notifyMsgChanged")
            if (widthPx > 0) {
                setNewMsg(msgIdx)
            }
        }

        fun setMsgIdx(index: Int) {
            MyLog.d("MsgView", "setMsgIdx, msg index : $index")
            msgIdx = index
        }
    }
