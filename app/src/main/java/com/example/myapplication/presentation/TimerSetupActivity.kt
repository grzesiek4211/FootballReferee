package com.example.myapplication.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class TimerSetupActivity : AppCompatActivity() {
    private var timeInMinutes = 5L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer_setup)

        val timeTextView: TextView = findViewById(R.id.timeTextView)
        val incrementButton: Button = findViewById(R.id.incrementButton)
        val decrementButton: Button = findViewById(R.id.decrementButton)
        val startButton: Button = findViewById(R.id.startButton)

        updateDisplayedTime(timeTextView)

        incrementButton.setOnClickListener {
            timeInMinutes++
            updateDisplayedTime(timeTextView)
        }

        decrementButton.setOnClickListener {
            if (timeInMinutes > 1) {
                timeInMinutes--
                updateDisplayedTime(timeTextView)
            }
        }

        startButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("TIMER_DURATION", timeInMinutes * 60 * 1000)
            startActivity(intent)
            finish()  // Close this activity to prevent going back to it
        }
    }

    private fun updateDisplayedTime(timeTextView: TextView) {
        timeTextView.text = String.format("%02d:00", timeInMinutes)
    }
}
