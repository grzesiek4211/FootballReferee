package com.example.myapplication.presentation

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import android.util.Log


class FullScreenAlarmActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("FullScreenAlarmActivity", "onCreate: Displaying full screen alarm.")

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_full_screen_alarm)

        val stopButton: Button = findViewById(R.id.stopAlarmButton)
        stopButton.setOnClickListener {
            Log.d("FullScreenAlarmActivity", "Stop button clicked. Stopping AlarmNotificationService.")
            val stopAlarmIntent = Intent(this, AlarmNotificationService::class.java)
            stopService(stopAlarmIntent)

            finish()
        }
    }

    override fun onBackPressed() {
        Log.d("FullScreenAlarmActivity", "Back button pressed. Stopping AlarmNotificationService.")
        val stopAlarmIntent = Intent(this, AlarmNotificationService::class.java)
        stopService(stopAlarmIntent)
        super.onBackPressed()
    }
}