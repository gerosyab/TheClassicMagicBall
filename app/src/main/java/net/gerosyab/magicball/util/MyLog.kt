/*
 * TheClassicMagicBall - Android Magic 8 Ball Simulator
 * Copyright (C) 2014 DAISSUE
 */
package net.gerosyab.magicball.util

import android.util.Log
import net.gerosyab.magicball.data.Const

object MyLog {
    fun i(tag: String, message: String) {
        if (Const.DEBUG) Log.i(tag, message)
    }

    fun w(tag: String, message: String) {
        if (Const.DEBUG) Log.w(tag, message)
    }

    fun d(tag: String, message: String) {
        if (Const.DEBUG) Log.d(tag, message)
    }

    fun e(tag: String, message: String) {
        if (Const.DEBUG) Log.e(tag, message)
    }
}
