/*
 * TheClassicMagicBall - Android Magic 8 Ball Simulator
 * Copyright (C) 2014 DAISSUE
 */
package net.gerosyab.magicball.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Single clock for hint pill + secondary line so Main ⇄ Msg (hide/show) does not reset rotation.
 * Synchronized with the previous per-fragment 1s timer logic.
 */
object BottomChromeTicker {
    /** Must match the secondary message arrays in MainFragment / MsgFragment. */
    const val SECONDARY_MESSAGE_COUNT = 9

    private val mainHandler = Handler(Looper.getMainLooper())
    private var timer: Timer? = null
    private var tickSecond = -1
    var hintStep: Int = 0
        private set
    var secondaryIdx: Int = -1
        private set
    var secondaryBlank: Boolean = true
        private set

    private val listeners = CopyOnWriteArraySet<() -> Unit>()

    fun addListener(listener: () -> Unit) {
        val wasEmpty = listeners.isEmpty()
        listeners.add(listener)
        if (wasEmpty) {
            ensureTimerRunning()
        }
        mainHandler.post { listener.invoke() }
    }

    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
        if (listeners.isEmpty()) {
            timer?.cancel()
            timer = null
        }
    }

    private fun ensureTimerRunning() {
        if (timer != null) return
        timer =
            Timer().apply {
                scheduleAtFixedRate(
                    object : TimerTask() {
                        override fun run() {
                            mainHandler.post { advanceOneSecond() }
                        }
                    },
                    0L,
                    1000L,
                )
            }
    }

    private fun advanceOneSecond() {
        tickSecond++
        if (tickSecond % 2 == 0) {
            hintStep = (hintStep + 1) % HintRotation.PHASE_COUNT
        }
        if (tickSecond % 4 == 3 && !secondaryBlank) {
            secondaryBlank = true
        } else if (tickSecond % 4 == 0 && secondaryBlank) {
            secondaryBlank = false
            secondaryIdx = (secondaryIdx + 1) % SECONDARY_MESSAGE_COUNT
        }
        listeners.forEach { it.invoke() }
    }

    fun hintLabel(context: Context): String = HintRotation.label(context, hintStep)

    fun secondaryText(messages: Array<String>): String =
        if (secondaryBlank || secondaryIdx < 0) {
            ""
        } else {
            messages.getOrElse(secondaryIdx) { "" }
        }
}
