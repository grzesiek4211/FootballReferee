package com.example.myapplication.presentation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.DATE_ADDED
import android.provider.MediaStore.Images.Media.RELATIVE_PATH
import android.provider.MediaStore.Images.Media._ID
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R
import com.example.myapplication.presentation.MainActivity.Companion.ONGOING_NOTIFICATION_ID
import com.example.myapplication.presentation.TimerFragment.Companion.TIMER_REQUEST_CODE
import java.time.LocalDateTime

private const val SCREENSHOTS_LOCATION = "Pictures/Football_Referee"
private const val WEEK_IN_SECS = 7 * 24 * 60 * 60L

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
            cancelTimerAndAlarm()
            val notificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.cancel(ONGOING_NOTIFICATION_ID)
            removeScreenshotsOlderThan(WEEK_IN_SECS)
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
                put(MediaStore.Images.Media.DISPLAY_NAME, "${LocalDateTime.now()}_summary.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(RELATIVE_PATH, SCREENSHOTS_LOCATION)
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

    private fun removeScreenshotsOlderThan(secs: Long) {
        val resolver = context?.contentResolver ?: return

        val oneWeekAgoInSeconds = System.currentTimeMillis() / 1000 - secs

        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(_ID, DATE_ADDED, RELATIVE_PATH)

        val selection = "$RELATIVE_PATH LIKE ? AND $DATE_ADDED < ?"
        val selectionArgs = arrayOf("%$SCREENSHOTS_LOCATION%", oneWeekAgoInSeconds.toString())
        val cursor = resolver.query(uri, projection, selection, selectionArgs, null)

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(_ID)
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val imageUri = ContentUris.withAppendedId(uri, id)

                try {
                    val rows = resolver.delete(imageUri, null, null)
                    Log.d("ScreenshotCleanup", "Deleted URI: $imageUri (rows: $rows)")
                } catch (e: SecurityException) {
                    Log.e("ScreenshotCleanup", "SecurityException deleting $imageUri", e)
                } catch (e: Exception) {
                    Log.e("ScreenshotCleanup", "Error deleting $imageUri", e)
                }
            }
        }
    }

    private fun cancelTimerAndAlarm() {
        val alarmServiceIntent = Intent(requireContext(), AlarmNotificationService::class.java)
        requireContext().stopService(alarmServiceIntent)

        val alarmIntent = Intent(requireContext(), TimerExpiredReceiver::class.java).apply {
            action = TimerExpiredReceiver.ACTION_TIMER_EXPIRED
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            TIMER_REQUEST_CODE,
            alarmIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        pendingIntent?.let {
            alarmManager.cancel(it)
        }
    }

}

