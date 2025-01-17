package com.example.myapplication.presentation

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import com.example.myapplication.R

class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        val wakeLock = powerManager.newWakeLock(
            android.os.PowerManager.SCREEN_BRIGHT_WAKE_LOCK or android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "MyApp:AlarmWakeLock"
        )
        wakeLock.acquire(10 * 1000L)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator?.vibrate(VibrationEffect.createOneShot(60000, VibrationEffect.DEFAULT_AMPLITUDE))

        mediaPlayer = MediaPlayer.create(this, R.raw.google_duo)
        mediaPlayer?.start()

        wakeLock.release()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        vibrator?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
