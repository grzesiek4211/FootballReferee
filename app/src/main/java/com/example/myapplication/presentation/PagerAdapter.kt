package com.example.myapplication.presentation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PagerAdapter(fragmentActivity: FragmentActivity, timerDuration: Long) : FragmentStateAdapter(fragmentActivity) {

    private val timerFragment: TimerFragment = TimerFragment.newInstance(timerDuration)
    private val scoreFragment: ScoreFragment = ScoreFragment()
    private val stopFragment: StopFragment = StopFragment()

    override fun getItemCount(): Int = 3 // Number of pages

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> timerFragment
            1 -> scoreFragment
            2 -> stopFragment
            else -> TimerFragment()
        }
    }
}