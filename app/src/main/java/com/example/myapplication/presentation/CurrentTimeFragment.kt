package com.example.myapplication.presentation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

open class CurrentTimeFragment(private val fragmentLayout: Int, private val timeTextView: Int) : Fragment() {
    private lateinit var currentTimeTextView: TextView
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(fragmentLayout, container, false)
        currentTimeTextView = view.findViewById(timeTextView)
        return view
    }

    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateTime(currentTimeTextView)
            handler.postDelayed(this, 1000) // Re-run every second
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateTimeRunnable) // Start updating time when fragment is visible
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateTimeRunnable) // Stop updating time when fragment is not visible
    }

    private fun updateTime(currentTimeTextView: TextView) {
        val currentTime = timeFormat.format(Date())
        currentTimeTextView.text = currentTime
    }
}