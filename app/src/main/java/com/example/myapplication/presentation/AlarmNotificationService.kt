package com.example.myapplication.presentation

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import android.util.Log
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status.Builder
import androidx.wear.ongoing.Status.TextPart


class AlarmNotificationService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val NOTIFICATION_ID = 123 // Unikalne ID powiadomienia
    private val NOTIFICATION_CHANNEL_ID = "timer_alarm_channel"
    private val NOTIFICATION_CHANNEL_NAME = "timer alarm channel name"

    override fun onCreate() {
        super.onCreate()
        Log.d("AlarmNotificationService", "Service created.")
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AlarmNotificationService", "onStartCommand received. Building notification.")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        startAlarmSoundAndVibration()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Log.d("AlarmNotificationService", "Service destroyed. Stopping alarm.")
        super.onDestroy()
        stopAlarmSoundAndVibration()
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notification for timer"
                enableVibration(true)
                setSound(Settings.System.DEFAULT_ALARM_ALERT_URI,
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("AlarmNotificationService", "Notification channel created.")
        }
    }

    private fun buildNotification(): Notification {
        val fullScreenIntent = Intent(this, FullScreenAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0, // Request code
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        Log.d("AlarmNotificationService", "FullScreen PendingIntent created.")

        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val contentPendingIntent = PendingIntent.getActivity(
            this,
            1,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        Log.d("AlarmNotificationService", "Content PendingIntent created with flags: ${contentIntent.flags}")

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.football_13302)
            .setContentTitle("Time is up!")
            .setContentText("Tap to open")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        }
        Log.d("AlarmNotificationService", "Notification built.")
        return builder.build()
    }

    private fun startAlarmSoundAndVibration() {
        Log.d("AlarmNotificationService", "Starting sound and vibration.")
        try {
            val alarmSound = Settings.System.DEFAULT_ALARM_ALERT_URI
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmNotificationService, alarmSound)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setAudioAttributes(audioAttributes)
                isLooping = true
                prepare()
                start()
            }
            Log.d("AlarmNotificationService", "MediaPlayer started.")
        } catch (e: Exception) {
            Log.e("AlarmNotificationService", "Error playing alarm sound: ${e.message}")
            mediaPlayer = null
        }

        if (vibrator?.hasVibrator() == true) {
            val pattern = longArrayOf(0, 1000, 1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(pattern, 0)
                vibrator?.vibrate(effect)
                Log.d("AlarmNotificationService", "Vibration started (Waveform).")
            } else {
                vibrator?.vibrate(pattern, 0)
                Log.d("AlarmNotificationService", "Vibration started (Pattern).")
            }
        } else {
            Log.w("AlarmNotificationService", "Vibrator not available.")
        }
    }

    private fun stopAlarmSoundAndVibration() {
        Log.d("AlarmNotificationService", "Stopping sound and vibration.")
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        vibrator?.cancel()
    }
}