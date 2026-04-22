/*
 * TheClassicMagicBall - Android Magic 8 Ball Simulator
 * Copyright (C) 2014 DAISSUE
 */
package net.gerosyab.magicball.ui

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import net.gerosyab.magicball.BuildConfig
import net.gerosyab.magicball.R
import net.gerosyab.magicball.data.Const
import net.gerosyab.magicball.databinding.ActivityMainBinding
import net.gerosyab.magicball.util.MyLog
import net.gerosyab.magicball.util.Shaker

class MainActivity :
    AppCompatActivity(),
    Shaker.Callback {
    private lateinit var binding: ActivityMainBinding
    private var mBackKeyFlag = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!resources.getBoolean(R.bool.allow_rotation)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.motherLinear) { v, windowInsets ->
            val bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        if (BuildConfig.ADS_ENABLED) {
            MobileAds.initialize(this) {}
            binding.adView.loadAd(AdRequest.Builder().build())
        } else {
            binding.adBannerSlot.visibility = View.GONE
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.content_frame, MainFragment())
            }
        }
    }

    override fun onResume() {
        MyLog.d("MainActivity", "onResume")
        super.onResume()
        if (BuildConfig.ADS_ENABLED) binding.adView.resume()
        if (shaker == null) {
            shaker = Shaker(applicationContext, this)
        }
        shaker?.open()
    }

    override fun onPause() {
        MyLog.d("MainActivity", "onPause")
        if (BuildConfig.ADS_ENABLED) binding.adView.pause()
        shaker?.close()
        super.onPause()
    }

    override fun onDestroy() {
        MyLog.d("MainActivity", "onDestroy")
        if (BuildConfig.ADS_ENABLED) binding.adView.destroy()
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        MyLog.d(
            "MainActivity",
            "onBackPressed getSupportFragmentManager().getBackStackEntryCount() " +
                supportFragmentManager.backStackEntryCount,
        )
        if (supportFragmentManager.backStackEntryCount > 0) {
            MyLog.d("MainActivity", "onBackPressed popBackStack")
            supportFragmentManager.popBackStack()
            return
        }
        if (!mBackKeyFlag) {
            MyLog.d("MainActivity", "onBackPressed !mBackKeyFlag")
            Toast
                .makeText(
                    applicationContext,
                    R.string.exit_press_again,
                    Toast.LENGTH_SHORT,
                ).show()
            mBackKeyFlag = true
            handler.postDelayed({ mBackKeyFlag = false }, 2000)
        } else {
            super.onBackPressed()
        }
    }

    override fun onShakingDetected() {
        MyLog.d(
            "MainActivity",
            "onShakingDetected backStack=" + supportFragmentManager.backStackEntryCount,
        )
        if (supportFragmentManager.backStackEntryCount > 0) {
            // MsgFragment.setNewMessage() handles vibration itself
            (supportFragmentManager.findFragmentById(R.id.content_frame) as? MsgFragment)
                ?.setNewMessage()
        } else {
            vibrateShort()
            supportFragmentManager.commit {
                replace(R.id.content_frame, MsgFragment.newInstance(tabletScaleFactor = 1f))
                addToBackStack(null)
            }
        }
    }

    fun onBallTouched() {
        onShakingDetected()
    }

    fun openMsgFromPill() {
        vibrateShort()
        if (supportFragmentManager.backStackEntryCount > 0) {
            return
        }
        supportFragmentManager.commit {
            replace(R.id.content_frame, MsgFragment.newInstance(tabletScaleFactor = 1f))
            addToBackStack(null)
        }
    }

    fun vibrateShort() {
        val duration = Const.VIB_TIME_MS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val v = vm.defaultVibrator
            v.vibrate(
                VibrationEffect.createOneShot(
                    duration,
                    VibrationEffect.DEFAULT_AMPLITUDE,
                ),
            )
        } else {
            @Suppress("DEPRECATION")
            val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(
                    VibrationEffect.createOneShot(
                        duration,
                        VibrationEffect.DEFAULT_AMPLITUDE,
                    ),
                )
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(duration)
            }
        }
    }

    companion object {
        @Volatile
        @JvmField
        var shaker: Shaker? = null

        @JvmStatic
        fun getShakerInstance(): Shaker? = shaker
    }
}
