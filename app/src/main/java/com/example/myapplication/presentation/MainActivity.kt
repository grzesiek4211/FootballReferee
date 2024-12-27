


package com.example.myapplication.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.InputDevice
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

const val DEFAULT_TIME_DURATION_MINUTES = 5L

class MainActivity : AppCompatActivity() {
    private var timerDuration: Long = DEFAULT_TIME_DURATION_MINUTES
    private lateinit var myTeam: ArrayList<String>
    private lateinit var opponentTeam: ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        timerDuration = intent.getLongExtra("TIMER_DURATION", DEFAULT_TIME_DURATION_MINUTES)
        myTeam = intent.getStringArrayListExtra("MY_TEAM" ) ?: arrayListOf()
        opponentTeam = intent.getStringArrayListExtra("OPPONENT_TEAM" ) ?: arrayListOf()

        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val pagerAdapter = PagerAdapter(this, timerDuration, myTeam, opponentTeam)
        viewPager.adapter = pagerAdapter

        setNotClickablePageIndicators(viewPager)
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
        viewPager.currentItem += 1
    }

    private fun rotateToLeft(viewPager: ViewPager2) {
        viewPager.currentItem -= 1
    }
}
