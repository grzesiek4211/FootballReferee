package com.example.myapplication.presentation

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.InputDevice
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.time.Instant
import java.time.format.DateTimeParseException
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status.Builder
import androidx.wear.ongoing.Status.TextPart

const val DEFAULT_TIME_DURATION_MINUTES = 5L

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val STATE_MATCH_START_TIME = "state_match_start_time"
        const val EXTRA_TIMER_DURATION = "TIMER_DURATION"
        const val EXTRA_MY_TEAM = "MY_TEAM"
        const val EXTRA_OPPONENT_TEAM = "OPPONENT_TEAM"
        const val EXTRA_MATCH_START_TIME = "MATCH_START_TIME"
        const val ONGOING_NOTIFICATION_ID = 456
        private const val ONGOING_CHANNEL_ID = "ongoing_timer"
    }

    private var timerDuration: Long = DEFAULT_TIME_DURATION_MINUTES * 60 * 1000L
    private var myTeam: ArrayList<String>? = null
    private var opponentTeam: ArrayList<String>? = null
    private lateinit var matchStartTime: Instant

    // Flaga, która powie nam, czy dane są już wczytane, żeby ich nie nadpisywać
    private var isDataInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        Log.d(TAG, "onCreate: MainActivity started.")

        // 1. Sprawdź, czy dane już istnieją w pamięci (np. po obrocie lub powrocie)
        if (savedInstanceState != null) {
            val savedStartTimeString = savedInstanceState.getString(STATE_MATCH_START_TIME)
            if (!savedStartTimeString.isNullOrEmpty()) {
                matchStartTime = Instant.parse(savedStartTimeString)
                isDataInitialized = true
            }
        }

        // 2. Jeśli nie ma danych z savedInstanceState, spróbuj z Intentu
        if (!isDataInitialized) {
            initFromIntentOrDefaults()
        }

        val sharedTeamViewModel = ViewModelProvider(this)[SharedTeamViewModel::class.java]
        sharedTeamViewModel.initTeams(myTeam ?: arrayListOf(), opponentTeam ?: arrayListOf())

        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val pagerAdapter = PagerAdapter(this, timerDuration, matchStartTime)
        viewPager.adapter = pagerAdapter

        setNotClickablePageIndicators(viewPager)
        startOngoingChip()
    }

    // Dodajemy obsługę nowego Intentu (wywoływane przy launchMode="singleInstance")
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent) // Ważne: aktualizujemy intent aktywności
        Log.d(TAG, "onNewIntent: Re-opening existing activity instance.")
    }

    private fun initFromIntentOrDefaults() {
        Log.d(TAG, "initFromIntentOrDefaults")
        timerDuration = intent.getLongExtra(EXTRA_TIMER_DURATION, DEFAULT_TIME_DURATION_MINUTES * 60 * 1000L)
        myTeam = intent.getStringArrayListExtra(EXTRA_MY_TEAM)
        opponentTeam = intent.getStringArrayListExtra(EXTRA_OPPONENT_TEAM)

        val matchStartTimeString = intent.getStringExtra(EXTRA_MATCH_START_TIME)
        matchStartTime = if (!matchStartTimeString.isNullOrEmpty()) {
            try { Instant.parse(matchStartTimeString) } catch (e: Exception) { Instant.now() }
        } else {
            Instant.now()
        }
        isDataInitialized = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::matchStartTime.isInitialized) {
            outState.putString(STATE_MATCH_START_TIME, matchStartTime.toString())
        }
    }

    private fun setNotClickablePageIndicators(viewPager: ViewPager2) {
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()
        tabLayout.touchables.forEach { it.isClickable = false }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // Zamiast nic nie robić, na Wear OS często lepiej przenieść apkę do tyłu
        moveTaskToBack(true)
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_SCROLL && event.isFromSource(InputDevice.SOURCE_ROTARY_ENCODER)) {
            val viewPager: ViewPager2 = findViewById(R.id.viewPager)
            val delta = -event.getAxisValue(MotionEvent.AXIS_SCROLL)
            if (delta > 0) rotateToRight(viewPager) else rotateToLeft(viewPager)
            return true
        }
        return super.onGenericMotionEvent(event)
    }

    private fun rotateToRight(viewPager: ViewPager2) {
        if (viewPager.currentItem + 1 < (viewPager.adapter?.itemCount ?: 0)) {
            viewPager.currentItem += 1
        }
    }

    private fun rotateToLeft(viewPager: ViewPager2) {
        if (viewPager.currentItem - 1 >= 0) {
            viewPager.currentItem -= 1
        }
    }

    private fun startOngoingChip() {
        createOngoingNotificationChannel()

        // FLAG_IMMUTABLE dla bezpieczeństwa, ale singleInstance załatwi resztę
        val contentIntent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val builder = NotificationCompat.Builder(this, ONGOING_CHANNEL_ID)
            .setSmallIcon(R.mipmap.football_13302)
            .setContentTitle("Match in progress")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(contentIntent)

        val ongoingActivityStatus = Builder()
            .addPart("timer_status", TextPart("Match in progress"))
            .build()

        val ongoingActivity = OngoingActivity.Builder(this, ONGOING_NOTIFICATION_ID, builder)
            .setStaticIcon(R.mipmap.football_13302)
            .setTouchIntent(contentIntent)
            .setStatus(ongoingActivityStatus)
            .build()

        ongoingActivity.apply(this)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(ONGOING_NOTIFICATION_ID, builder.build())
    }

    private fun createOngoingNotificationChannel() {
        val channel = NotificationChannel(ONGOING_CHANNEL_ID, "Ongoing Timer", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}