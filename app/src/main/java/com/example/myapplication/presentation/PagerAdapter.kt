package com.example.myapplication.presentation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.time.Instant

class PagerAdapter(
    fragmentActivity: FragmentActivity,
    timerDuration: Long,
    matchStartTime: Instant
) : FragmentStateAdapter(fragmentActivity) {

    private val timerFragment: TimerFragment = TimerFragment.newInstance(timerDuration)
    private val scoreFragment: ScoreFragment = ScoreFragment.newInstance(matchStartTime)
    private val matchSummaryFragment: MatchSummaryFragment = MatchSummaryFragment.newInstance()
    private val stopFragment: StopFragment = StopFragment()

    override fun getItemCount(): Int = 5 // Number of pages

    override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> timerFragment
                1 -> scoreFragment
                2 -> matchSummaryFragment
                3 -> PlayerSelectorFragment()
                4 -> stopFragment
                else ->  StopFragment()
            }
    }
}