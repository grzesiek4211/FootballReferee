package com.example.myapplication.presentation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.myapplication.R

class TimerFragment : CurrentTimeFragment(R.layout.timer_fragment, R.id.current_time) {

    private lateinit var prefs: SharedPreferences
    private lateinit var timerTextView: TextView
    private lateinit var buttonPausePlay: Button
    private lateinit var buttonRestart: Button

    private var timer: CountDownTimer? = null
    private var isPaused = false
    private var initialTimerValue: Long = 5 * 60 * 1000L // Domyślnie 5 min
    private var timeRemaining: Long = initialTimerValue
    private var endTime: Long = 0L

    companion object {
        fun newInstance(timerDuration: Long) = TimerFragment().apply {
            arguments = Bundle().apply {
                putLong("TIMER_DURATION", timerDuration)
            }
        }
        const val TIMER_REQUEST_CODE = 100
        private const val PREFS_NAME = "timer_prefs"
        private const val KEY_END_TIME = "end_time"
        private const val KEY_IS_PAUSED = "is_paused"
        private const val KEY_REMAINING = "remaining"
        private const val TAG = "TimerFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        timerTextView = view.findViewById(R.id.timerTextView)
        buttonPausePlay = view.findViewById(R.id.button_pause_play)
        buttonRestart = view.findViewById(R.id.button_restart)

        // 1. Załaduj stan z pamięci trwałej (SharedPrefs)
        loadState()

        // 2. Obsługa argumentów tylko przy pierwszym uruchomieniu
        if (savedInstanceState == null && endTime == 0L && timeRemaining == initialTimerValue) {
            arguments?.let {
                initialTimerValue = it.getLong("TIMER_DURATION", initialTimerValue)
                timeRemaining = initialTimerValue
            }
            // AUTO-START: Odpalamy od razu
            startTimer(timeRemaining)
        }

        buttonPausePlay.setOnClickListener {
            if (isPaused) {
                checkExactAlarmPermission()
                startTimer(timeRemaining)
            } else {
                pauseTimer()
            }
        }

        buttonRestart.setOnClickListener {
            restartTimer()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Kluczowe dla Wear OS 6: Przelicz czas po powrocie do apki
        refreshTimerState()
    }

    override fun onPause() {
        super.onPause()
        // Zapisz stan, gdy użytkownik wychodzi przyciskiem "wstecz"
        saveState()
    }

    private fun refreshTimerState() {
        if (!isPaused && endTime > 0) {
            val now = System.currentTimeMillis()
            timeRemaining = endTime - now

            if (timeRemaining <= 0) {
                timeRemaining = 0
                updateUI(0)
                isPaused = true
            } else {
                startUITimer(timeRemaining)
            }
        } else {
            updateUI(timeRemaining)
        }
        updateButtonText()
    }

    private fun startTimer(timeMillis: Long) {
        isPaused = false
        endTime = System.currentTimeMillis() + timeMillis
        saveState()
        startUITimer(timeMillis)
        scheduleAlarm(timeMillis)
        updateButtonText()
    }

    private fun pauseTimer() {
        isPaused = true
        cancelTimer()
        cancelAlarm()
        endTime = 0
        saveState()
        updateButtonText()
    }

    private fun restartTimer() {
        cancelTimer()
        cancelAlarm()
        stopAlarmServiceIfRunning()
        timeRemaining = initialTimerValue
        startTimer(timeRemaining)
    }

    private fun startUITimer(timeMillis: Long) {
        timer?.cancel()
        timer = object : CountDownTimer(timeMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                updateUI(millisUntilFinished)
            }
            override fun onFinish() {
                timeRemaining = 0
                endTime = 0
                isPaused = true
                updateUI(0)
                updateButtonText()
            }
        }.start()
    }

    private fun updateUI(millis: Long) {
        val minutes = millis / 1000 / 60
        val seconds = (millis / 1000) % 60
        timerTextView.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateButtonText() {
        buttonPausePlay.text = if (isPaused) "▶️" else "⏸"
    }

    // --- SYSTEM ALARM & PERMISSIONS ---

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        }
    }

    private fun scheduleAlarm(timeUntilFinishMillis: Long) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + timeUntilFinishMillis

        val intent = Intent(requireContext(), TimerExpiredReceiver::class.java).apply {
            action = TimerExpiredReceiver.ACTION_TIMER_EXPIRED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            TIMER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(requireContext(), MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            requireContext(),
            TIMER_REQUEST_CODE + 1,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)

        try {
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    private fun cancelAlarm() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), TimerExpiredReceiver::class.java).apply {
            action = TimerExpiredReceiver.ACTION_TIMER_EXPIRED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            TIMER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun stopAlarmServiceIfRunning() {
        val intent = Intent(requireContext(), AlarmNotificationService::class.java)
        requireContext().stopService(intent)
    }

    // --- PERSISTENCE ---

    private fun saveState() {
        prefs.edit().apply {
            putLong(KEY_END_TIME, endTime)
            putLong(KEY_REMAINING, timeRemaining)
            putBoolean(KEY_IS_PAUSED, isPaused)
            apply()
        }
    }

    private fun loadState() {
        // Czytamy czas zakończenia i stan pauzy
        endTime = prefs.getLong(KEY_END_TIME, 0L)
        isPaused = prefs.getBoolean(KEY_IS_PAUSED, false)

        // CZYTAMY ZAPISANY CZAS SETUPU (nasze 4 minuty)
        val savedSetupTime = prefs.getLong("saved_setup_time", 5) // 5 jako domyślne
        initialTimerValue = savedSetupTime * 60 * 1000L

        // Jeśli nie mamy zapisanego pozostałego czasu, używamy tego z setupu
        timeRemaining = prefs.getLong(KEY_REMAINING, initialTimerValue)
    }

    private fun cancelTimer() {
        timer?.cancel()
        timer = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cancelTimer()
    }
}