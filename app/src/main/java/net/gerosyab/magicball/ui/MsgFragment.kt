/*
 * TheClassicMagicBall - Android Magic 8 Ball Simulator
 * Copyright (C) 2014 DAISSUE
 */
package net.gerosyab.magicball.ui

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.SpannableString
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import java.io.ByteArrayOutputStream
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
        binding.msgview.holder.setFormat(PixelFormat.TRANSPARENT)
        binding.msgview.tabletScaleFactor = tabletScale
        binding.msgview.setMsgIdx(MyRandom.getNum())

        binding.infoText.setOnClickListener { showInfoDialog() }

        val tablet = resources.getBoolean(R.bool.is_tablet_layout)
        binding.buttonBack.isVisible = !tablet
        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.buttonCapture.setOnClickListener {
            captureMsgArea()
        }
    }

    fun applyTabletScale(scale: Float) {
        arguments = bundleOf(ARG_TABLET_SCALE to scale)
        _binding?.msgview?.tabletScaleFactor = scale
    }

    fun setNewMessage() {
        MyLog.d("MsgFragment", "setNewMessage")
        val mv = _binding?.msgview ?: return
        mv.setMsgIdx(MyRandom.getNum())
        mv.notifyMsgChanged()
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
        Linkify.addLinks(s, Linkify.ALL)
        val dialog =
            AlertDialog.Builder(requireContext())
                .setTitle("Information")
                .setMessage(s)
                .create()
        dialog.show()
        dialog.findViewById<TextView>(android.R.id.message)?.textSize = 14f
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
