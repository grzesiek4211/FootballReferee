package com.example.myapplication.presentation

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import android.util.Log


class AlarmNotificationService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val NOTIFICATION_ID = 123 // Unikalne ID powiadomienia
    private val NOTIFICATION_CHANNEL_ID = "timer_alarm_channel"
    private val NOTIFICATION_CHANNEL_NAME = "Alarmy Minutnika"

    override fun onCreate() {
        super.onCreate()
        Log.d("AlarmNotificationService", "Service created.")
        // Inicjalizacja vibratora
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AlarmNotificationService", "onStartCommand received. Building notification.")
        createNotificationChannel() // Upewnij się, że kanał istnieje (wymagane od Androida O)
        startForeground(NOTIFICATION_ID, buildNotification()) // Uruchom jako foreground service

        // Rozpocznij wibrację i dźwięk
        startAlarmSoundAndVibration()

        // START_NOT_STICKY - jeśli system zabije serwis, nie próbuj go restartować automatycznie
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Log.d("AlarmNotificationService", "Service destroyed. Stopping alarm.")
        super.onDestroy()
        stopAlarmSoundAndVibration()
        // Zatrzymaj foreground service (usuń powiadomienie)
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        // Wymagane od Androida O (API 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // Ważne: IMPORTANCE_HIGH lub MAX dla fullScreenIntent
            ).apply {
                description = "Powiadomienia dla zakończonych minutników."
                enableVibration(true)
                // Możesz ustawić niestandardowy dźwięk tutaj, jeśli chcesz
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
        // PendingIntent dla fullScreenIntent - uruchomi FullScreenAlarmActivity
        val fullScreenIntent = Intent(this, FullScreenAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Opcjonalnie dodaj extra dane, jeśli FullScreenAlarmActivity ich potrzebuje
            // putExtra("TIMER_ID", timerId)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0, // Request code
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Użyj FLAG_IMMUTABLE
        )
        Log.d("AlarmNotificationService", "FullScreen PendingIntent created.")

        val contentIntent = Intent(this, MainActivity::class.java).apply {
            // *** Zmień flagi, dodając FLAG_ACTIVITY_CLEAR_TOP ***
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            // Możesz też opcjonalnie dodać FLAG_ACTIVITY_SINGLE_TOP, ale launchMode="singleTop" w manifeście jest silniejsze
            // flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val contentPendingIntent = PendingIntent.getActivity(
            this,
            // Użyj innego unikalnego kodu żądania niż dla fullScreenIntent
            // Np. TIMER_REQUEST_CODE + 2 (jeśli fullScreenIntent używa +1)
            1, // Upewnij się, że ten kod jest unikalny w tym PendingIntent.getActivity
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        Log.d("AlarmNotificationService", "Content PendingIntent created with flags: ${contentIntent.flags}")



        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.football_13302) // Upewnij się, że masz taką ikonkę!
            .setContentTitle("Minutnik skończony!")
            .setContentText("Twój czas minął.")
            .setPriority(NotificationCompat.PRIORITY_MAX) // Ustaw najwyższy priorytet
            .setCategory(NotificationCompat.CATEGORY_ALARM) // Kategoria ALARM jest ważna dla budzenia urządzenia
            .setAutoCancel(true) // Powiadomienie znika po kliknięciu
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Domyślne dźwięki i wibracje (można nadpisać)
            // To jest kluczowe: Ustaw fullScreenIntent, który uruchomi Activity
            .setFullScreenIntent(fullScreenPendingIntent, true) // true oznacza wysoki priorytet dla tego intentu
            // Ustaw standardowy PendingIntent dla kliknięcia
            .setContentIntent(contentPendingIntent)

        // Dla Androida S i nowszych, potrzebujesz specjalnego typu foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        }
        Log.d("AlarmNotificationService", "Notification built.")
        return builder.build()
    }

    private fun startAlarmSoundAndVibration() {
        Log.d("AlarmNotificationService", "Starting sound and vibration.")
        // Dźwięk (możesz użyć systemowego dźwięku alarmu)
        try {
            val alarmSound = Settings.System.DEFAULT_ALARM_ALERT_URI
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmNotificationService, alarmSound)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM) // Użyj kategorii ALARM
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setAudioAttributes(audioAttributes)
                isLooping = true // Alarm powinien dzwonić w pętli
                prepare()
                start()
            }
            Log.d("AlarmNotificationService", "MediaPlayer started.")
        } catch (e: Exception) {
            Log.e("AlarmNotificationService", "Error playing alarm sound: ${e.message}")
            mediaPlayer = null // Upewnij się, że obiekt jest null w przypadku błędu
        }


        // Wibracja
        if (vibrator?.hasVibrator() == true) {
            val pattern = longArrayOf(0, 1000, 1000) // Wibracja przez 1s, przerwa 1s, wibracja przez 1s...
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(pattern, 0) // 0 oznacza zapętlanie
                vibrator?.vibrate(effect)
                Log.d("AlarmNotificationService", "Vibration started (Waveform).")
            } else {
                // Dla starszych API
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
        mediaPlayer = null // Zapobiegaj wyciekom pamięci

        vibrator?.cancel()
    }
}