package com.example.myapplication.presentation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.time.Instant

class PagerAdapter(
    fragmentActivity: FragmentActivity,
    timerDuration: Long,
    team1: ArrayList<String>,
    team2: ArrayList<String>,
    matchStartTime: Instant
) : FragmentStateAdapter(fragmentActivity) {

    private val timerFragment: TimerFragment = TimerFragment.newInstance(timerDuration)
    private val scoreFragment: ScoreFragment = ScoreFragment.newInstance(team1, team2, matchStartTime)
    private val matchSummaryFragment: MatchSummaryFragment = MatchSummaryFragment.newInstance()
    private val stopFragment: StopFragment = StopFragment()

    override fun getItemCount(): Int = 5 // Number of pages

    override fun createFragment(position: Int): Fragment {
        if (scoreFragment.team1.isNotEmpty() && scoreFragment.team2.isNotEmpty()) {
            return when (position) {
                0 -> timerFragment
                1 -> scoreFragment
                2 -> matchSummaryFragment
                3 -> PlayerSelectorFragment(scoreFragment.team1, scoreFragment.team2)
                4 -> stopFragment
                else ->  StopFragment()
            }
        } else {
            return when (position) {
                0 -> timerFragment
                1 -> scoreFragment
                2 -> matchSummaryFragment
                3 -> stopFragment
                else -> matchSummaryFragment
            }
        }
    }
}