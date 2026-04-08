/*
 * TheClassicMagicBall - Android Magic 8 Ball Simulator
 * Copyright (C) 2014 DAISSUE
 */
package net.gerosyab.magicball.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import net.gerosyab.magicball.util.MyLog

class FrontView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
    ) : SurfaceView(context, attrs, defStyle),
        SurfaceHolder.Callback {
        private val rBoundary = 15f
        private var degree = 0f
        private var rIncrease = true
        private var surfaceWidth = 0
        private var surfaceHeight = 0
        var cx: Float = 0f
            private set
        var cy: Float = 0f
            private set
        private var outerRadius = 0f
        private var reflectRadius = 0f
        private val reflectRectF = RectF()
        private var innerRadius = 0f
        private var charcterRadius1 = 0f
        private var charcterRadius2 = 0f
        private var strokeWidth = 0f
        private val blackPaint = Paint()
        private val reflectPaint = Paint()
        private val whitePaint = Paint()
        private val characterPaint = Paint()

        init {
            holder.addCallback(this)
        }

        val radius: Float
            get() = outerRadius

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawCircle(cx, cy, outerRadius, blackPaint)
            canvas.drawCircle(cx, cy, innerRadius, whitePaint)
            canvas.drawCircle(cx, cy - charcterRadius1, charcterRadius1, characterPaint)
            canvas.drawCircle(cx, cy + charcterRadius2, charcterRadius2, characterPaint)
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
            canvas.drawArc(reflectRectF, 135 + degree, 180f, true, reflectPaint)
            invalidate()
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            MyLog.d("FrontView", "surfaceCreated")
        }

        override fun surfaceChanged(
            holder: SurfaceHolder,
            format: Int,
            width: Int,
            height: Int,
        ) {
            surfaceWidth = width
            surfaceHeight = height
            cx = surfaceWidth / 2f
            cy = surfaceHeight / 2f - surfaceHeight * 0.1f
            outerRadius = (surfaceWidth * 0.275).toFloat()
            reflectRadius = outerRadius * 0.95f
            innerRadius = outerRadius * 0.425f
            charcterRadius1 = innerRadius * 0.225f
            charcterRadius2 = innerRadius * 0.25f
            strokeWidth = innerRadius * 0.1f
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
            whitePaint.color = Color.WHITE
            whitePaint.isAntiAlias = true
            characterPaint.color = Color.BLACK
            characterPaint.isAntiAlias = true
            characterPaint.strokeWidth = strokeWidth
            characterPaint.style = Paint.Style.STROKE
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            MyLog.d("FrontView", "surfaceDestroyed")
        }
    }
