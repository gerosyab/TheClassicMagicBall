/*
 *     TheClassicMagicBall - Android Magic 8 Ball Simulator
 *     Copyright (C) 2014 gerosyab
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.gerosyab.magicball.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

public class Shaker {
	private final double thresholdValue = 1.9d;
	private SensorManager mSensorManager;
	private long mTimeCheckpoint;
	private double mThreshold = Math.pow(thresholdValue, 2) * Math.pow(SensorManager.GRAVITY_EARTH, 2);
	private long mInterval = 500;
	private double resultantForce;
	private float sx;
	private float sy;
	private float sz;
	private Callback mCallBack;

	public Shaker(Context context, Callback callBack) {
		this.mCallBack = callBack;
		this.mTimeCheckpoint = 0;

		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
	}

	private SensorEventListener listener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent e) {
			if (e.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				
				sx = e.values[0];
				sy = e.values[1];
				sz = e.values[2];
				
				resultantForce = (sx * sx) + (sy * sy) + (sz * sz);
				
				if (mThreshold < resultantForce) {
					isShaking();
				} else {
					isNotShaking();
				}
			}
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	public void open(){
		MyLog.d("Shaker", "Shaker open()");
		mSensorManager.registerListener(listener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
	}
	
	public void close() {
		MyLog.d("Shaker", "Shaker close()");
		mSensorManager.unregisterListener(listener);
	}

	private void isShaking() {
		mTimeCheckpoint = SystemClock.elapsedRealtime();
	}

	private void isNotShaking() {
		long curTime = SystemClock.elapsedRealtime();

		if (mTimeCheckpoint > 0) {
			if (curTime - mTimeCheckpoint > mInterval) {
				mTimeCheckpoint = 0;
				if (mCallBack != null) {
					mCallBack.onShakingDetected();
				}
			}
		}
	}
	
	public float getSx(){
		return sx;
	}
	
	public float getSy(){
		return sy;
	}
	
	public float getSz(){
		return sz;
	}
	
	public interface Callback {
		void onShakingDetected();
	}
}