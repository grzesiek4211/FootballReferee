package com.example.myapplication.presentation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PagerAdapter(fragmentActivity: FragmentActivity, timerDuration: Long) : FragmentStateAdapter(fragmentActivity) {

    private val firstFragment: FirstFragment = FirstFragment.newInstance(timerDuration)
    private val secondFragment: SecondFragment = SecondFragment()
    private val thirdFragment: ThirdFragment = ThirdFragment()

    override fun getItemCount(): Int = 3 // Number of pages

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> firstFragment // First screen with the current time
            1 -> secondFragment // Second screen, replace with your actual fragment
            2 -> thirdFragment // Second screen, replace with your actual fragment
            else -> FirstFragment()
        }
    }
}

//class PagerAdapter(private val context: Context) : RecyclerView.Adapter<PagerAdapter.PagerViewHolder>() {
//
//    private val layouts = listOf(R.layout.fragment_first, R.layout.fragment_second, R.layout.fragment_third)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
//        val view = LayoutInflater.from(context).inflate(viewType, parent, false)
//        return PagerViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
//         No additional binding needed
//    }
//
//    override fun getItemViewType(position: Int): Int {
//        return layouts[position]
//    }
//
//    override fun getItemCount(): Int {
//        return layouts.size
//    }
//
//    class PagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
//}
//