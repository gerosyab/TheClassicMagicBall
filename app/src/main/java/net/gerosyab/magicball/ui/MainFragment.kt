/*
 * TheClassicMagicBall - Android Magic 8 Ball Simulator
 * Copyright (C) 2014 DAISSUE
 */
package net.gerosyab.magicball.ui

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.util.Linkify
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import java.io.ByteArrayOutputStream
import net.gerosyab.magicball.R
import net.gerosyab.magicball.databinding.MainFragmentBinding
import net.gerosyab.magicball.util.MyLog

class MainFragment : Fragment() {
    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!

    private var activityRef: MainActivity? = null

    private var isBallTouched = false

    private fun refreshBottomChromeFromTicker() {
        val b = _binding ?: return
        val ctx = context ?: return
        b.hintActionButton.text = BottomChromeTicker.hintLabel(ctx)
        b.secondaryTextSwitcher.setText(BottomChromeTicker.secondaryText(secondaryMessages))
    }

    private val bottomChromeListener: () -> Unit = { refreshBottomChromeFromTicker() }

    private val secondaryMessages: Array<String> by lazy {
        arrayOf(
            getString(R.string.msg_miss_me),
            getString(R.string.msg_boring),
            getString(R.string.msg_ask_me),
            getString(R.string.msg_and),
            getString(R.string.msg_find_answer),
            getString(R.string.msg_but),
            getString(R.string.msg_do_not_trust),
            getString(R.string.msg_might_be_wrong),
            getString(R.string.msg_sometimes),
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        MyLog.d("MainFragment", "onCreateView")
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()
        val fadeIn = AnimationUtils.loadAnimation(ctx, android.R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(ctx, android.R.anim.fade_out)

        binding.secondaryTextSwitcher.setFactory {
            makeSecondaryTextView(ctx)
        }
        binding.secondaryTextSwitcher.inAnimation = fadeIn
        binding.secondaryTextSwitcher.outAnimation = fadeOut

        binding.infoText.setOnClickListener { showInfoDialog() }

        binding.hintActionButton.setOnClickListener {
            activityRef?.openMsgFromPill()
        }

        binding.frontview.setOnTouchListener { _, event ->
            val fv = binding.frontview
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isInsideBall(event.x, event.y, fv)) {
                        isBallTouched = true
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (isBallTouched &&
                        isInsideBall(event.x, event.y, fv)
                    ) {
                        isBallTouched = false
                        activityRef?.onBallTouched()
                    }
                }
            }
            true
        }
    }

    private fun makeSecondaryTextView(ctx: android.content.Context): TextView {
        return TextView(ctx).apply {
            gravity = Gravity.CENTER
            textSize = 20f
            setTextColor(Color.WHITE)
        }
    }

    private fun isInsideBall(
        x: Float,
        y: Float,
        fv: net.gerosyab.magicball.ui.view.FrontView,
    ): Boolean {
        val dx = x - fv.cx
        val dy = y - fv.cy
        val r = fv.radius
        return dx * dx + dy * dy <= r * r
    }

    private fun showInfoDialog() {
        val message =
            try {
                resources.openRawResource(R.raw.info).use { input ->
                    ByteArrayOutputStream().use { bos ->
                        input.copyTo(bos)
                        bos.toString(Charsets.UTF_8.name())
                    }
                }
            } catch (_: Exception) {
                return
            }
        val s = SpannableString(message)
        Linkify.addLinks(s, Linkify.WEB_URLS)
        val dialog =
            AlertDialog.Builder(requireContext())
                .setTitle("Information")
                .setMessage(s)
                .create()
        dialog.show()
        dialog.findViewById<TextView>(android.R.id.message)?.textSize = 12f
    }

    override fun onAttach(context: android.content.Context) {
        MyLog.d("MainFragment", "onAttach")
        super.onAttach(context)
        activityRef = context as? MainActivity
    }

    override fun onDetach() {
        activityRef = null
        super.onDetach()
    }

    override fun onResume() {
        MyLog.d("MainFragment", "onResume")
        super.onResume()
        BottomChromeTicker.addListener(bottomChromeListener)
    }

    override fun onPause() {
        BottomChromeTicker.removeListener(bottomChromeListener)
        super.onPause()
    }

    override fun onDestroyView() {
        BottomChromeTicker.removeListener(bottomChromeListener)
        _binding = null
        super.onDestroyView()
    }
}
