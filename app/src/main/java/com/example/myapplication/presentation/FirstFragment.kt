package com.example.myapplication.presentation


import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirstFragment() : Fragment() {

    companion object {
        fun newInstance(timerDuration: Long) = FirstFragment().apply {
            arguments = Bundle().apply {
                putLong("TIMER_DURATION", timerDuration)
            }
        }

            private const val REQUEST_CODE_NOTIFICATION = 101
    }

    private lateinit var currentTimeTextView: TextView
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var timerTextView: TextView
    private lateinit var buttonPausePlay: Button
    private lateinit var buttonRestart: Button
    private lateinit var timer: CountDownTimer
    private var isPaused = false
    private var initialTimerValue: Long = 5 * 60 * 1000
    private var timeRemaining: Long = initialTimerValue// = INITIAL_TIMER_VALUE


    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null


    // Runnable to update time every second
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 1000) // Re-run every second
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        arguments?.let {
            timeRemaining = it.getLong("TIMER_DURATION", 5 * 60 * 1000)
            initialTimerValue = it.getLong("TIMER_DURATION", 5 * 60 * 1000)
        }
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_first, container, false)

        // Initialize the TextView for displaying time
        currentTimeTextView = view.findViewById(R.id.current_time)

        timerTextView = view.findViewById(R.id.timerTextView)
        buttonPausePlay = view.findViewById(R.id.button_pause_play)
        buttonRestart = view.findViewById(R.id.button_restart)

        // Initialize Timer
        initializeTimer()
        startTimer(timeRemaining)

        // Pause/Play Button
        buttonPausePlay.setOnClickListener {
            if (isPaused) {
                startTimer(timeRemaining)
                buttonPausePlay.text = "⏸" // Set to pause icon
                isPaused = false
            } else {
                if (::timer.isInitialized) {
                    timer.cancel()
                }
                buttonPausePlay.text = "▶️" // Set to play icon
                isPaused = true
            }
            stopAlarm()
        }

        // Restart Button
        buttonRestart.setOnClickListener {
            if (::timer.isInitialized) {
                timer.cancel()
            }
            stopAlarm()
            timeRemaining = initialTimerValue // Reset to 5 minutes
            startTimer(timeRemaining)
            buttonPausePlay.text = "⏸" // Set to pause icon
            isPaused = false
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateTimeRunnable) // Start updating time when fragment is visible
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateTimeRunnable) // Stop updating time when fragment is not visible
    }

    // Function to update the time displayed in the TextView
    private fun updateTime() {
        val currentTime = timeFormat.format(Date())
        currentTimeTextView.text = currentTime
    }

    private fun initializeTimer() {
        val minutes = timeRemaining / 1000 / 60
        val seconds = (timeRemaining / 1000) % 60
        timerTextView.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun startTimer(timeMillis: Long) {
        timer = object : CountDownTimer(timeMillis, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                timerTextView.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                timerTextView.text = "00:00"
                triggerAlarm()
                sendTimerExpiredNotification()
            }
        }
        timer.start()


    }
//    private fun sendTimerExpiredNotification() {
//        val context = requireContext()
//
//         Start the foreground service when the timer expires
//        val serviceIntent = Intent(context, TimerExpiredService::class.java)
//        context.startService(serviceIntent)

        /*
        val context = requireContext()
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // Create an intent to open your app's main activity
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            // Create a pending intent that will open the app when the notification is clicked
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Build the notification
            val notification = NotificationCompat.Builder(context, "your_channel_id")
                .setSmallIcon(R.drawable.splash_icon) // Ensure this icon exists
                .setContentTitle("Timer Expired")
                .setContentText("The timer has reached 0:00")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent) // Set the intent to open the app
                .build()

            // Show the notification
            with(NotificationManagerCompat.from(context)) {
                notify(1, notification)
            }
        } else {
            // Request permission if not already granted
            requestNotificationPermission()
        }*/
//    }

    fun sendTimerExpiredNotification() {

        val context = requireContext()
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, send the notification
            with(NotificationManagerCompat.from(context)) {
                notify(1, buildNotification().build())  // Call build() to get Notification
            }
        } else {
            // Permission is not granted, request it
            requestNotificationPermission()
        }


    }

    private fun buildNotification(): NotificationCompat.Builder {
        val context = requireContext()
        return NotificationCompat.Builder(context, "your_channel_id")
            .setSmallIcon(R.drawable.splash_icon) // Make sure you have this icon
            .setContentTitle("Timer Expired")
            .setContentText("The timer has reached 0:00")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

    }

    private fun requestNotificationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            REQUEST_CODE_NOTIFICATION
        )
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.SYSTEM_ALERT_WINDOW),
            REQUEST_CODE_NOTIFICATION
        )
    }

    private fun triggerAlarm() {
        val requireContext = requireContext()
        vibrator = requireContext.getSystemService(Vibrator::class.java)
        vibrator?.vibrate(VibrationEffect.createOneShot(60000, VibrationEffect.DEFAULT_AMPLITUDE))
        mediaPlayer = MediaPlayer.create(requireContext, R.raw.google_duo)
        mediaPlayer?.start()
    }

    private fun stopAlarm() {
        // Stop vibration
        vibrator?.cancel()

        // Stop the sound
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::timer.isInitialized) {
            timer.cancel()
        }
        stopAlarm()
    }
}
