package com.example.myapplication.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class TimerSetupActivity : AppCompatActivity() {
    private var timeInMinutes = DEFAULT_TIME_DURATION_MINUTES

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // KLUCZOWY FIX: Sprawdź, czy timer już działa w tle
        val prefs = getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)
        val endTime = prefs.getLong("end_time", 0L)
        val isPaused = prefs.getBoolean("is_paused", true)

        // Jeśli endTime > 0, to znaczy, że timer został uruchomiony i nie został zresetowany w StopFragment
        if (endTime > 0 || !isPaused) {
            val intent = Intent(this, MainActivity::class.java)
            // Flagi upewniają się, że nie tworzymy nowej kopii, tylko wracamy do starej
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            finish() // Zamykamy ekran setupu
            return
        }

        setContentView(R.layout.timer_setup_activity)

        // ... reszta Twojego kodu bez zmian ...
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
            val intent = Intent(this, PlayersSetupActivity::class.java)
            intent.putExtra("TIMER_DURATION", timeInMinutes * 60 * 1000)
            startActivity(intent)
            // UWAGA: Nie dawaj tu finish(), jeśli chcesz móc wrócić do ustawień czasu
            // z poziomu listy graczy przyciskiem wstecz.
        }
    }

    private fun updateDisplayedTime(timeTextView: TextView) {
        timeTextView.text = String.format("%02d:00", timeInMinutes)
    }
}