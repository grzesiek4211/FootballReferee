package com.example.myapplication.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class TimerSetupActivity : AppCompatActivity() {

    // Klucze do SharedPreferences
    private val PREFS_NAME = "timer_prefs"
    private val KEY_SAVED_SETUP_TIME = "saved_setup_time"
    private val KEY_END_TIME = "end_time"
    private val KEY_IS_PAUSED = "is_paused"

    private var timeInMinutes = DEFAULT_TIME_DURATION_MINUTES

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val endTime = prefs.getLong(KEY_END_TIME, 0L)
        val isPaused = prefs.getBoolean(KEY_IS_PAUSED, true)

        // 1. Przekierowanie, jeśli mecz trwa
        if (endTime > 0 || !isPaused) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.timer_setup_activity)

        // 2. Wczytaj ostatnio używany czas (np. Twoje 4 minuty)
        timeInMinutes = prefs.getLong(KEY_SAVED_SETUP_TIME, DEFAULT_TIME_DURATION_MINUTES)

        val timeTextView: TextView = findViewById(R.id.timeTextView)
        val incrementButton: Button = findViewById(R.id.incrementButton)
        val decrementButton: Button = findViewById(R.id.decrementButton)
        val nextButton: Button = findViewById(R.id.nextButton)

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

        nextButton.setOnClickListener {
            // 3. ZAPISZ czas przed przejściem dalej, żeby apka go pamiętała na zawsze
            prefs.edit().putLong(KEY_SAVED_SETUP_TIME, timeInMinutes).apply()

            val intent = Intent(this, PlayersSetupActivity::class.java)
            intent.putExtra("TIMER_DURATION", timeInMinutes * 60 * 1000)
            startActivity(intent)
        }
    }

    private fun updateDisplayedTime(timeTextView: TextView) {
        timeTextView.text = String.format("%02d:00", timeInMinutes)
    }

    // Ten fragment kodu sprawi, że jak mecz trwa i jakimś cudem tu trafisz
    // (np. klikając wstecz), apka po prostu się schowa zamiast pokazać setup.
    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val endTime = prefs.getLong(KEY_END_TIME, 0L)
        if (endTime > 0) {
            finish()
        }
    }
}