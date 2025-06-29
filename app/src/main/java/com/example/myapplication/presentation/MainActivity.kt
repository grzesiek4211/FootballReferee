package com.example.myapplication.presentation

import android.annotation.SuppressLint
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

const val DEFAULT_TIME_DURATION_MINUTES = 5L

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val STATE_MATCH_START_TIME = "state_match_start_time"
        const val EXTRA_TIMER_DURATION = "TIMER_DURATION"
        const val EXTRA_MY_TEAM = "MY_TEAM"
        const val EXTRA_OPPONENT_TEAM = "OPPONENT_TEAM"
        const val EXTRA_MATCH_START_TIME = "MATCH_START_TIME" // Key for Intent extra
        const val ACTION_SHOW_TIMER = "com.example.myapplication.ACTION_SHOW_TIMER"
    }

    private var timerDuration: Long = DEFAULT_TIME_DURATION_MINUTES * 60 * 1000L // Default in milliseconds
    private lateinit var myTeam: ArrayList<String>
    private lateinit var opponentTeam: ArrayList<String>
    private lateinit var matchStartTime: Instant

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        Log.d(TAG, "onCreate: MainActivity started.")

        if (savedInstanceState != null) {
            val savedStartTimeString = savedInstanceState.getString(STATE_MATCH_START_TIME)
            Log.d(TAG, "onCreate: Found savedInstanceState. savedStartTimeString = $savedStartTimeString")
            if (!savedStartTimeString.isNullOrEmpty()) {
                try {
                    matchStartTime = Instant.parse(savedStartTimeString)
                    Log.d(TAG, "onCreate: Restored matchStartTime from savedInstanceState: $matchStartTime")
                } catch (e: DateTimeParseException) {
                    Log.e(TAG, "onCreate: Error parsing saved match start time. Using default.", e)
                    initFromIntentOrDefaults()
                }
            } else {
                Log.d(TAG, "onCreate: savedInstanceState exists but STATE_MATCH_START_TIME is null/empty.")
                initFromIntentOrDefaults()
            }
        } else {
            Log.d(TAG, "onCreate: savedInstanceState is null. Initial launch or process restart.")
            initFromIntentOrDefaults()
        }

        if (!::matchStartTime.isInitialized || !::myTeam.isInitialized || !::opponentTeam.isInitialized) {
            Log.e(TAG, "onCreate: Data not fully initialized after logic. Calling initFromIntentOrDefaults again as safeguard.")
            initFromIntentOrDefaults()
        }

        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val pagerAdapter = PagerAdapter(this, timerDuration, myTeam, opponentTeam, matchStartTime)
        viewPager.adapter = pagerAdapter
        Log.d(TAG, "onCreate: PagerAdapter created with timerDuration=${timerDuration/1000}s, matchStartTime=$matchStartTime")

        setNotClickablePageIndicators(viewPager)
    }

    private fun initFromIntentOrDefaults() {
        Log.d(TAG, "initFromIntentOrDefaults: Initializing from Intent or using defaults.")
        timerDuration = intent.getLongExtra(EXTRA_TIMER_DURATION, DEFAULT_TIME_DURATION_MINUTES * 60 * 1000L)
        Log.d(TAG, "initFromIntentOrDefaults: timerDuration = ${timerDuration / 1000}s")

        myTeam = intent.getStringArrayListExtra(EXTRA_MY_TEAM ) ?: arrayListOf()
        opponentTeam = intent.getStringArrayListExtra(EXTRA_OPPONENT_TEAM ) ?: arrayListOf()
        Log.d(TAG, "initFromIntentOrDefaults: myTeam size = ${myTeam.size}, opponentTeam size = ${opponentTeam.size}")

        val matchStartTimeString = intent.getStringExtra(EXTRA_MATCH_START_TIME)
        Log.d(TAG, "initFromIntentOrDefaults: Received EXTRA_MATCH_START_TIME string from Intent: $matchStartTimeString")

        matchStartTime = if (!matchStartTimeString.isNullOrEmpty()) {
            try {
                Instant.parse(matchStartTimeString)
            } catch (e: DateTimeParseException) { // Use more specific exception
                Log.e(TAG, "initFromIntentOrDefaults: Error parsing MATCH_START_TIME string from Intent: $matchStartTimeString. Using Instant.now()", e)
                Instant.now()
            }
        } else {
            Log.w(TAG, "initFromIntentOrDefaults: MATCH_START_TIME extra is null or empty in Intent. Using Instant.now() as default.")
            Instant.now()
        }
        Log.d(TAG, "initFromIntentOrDefaults: final matchStartTime = $matchStartTime")
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::matchStartTime.isInitialized) {
            outState.putString(STATE_MATCH_START_TIME, matchStartTime.toString())
            Log.d(TAG, "onSaveInstanceState: Saved matchStartTime: $matchStartTime")
        } else {
            Log.w(TAG, "onSaveInstanceState: matchStartTime not initialized, not saving.")
        }
    }

    private fun setNotClickablePageIndicators(viewPager: ViewPager2) {
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()
        tabLayout.touchables.forEach { it.isClickable = false }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // no action on back pressed
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_SCROLL && event.isFromSource(InputDevice.SOURCE_ROTARY_ENCODER)) {
            val viewPager: ViewPager2 = findViewById(R.id.viewPager)
            val delta = -event.getAxisValue(MotionEvent.AXIS_SCROLL)
            if (delta > 0) {
                rotateToRight(viewPager)
            } else {
                rotateToLeft(viewPager)
            }
            return true
        }
        return super.onGenericMotionEvent(event)
    }

    private fun rotateToRight(viewPager: ViewPager2) {
        val nextItem = viewPager.currentItem + 1
        if (nextItem < viewPager.adapter?.itemCount ?: 0) {
            viewPager.currentItem = nextItem
        }
    }

    private fun rotateToLeft(viewPager: ViewPager2) {
        val prevItem = viewPager.currentItem - 1
        if (prevItem >= 0) {
            viewPager.currentItem = prevItem
        }
    }
}