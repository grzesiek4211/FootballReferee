package com.example.myapplication.presentation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.myapplication.R


class TimerFragment : CurrentTimeFragment(R.layout.timer_fragment, R.id.current_time) {

    companion object {
        fun newInstance(timerDuration: Long) = TimerFragment().apply {
            arguments = Bundle().apply {
                putLong("TIMER_DURATION", timerDuration)
            }
        }
        private const val TIMER_REQUEST_CODE = 100 // Unikalny kod dla PendingIntent AlarmManagera
        private const val STATE_TIME_REMAINING = "state_time_remaining"
        private const val STATE_IS_PAUSED = "state_is_paused"
        private const val TAG = "TimerFragment"
    }

    private lateinit var timerTextView: TextView
    private lateinit var buttonPausePlay: Button
    private lateinit var buttonRestart: Button
    private var timer: CountDownTimer? = null
    private var isPaused = false
    private var initialTimerValue: Long = DEFAULT_TIME_DURATION_MINUTES * 60 * 1000L
    private var timeRemaining: Long = initialTimerValue


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")

        savedInstanceState?.let {
            timeRemaining = it.getLong(STATE_TIME_REMAINING, initialTimerValue)
            isPaused = it.getBoolean(STATE_IS_PAUSED, false)
            Log.d(TAG, "onCreateView: Restored state timeRemaining=${timeRemaining/1000}s, isPaused=${isPaused}")
        }

        if (savedInstanceState == null) {
            arguments?.let {
                timeRemaining = it.getLong("TIMER_DURATION", DEFAULT_TIME_DURATION_MINUTES * 60 * 1000L)
                initialTimerValue = it.getLong("TIMER_DURATION", DEFAULT_TIME_DURATION_MINUTES * 60 * 1000L)
                Log.d(TAG, "onCreateView: Initial timer duration from arguments: ${initialTimerValue / 1000}s")
            }
        }

        val view = super.onCreateView(inflater, container, savedInstanceState)

        timerTextView = view.findViewById(R.id.timerTextView)
        buttonPausePlay = view.findViewById(R.id.button_pause_play)
        buttonRestart = view.findViewById(R.id.button_restart)

        initializeTimerDisplay()
        if (!isPaused) {
            val alarmIsScheduled = getAlarmPendingIntentExists()
            Log.d(TAG, "onCreateView: Timer was not paused. Alarm PendingIntent exists: $alarmIsScheduled")

            if (alarmIsScheduled) {
                startUITimer(timeRemaining)
                Log.d(TAG, "onCreateView: System alarm scheduled, starting UI timer only.")
            } else {
                Log.d(TAG, "onCreateView: System alarm NOT scheduled, starting full timer sequence.")
                startUITimer(timeRemaining)
                scheduleAlarm(timeRemaining)
            }
            buttonPausePlay.text = "⏸"
        } else {
            initializeTimerDisplay() // Upewnij się, że wyświetla poprawny, zapisany czas
            buttonPausePlay.text = "▶️"
            Log.d(TAG, "onCreateView: Timer was paused.")
            if (getAlarmPendingIntentExists()) {
                Log.w(TAG, "onCreateView: Timer was paused but system alarm still exists! Cancelling.")
                cancelAlarm()
            }
        }


        buttonPausePlay.setOnClickListener {
            if (isPaused) {
                Log.d(TAG, "buttonPausePlay clicked: Resuming timer.")
                startTimer(timeRemaining)
                buttonPausePlay.text = "⏸"
                isPaused = false
            } else {
                Log.d(TAG, "buttonPausePlay clicked: Pausing timer. Time remaining: ${timeRemaining / 1000}s")
                pauseTimer()
                buttonPausePlay.text = "▶️"
                isPaused = true
            }
        }

        buttonRestart.setOnClickListener {
            Log.d(TAG, "buttonRestart clicked: Restarting timer.")
            cancelTimer()
            cancelAlarm()
            timeRemaining = initialTimerValue
            initializeTimerDisplay()
            startTimer(timeRemaining)
            buttonPausePlay.text = "⏸"
            isPaused = false
            stopAlarmServiceIfRunning()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }


    private fun initializeTimerDisplay() {
        val minutes = timeRemaining / 1000 / 60
        val seconds = (timeRemaining / 1000) % 60
        timerTextView.text = String.format("%02d:%02d", minutes, seconds)
        Log.d(TAG, "Display initialized to: ${timerTextView.text}")
    }

    private fun startUITimer(timeMillis: Long) {
        Log.d(TAG, "Starting *only* UI timer for ${timeMillis / 1000}s")
        cancelTimer()

        if (timeMillis <= 0) {
            timerTextView.text = "00:00"
            Log.d(TAG, "startUITimer: Initial time <= 0, timer finished instantly.")
            return
        }

        timer = object : CountDownTimer(timeMillis, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                timerTextView.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                Log.d(TAG, "UI Timer finished.")
                timeRemaining = 0
                timerTextView.text = "00:00"
            }
        }.start()
    }

    private fun startTimer(timeMillis: Long) {
        Log.d(TAG, "Starting UI timer and scheduling system alarm for ${timeMillis / 1000}s from now.")
        startUITimer(timeMillis)
        scheduleAlarm(timeMillis)
    }


    private fun pauseTimer() {
        Log.d(TAG, "Pausing UI timer and cancelling system alarm.")
        cancelTimer()
        cancelAlarm()
    }

    private fun cancelTimer() {
        timer?.cancel()
        timer = null
        Log.d(TAG, "UI timer cancelled.")
    }


    private fun getAlarmManager(): AlarmManager {
        return requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    private fun getAlarmPendingIntent(): PendingIntent {
        val intent = Intent(requireContext(), TimerExpiredReceiver::class.java).apply {
            action = TimerExpiredReceiver.ACTION_TIMER_EXPIRED
        }
        return PendingIntent.getBroadcast(
            requireContext(),
            TIMER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getAlarmPendingIntentExists(): Boolean {
        val intent = Intent(requireContext(), TimerExpiredReceiver::class.java).apply {
            action = TimerExpiredReceiver.ACTION_TIMER_EXPIRED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            TIMER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        val exists = pendingIntent != null
        Log.d(TAG, "Checking if Alarm PendingIntent exists (FLAG_NO_CREATE): $exists")
        return exists
    }

    private fun scheduleAlarm(timeUntilFinishMillis: Long) {
        Log.d(TAG, "Scheduling system alarm for ${timeUntilFinishMillis / 1000}s from now.")
        cancelAlarm()

        val alarmManager = getAlarmManager()
        val pendingIntent = getAlarmPendingIntent()

        val triggerTime = System.currentTimeMillis() + timeUntilFinishMillis

        val showIntent = Intent(requireContext(), MainActivity::class.java) // Wskaż Aktywność, którą system ma pokazać
        val showPendingIntent = PendingIntent.getActivity(
            requireContext(),
            TIMER_REQUEST_CODE + 1, // Inny unikalny request code
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                Log.d(TAG, "AlarmManager setAlarmClock scheduled (canScheduleExactAlarms granted).")
            } else {
                Log.w(TAG, "Cannot schedule exact alarms. Alarm may be delayed. Using fallback.")
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                Log.d(TAG, "AlarmManager setExactAndAllowWhileIdle scheduled (API 31+ fallback).")
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            Log.d(TAG, "AlarmManager setExactAndAllowWhileIdle scheduled (API 23-30).")
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            Log.d(TAG, "AlarmManager setExact scheduled (<API 23).")
        }
    }

    private fun cancelAlarm() {
        Log.d(TAG, "Attempting to cancel system alarm.")
        val alarmManager = getAlarmManager()
        val pendingIntent = getAlarmPendingIntent()
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "System alarm cancellation requested.")
        stopAlarmServiceIfRunning()
    }

    private fun stopAlarmServiceIfRunning() {
        Log.d(TAG, "Attempting to stop AlarmNotificationService.")
        val alarmServiceIntent = Intent(requireContext(), AlarmNotificationService::class.java)
        try {
            requireContext().stopService(alarmServiceIntent)
            Log.d(TAG, "AlarmNotificationService stop requested.")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping AlarmNotificationService: ${e.message}")
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(STATE_TIME_REMAINING, timeRemaining)
        outState.putBoolean(STATE_IS_PAUSED, isPaused)
        Log.d(TAG, "onSaveInstanceState: Saving timeRemaining=${timeRemaining/1000}s, isPaused=${isPaused}")
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.d(TAG, "onViewStateRestored")
        savedInstanceState?.let {
            Log.d(TAG, "onViewStateRestored: Using state restored in onCreateView.")
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView. Cancelling UI timer.")
        cancelTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }
}