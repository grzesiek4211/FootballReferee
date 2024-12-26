


package com.example.myapplication.presentation

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import android.app.NotificationChannel
import android.content.Context
import android.os.Build
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedDispatcher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private var timerDuration: Long = 5  // Default to 5 minutes
    private val REQUEST_CODE_NOTIFICATION = 101
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the timer duration from the intent
        timerDuration = intent.getLongExtra("TIMER_DURATION", 5L)

        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val pagerAdapter = PagerAdapter(this, timerDuration)
        viewPager.adapter = pagerAdapter

        // Set up the dots (page indicator)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

//        requestNotificationPermission()
        createNotificationChannel(this)
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
//        super.onBackPressed()
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_SCROLL && event.isFromSource(InputDevice.SOURCE_ROTARY_ENCODER)) {
            val viewPager: ViewPager2 = findViewById(R.id.viewPager)
            val delta = -event.getAxisValue(MotionEvent.AXIS_SCROLL)
            if (delta > 0) {
                // Rotate right
                viewPager.currentItem += 1
            } else {
                // Rotate left
                viewPager.currentItem -= 1
            }
            return true
        }
        return super.onGenericMotionEvent(event)
    }

//    private fun requestNotificationPermission() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_NOTIFICATION)
//        }
//    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "timer_channel"
            val channelName = "Timer Notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Notifications for timer expiration"
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


}
