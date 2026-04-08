/*
 * TheClassicMagicBall - Android Magic 8 Ball Simulator
 * Copyright (C) 2014 DAISSUE
 */
package net.gerosyab.magicball.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import kotlin.math.pow

class Shaker(
    context: Context,
    private val callback: Callback?,
) {
    private val thresholdValue = 1.9
    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var timeCheckpoint: Long = 0
    private val threshold =
        thresholdValue.pow(2) * SensorManager.GRAVITY_EARTH.pow(2)
    private val intervalMs = 500L
    private var resultantForce = 0.0
    var sx: Float = 0f
        private set
    var sy: Float = 0f
        private set
    var sz: Float = 0f
        private set

    private val listener: SensorEventListener =
        object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                if (e.sensor.type != Sensor.TYPE_ACCELEROMETER) return
                sx = e.values[0]
                sy = e.values[1]
                sz = e.values[2]
                resultantForce = (sx * sx + sy * sy + sz * sz).toDouble()
                if (threshold < resultantForce) {
                    isShaking()
                } else {
                    isNotShaking()
                }
            }

            override fun onAccuracyChanged(
                sensor: Sensor?,
                accuracy: Int,
            ) = Unit
        }

    fun open() {
        MyLog.d("Shaker", "Shaker open()")
        sensorManager.registerListener(
            listener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI,
        )
    }

    fun close() {
        MyLog.d("Shaker", "Shaker close()")
        sensorManager.unregisterListener(listener)
    }

    private fun isShaking() {
        timeCheckpoint = SystemClock.elapsedRealtime()
    }

    private fun isNotShaking() {
        val curTime = SystemClock.elapsedRealtime()
        if (timeCheckpoint > 0 && curTime - timeCheckpoint > intervalMs) {
            timeCheckpoint = 0
            callback?.onShakingDetected()
        }
    }

    fun interface Callback {
        fun onShakingDetected()
    }
}
