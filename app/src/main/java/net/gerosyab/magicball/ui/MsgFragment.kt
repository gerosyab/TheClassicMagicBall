/*
 * TheClassicMagicBall - Android Magic 8 Ball Simulator
 * Copyright (C) 2014 DAISSUE
 */
package net.gerosyab.magicball.ui

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import net.gerosyab.magicball.R
import net.gerosyab.magicball.databinding.MsgFragmentBinding
import net.gerosyab.magicball.util.MyLog
import net.gerosyab.magicball.util.MyRandom

class MsgFragment : Fragment() {
    private var _binding: MsgFragmentBinding? = null
    private val binding get() = _binding!!

    private val mainHandler = Handler(Looper.getMainLooper())

    private val tabletScale: Float
        get() = arguments?.getFloat(ARG_TABLET_SCALE) ?: 1f

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

    private fun refreshBottomChromeFromTicker() {
        val b = _binding ?: return
        val ctx = context ?: return
        b.buttonHintCenter.text = BottomChromeTicker.hintLabel(ctx)
        b.secondaryTextSwitcher.setText(BottomChromeTicker.secondaryText(secondaryMessages))
    }

    private val bottomChromeListener: () -> Unit = { refreshBottomChromeFromTicker() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        MyLog.d("MsgFragment", "onCreateView")
        _binding = MsgFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.msgview.tabletScaleFactor = tabletScale
        binding.msgview.setMsgIdx(MyRandom.getNum())

        val ctx = requireContext()
        val fadeIn = AnimationUtils.loadAnimation(ctx, android.R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(ctx, android.R.anim.fade_out)
        binding.secondaryTextSwitcher.setFactory {
            TextView(ctx).apply {
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(Color.WHITE)
            }
        }
        binding.secondaryTextSwitcher.inAnimation = fadeIn
        binding.secondaryTextSwitcher.outAnimation = fadeOut

        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.buttonCapture.setOnClickListener {
            captureMsgArea()
        }
        binding.buttonHintCenter.setOnClickListener {
            setNewMessage()
        }
    }

    fun setNewMessage() {
        MyLog.d("MsgFragment", "setNewMessage")
        (activity as? MainActivity)?.vibrateShort()
        val mv = _binding?.msgview ?: return
        mv.setMsgIdx(MyRandom.getNum())
        mv.notifyMsgChanged()
    }

    override fun onResume() {
        super.onResume()
        BottomChromeTicker.addListener(bottomChromeListener)
    }

    override fun onPause() {
        BottomChromeTicker.removeListener(bottomChromeListener)
        super.onPause()
    }

    private fun captureMsgArea() {
        val v = binding.msgview
        if (v.width <= 0 || v.height <= 0) {
            Toast
                .makeText(requireContext(), R.string.capture_failed, Toast.LENGTH_SHORT)
                .show()
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Toast
                .makeText(requireContext(), R.string.capture_requires_api, Toast.LENGTH_SHORT)
                .show()
            return
        }
        val window = requireActivity().window
        val loc = IntArray(2)
        v.getLocationInWindow(loc)
        val rect =
            Rect(
                loc[0],
                loc[1],
                loc[0] + v.width,
                loc[1] + v.height,
            )
        val bitmap =
            Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        PixelCopy.request(
            window,
            rect,
            bitmap,
            { result ->
                if (result == PixelCopy.SUCCESS) {
                    savePngToGallery(bitmap)
                } else {
                    bitmap.recycle()
                    Toast
                        .makeText(requireContext(), R.string.capture_failed, Toast.LENGTH_SHORT)
                        .show()
                }
            },
            mainHandler,
        )
    }

    private fun savePngToGallery(bitmap: Bitmap) {
        val name =
            "magicball_" +
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) +
                ".png"
        val resolver = requireContext().contentResolver
        val contentValues =
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/MagicBall",
                    )
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val uri = resolver.insert(collection, contentValues) ?: run {
            bitmap.recycle()
            Toast
                .makeText(requireContext(), R.string.capture_failed, Toast.LENGTH_SHORT)
                .show()
            return
        }
        try {
            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            } ?: run {
                bitmap.recycle()
                Toast
                    .makeText(requireContext(), R.string.capture_failed, Toast.LENGTH_SHORT)
                    .show()
                return
            }
        } catch (_: Exception) {
            bitmap.recycle()
            Toast
                .makeText(requireContext(), R.string.capture_failed, Toast.LENGTH_SHORT)
                .show()
            return
        }
        bitmap.recycle()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }
        Toast
            .makeText(requireContext(), R.string.capture_saved, Toast.LENGTH_SHORT)
            .show()
    }

    override fun onDestroyView() {
        BottomChromeTicker.removeListener(bottomChromeListener)
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_TABLET_SCALE = "tablet_scale"

        fun newInstance(tabletScaleFactor: Float): MsgFragment =
            MsgFragment().apply {
                arguments = bundleOf(ARG_TABLET_SCALE to tabletScaleFactor)
            }
    }
}
