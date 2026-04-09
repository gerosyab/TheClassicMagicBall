/*
 * TheClassicMagicBall - Android Magic 8 Ball Simulator
 * Copyright (C) 2014 DAISSUE
 */
package net.gerosyab.magicball.ui

import android.content.Context
import net.gerosyab.magicball.R

/** Cycles: TOUCH ME → SHAKE ME → TRY ME (every step of the outer timer). */
object HintRotation {
    const val PHASE_COUNT = 3

    fun label(
        context: Context,
        phase: Int,
    ): String =
        when (phase % PHASE_COUNT) {
            0 -> context.getString(R.string.hint_touch_me)
            1 -> context.getString(R.string.hint_shake_me)
            else -> context.getString(R.string.hint_try_me)
        }
}
