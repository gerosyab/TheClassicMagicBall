/*
 * TheClassicMagicBall - Android Magic 8 Ball Simulator
 * Copyright (C) 2014 DAISSUE
 */
package net.gerosyab.magicball.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import kotlin.math.sin
import kotlin.random.Random

/**
 * Night-sky star layer: random positions/sizes, each twinkles on its own period and phase.
 */
class StarfieldView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        private val bgPaint =
            Paint().apply {
                color = Color.parseColor("#004271")
                style = Paint.Style.FILL
            }
        private val starPaint =
            Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }

        private var stars: Array<Star> = emptyArray()
        private var choreographer: Choreographer? = null
        private val frameCallback =
            object : Choreographer.FrameCallback {
                override fun doFrame(frameTimeNanos: Long) {
                    invalidate()
                    choreographer?.postFrameCallback(this)
                }
            }

        private data class Star(
            val x: Float,
            val y: Float,
            val radius: Float,
            val phase: Float,
            val periodNs: Long,
            val minAlpha: Int,
            val maxAlpha: Int,
        )

        override fun onSizeChanged(
            w: Int,
            h: Int,
            oldw: Int,
            oldh: Int,
        ) {
            super.onSizeChanged(w, h, oldw, oldh)
            if (w <= 0 || h <= 0) return
            val area = w * h
            val count = (area / 9000).coerceIn(60, 160)
            val rnd = Random(w * 1000 + h)
            stars =
                Array(count) {
                    Star(
                        x = rnd.nextFloat() * w,
                        y = rnd.nextFloat() * h,
                        radius = rnd.nextFloat() * 1.8f + 0.6f,
                        phase = rnd.nextFloat() * (Math.PI * 2).toFloat(),
                        periodNs = (rnd.nextLong(2_000_000_000L, 8_000_000_000L)),
                        minAlpha = rnd.nextInt(40, 100),
                        maxAlpha = rnd.nextInt(180, 255),
                    )
                }
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            choreographer = Choreographer.getInstance()
            choreographer?.postFrameCallback(frameCallback)
        }

        override fun onDetachedFromWindow() {
            choreographer?.removeFrameCallback(frameCallback)
            choreographer = null
            super.onDetachedFromWindow()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat()
            val h = height.toFloat()
            canvas.drawRect(0f, 0f, w, h, bgPaint)
            val now = System.nanoTime()
            for (s in stars) {
                val t = (now % s.periodNs).toDouble() / s.periodNs.toDouble()
                val wave = (sin(t * Math.PI * 2 + s.phase) * 0.5 + 0.5).toFloat()
                val alpha = (s.minAlpha + wave * (s.maxAlpha - s.minAlpha)).toInt().coerceIn(0, 255)
                starPaint.color = Color.argb(alpha, 255, 255, 255)
                canvas.drawCircle(s.x, s.y, s.radius, starPaint)
            }
        }
    }
