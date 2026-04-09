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
import android.graphics.Typeface
import android.content.res.Configuration
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.res.ResourcesCompat
import kotlin.math.min
import net.gerosyab.magicball.R

class FrontView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
    ) : View(context, attrs, defStyle) {
        private val rBoundary = 15f
        private var degree = 0f
        private var rIncrease = true
        private var viewWidth = 0
        private var viewHeight = 0
        var cx: Float = 0f
            private set
        var cy: Float = 0f
            private set
        private var outerRadius = 0f
        private var reflectRadius = 0f
        private val reflectRectF = RectF()
        private var innerRadius = 0f
        private var strokeWidth = 0f
        private val blackPaint = Paint()
        private val reflectPaint = Paint()
        private val whitePaint = Paint()
        private val eightPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textAlign = Paint.Align.CENTER
            }
        private var chakraTypeface: Typeface? = null

        init {
            setLayerType(LAYER_TYPE_HARDWARE, null)
        }

        val radius: Float
            get() = outerRadius

        private fun ensureTypeface() {
            if (chakraTypeface == null) {
                chakraTypeface =
                    ResourcesCompat.getFont(context, R.font.chakra_petch_regular)
            }
            eightPaint.typeface = chakraTypeface ?: Typeface.DEFAULT_BOLD
        }

        override fun onSizeChanged(
            w: Int,
            h: Int,
            oldw: Int,
            oldh: Int,
        ) {
            super.onSizeChanged(w, h, oldw, oldh)
            viewWidth = w
            viewHeight = h
            if (w <= 0 || h <= 0) return
            cx = w / 2f
            // Geometric center (was h/2 - 0.1*h which shifted ball upward by 10% of view height)
            cy = h / 2f
            val minDim = min(w, h).toFloat()
            val maxDiamPx = resources.getDimension(R.dimen.magic_ball_max_diameter)
            val radiusFromWidth = minDim * 0.4f
            val maxRadiusFromCap = maxDiamPx / 2f
            val maxRadiusFromHeight = minDim * 0.38f
            val rBase = min(min(radiusFromWidth, maxRadiusFromCap), maxRadiusFromHeight)
            val pctRaw = resources.getInteger(R.integer.front_ball_radius_percent)
            val pct = pctRaw.coerceIn(70, 200)
            var r = rBase * (pct / 100f)
            val maxFit = min(w / 2f, h / 2f)
            outerRadius = min(min(r, maxRadiusFromCap), maxFit)
            val swDp = resources.configuration.smallestScreenWidthDp
            val orient =
                when (resources.configuration.orientation) {
                    Configuration.ORIENTATION_LANDSCAPE -> "landscape"
                    Configuration.ORIENTATION_PORTRAIT -> "portrait"
                    else -> "other(${resources.configuration.orientation})"
                }
            val limiter =
                when {
                    outerRadius >= maxFit - 0.5f -> "maxFit=min(w/2,h/2)"
                    outerRadius >= maxRadiusFromCap - 0.5f -> "maxRadiusCap(magic_ball_max_diameter/2)"
                    else -> "rBase*percent"
                }
            Log.i(
                "MagicBallScale",
                "FrontView | swDp=$swDp orient=$orient view=${w}x$h minDim=$minDim | " +
                    "radiusFromW(minDim*0.4)=$radiusFromWidth maxH(minDim*0.38)=$maxRadiusFromHeight " +
                    "cap=$maxRadiusFromCap | rBase=$rBase | front_pct raw=$pctRaw used=$pct -> rAfterPct=$r | " +
                    "maxFit=$maxFit | outerRadius=$outerRadius LIMITER=$limiter",
            )
            reflectRadius = outerRadius * 0.95f
            innerRadius = outerRadius * 0.425f
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
            ensureTypeface()
            // Text size from inner white circle radius (base 1.05 × innerRadius, then ×1.4 vs original stroke-8)
            eightPaint.textSize = innerRadius * 1.05f * 1.4f
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (viewWidth <= 0) return
            canvas.drawCircle(cx, cy, outerRadius, blackPaint)
            canvas.drawCircle(cx, cy, innerRadius, whitePaint)
            val eight = "8"
            ensureTypeface()
            val fm = eightPaint.fontMetrics
            val textY = cy - (fm.ascent + fm.descent) / 2f
            canvas.drawText(eight, cx, textY, eightPaint)
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
    }
