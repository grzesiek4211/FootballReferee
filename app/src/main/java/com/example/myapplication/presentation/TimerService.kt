//package com.example.myapplication.presentation
//
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.Service
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import android.os.CountDownTimer
//import android.os.IBinder
//import android.os.VibrationEffect
//import android.os.Vibrator
//import androidx.core.app.NotificationCompat
//import com.example.myapplication.R
//
//class TimerService : Service() {
//
//    private lateinit var countDownTimer: CountDownTimer
//
//    override fun onCreate() {
//        super.onCreate()
//        // Create the notification channel
//        createNotificationChannel()
//        // Start the service as a foreground service
//        startForeground(1, getNotification())
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        // Retrieve timer duration from the intent or use a default value
//        val timerDuration = intent?.getLongExtra("TIMER_DURATION", 5 * 60 * 1000) ?: 5 * 60 * 1000
//        startTimer(timerDuration)
//        return START_STICKY
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }
//
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = "Timer Service Channel"
//            val descriptionText = "Channel for timer service"
//            val importance = NotificationManager.IMPORTANCE_LOW
//            val channel = NotificationChannel("TimerServiceChannel", name, importance).apply {
//                description = descriptionText
//            }
//            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
//
//    private fun getNotification(): Notification {
//        val builder = NotificationCompat.Builder(this, "TimerServiceChannel")
//            .setContentTitle("Timer Service")
//            .setContentText("Timer is running")
//            .setSmallIcon(R.drawable.splash_icon)
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//        return builder.build()
//    }
//
//    private fun startTimer(durationMillis: Long) {
//        countDownTimer = object : CountDownTimer(durationMillis, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//                timeRemaining = millisUntilFinished
//                val minutes = millisUntilFinished / 1000 / 60
//                val seconds = (millisUntilFinished / 1000) % 60
//                timerTextView.text = String.format("%02d:%02d", minutes, seconds)
//            }
//
//            override fun onFinish() {
//                handleTimerExpired()
//            }
//        }.start()
//    }
//
//    private fun handleTimerExpired() {
//        // Vibrate when timer expires
//        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
//        } else {
//            @Suppress("DEPRECATION")
//            vibrator.vibrate(1000)
//        }
//
//        // Notify user
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val notification = NotificationCompat.Builder(this, "TimerServiceChannel")
//            .setContentTitle("Timer Expired")
//            .setContentText("Your timer has finished.")
//            .setSmallIcon(R.drawable.splash_icon)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .build()
//        notificationManager.notify(1, notification)
//
//        // Bring app to foreground
//        bringAppToForeground()
//    }
//
//    private fun bringAppToForeground() {
//        val intent = packageManager.getLaunchIntentForPackage(packageName)
//        intent?.let {
//            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
//            startActivity(it)
//        }
//    }
//}
