/*
 * TheClassicMagicBall - Android Magic 8 Ball Simulator
 * Copyright (C) 2014 DAISSUE
 */
package net.gerosyab.magicball.util

import java.util.Random
import net.gerosyab.magicball.data.Const

object MyRandom {
    private val random = Random(System.currentTimeMillis())
    private val history = intArrayOf(-1, -1, -1, -1, -1)
    private var curIndex = -1

    @JvmStatic
    fun getNum(): Int {
        MyLog.d("MyRandom", "history : ${history.joinToString()}")
        MyLog.d("MyRandom", "curIndex : $curIndex")
        curIndex = getNextIndex()
        MyLog.d("MyRandom", "getNextIndex() : $curIndex")
        var result: Int
        var generated: Boolean
        do {
            result = random.nextInt(Const.MSG_IDS.size)
            generated = true
            for (i in history.indices) {
                if (result == history[i]) {
                    generated = false
                    break
                }
            }
        } while (!generated)
        history[curIndex] = result
        return result
    }

    private fun getNextIndex(): Int {
        var nextIndex = curIndex + 1
        if (nextIndex >= history.size) {
            nextIndex = 0
        }
        return nextIndex
    }
}
