package com.example.myapplication.presentation

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R
import java.time.LocalDateTime

class StopFragment : Fragment() {

    private lateinit var stopLayout: View
    private lateinit var confirmationStopLayout: View
    private lateinit var sharedScreenshotViewModel: SharedScreenshotViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.stop_fragment, container, false)

        stopLayout = view.findViewById(R.id.stop_layout)
        confirmationStopLayout = view.findViewById(R.id.confirmation_stop_layout)

        sharedScreenshotViewModel = ViewModelProvider(requireActivity()).get(SharedScreenshotViewModel::class.java)

        val stopButton: Button = view.findViewById(R.id.stopButton)
        stopButton.setOnClickListener {
            showConfirmationStopDialog(stopLayout, confirmationStopLayout)
        }

        return view
    }

    private fun showConfirmationStopDialog(stopLayout: View, confirmationStopLayout: View) {
        showDialog(stopLayout, confirmationStopLayout)

        val cancelButton =
            confirmationStopLayout.findViewById<TextView>(R.id.stop_confirmation_cancel_button)
        val confirmButton =
            confirmationStopLayout.findViewById<TextView>(R.id.stop_confirmation_confirm_button)

        cancelButton.setOnClickListener {
            hideDialog(confirmationStopLayout, stopLayout)
        }

        confirmButton.setOnClickListener {
            saveScreenshotToMediaStore(sharedScreenshotViewModel.bitmap.value!!)
            restartApp()
        }
    }

    private fun showDialog(stopLayout: View, confirmationStopLayout: View) {
        stopLayout.visibility = View.GONE
        confirmationStopLayout.visibility = View.VISIBLE
    }

    private fun hideDialog(confirmationStopLayout: View, stopLayout: View) {
        confirmationStopLayout.visibility = View.GONE
        stopLayout.visibility = View.VISIBLE
    }

    private fun restartApp() {
        val intent = Intent(activity, TimerSetupActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun saveScreenshotToMediaStore(bitmap: Bitmap) {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "${LocalDateTime.now()}_screenshot_summary.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Screenshots")
            }

            val resolver = context?.contentResolver
            val uri = resolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it).use { outputStream ->
                    if (outputStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    } else {
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ScreenshotError", "Failed to save screenshot: ${e.message}", e)
        }
    }
}

