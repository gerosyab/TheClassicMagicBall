/*
 * TheClassicMagicBall - Android Magic 8 Ball Simulator
 * Copyright (C) 2014 DAISSUE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 */
package net.gerosyab.magicball.data

import net.gerosyab.magicball.R

object Const {
    @JvmField
    var DEBUG: Boolean = true

    @JvmField
    var VIEW_DEBUG: Boolean = false

    const val TAG: String = "magicball"
    const val VIB_TIME_MS: Long = 300

    @JvmField
    val MSG_IDS: IntArray =
        intArrayOf(
            R.drawable.msg01,
            R.drawable.msg02,
            R.drawable.msg03,
            R.drawable.msg04,
            R.drawable.msg05,
            R.drawable.msg06,
            R.drawable.msg07,
            R.drawable.msg08,
            R.drawable.msg09,
            R.drawable.msg10,
            R.drawable.msg11,
            R.drawable.msg12,
            R.drawable.msg13,
            R.drawable.msg14,
            R.drawable.msg15,
            R.drawable.msg16,
            R.drawable.msg17,
            R.drawable.msg18,
            R.drawable.msg19,
            R.drawable.msg20,
        )

    @JvmStatic
    fun setViewDebuggingMode(status: Boolean) {
        VIEW_DEBUG = status
    }
}
