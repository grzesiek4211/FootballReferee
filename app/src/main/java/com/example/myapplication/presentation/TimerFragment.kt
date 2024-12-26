package com.example.myapplication.presentation


import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimerFragment() : Fragment() {

    companion object {
        fun newInstance(timerDuration: Long) = TimerFragment().apply {
            arguments = Bundle().apply {
                putLong("TIMER_DURATION", timerDuration)
            }
        }
    }

    private lateinit var currentTimeTextView: TextView
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var timerTextView: TextView
    private lateinit var buttonPausePlay: Button
    private lateinit var buttonRestart: Button
    private lateinit var timer: CountDownTimer
    private var isPaused = false
    private var initialTimerValue: Long = DEFAULT_TIME_DURATION_MINUTES * 60 * 1000
    private var timeRemaining: Long = initialTimerValue


    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null


    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 1000) // Re-run every second
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        arguments?.let {
            timeRemaining = it.getLong("TIMER_DURATION", DEFAULT_TIME_DURATION_MINUTES * 60 * 1000)
            initialTimerValue = it.getLong("TIMER_DURATION", DEFAULT_TIME_DURATION_MINUTES * 60 * 1000)
        }
        val view = inflater.inflate(R.layout.timer_fragment, container, false)

        currentTimeTextView = view.findViewById(R.id.current_time)

        timerTextView = view.findViewById(R.id.timerTextView)
        buttonPausePlay = view.findViewById(R.id.button_pause_play)
        buttonRestart = view.findViewById(R.id.button_restart)

        initializeTimer()
        startTimer(timeRemaining)

        buttonPausePlay.setOnClickListener {
            if (isPaused) {
                startTimer(timeRemaining)
                buttonPausePlay.text = "⏸"
                isPaused = false
            } else {
                if (::timer.isInitialized) {
                    timer.cancel()
                }
                buttonPausePlay.text = "▶️"
                isPaused = true
            }
            stopAlarm()
        }

        buttonRestart.setOnClickListener {
            if (::timer.isInitialized) {
                timer.cancel()
            }
            stopAlarm()
            timeRemaining = initialTimerValue // Reset to 5 minutes
            startTimer(timeRemaining)
            buttonPausePlay.text = "⏸"
            isPaused = false
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateTimeRunnable) // Start updating time when fragment is visible
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateTimeRunnable) // Stop updating time when fragment is not visible
    }

    private fun updateTime() {
        val currentTime = timeFormat.format(Date())
        currentTimeTextView.text = currentTime
    }

    private fun initializeTimer() {
        val minutes = timeRemaining / 1000 / 60
        val seconds = (timeRemaining / 1000) % 60
        timerTextView.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun startTimer(timeMillis: Long) {
        timer = object : CountDownTimer(timeMillis, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                timerTextView.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                timerTextView.text = "00:00"
                triggerAlarm()
            }
        }
        timer.start()
    }

    private fun triggerAlarm() {
        val requireContext = requireContext()
        vibrator = requireContext.getSystemService(Vibrator::class.java)
        vibrator?.vibrate(VibrationEffect.createOneShot(60000, VibrationEffect.DEFAULT_AMPLITUDE))
        mediaPlayer = MediaPlayer.create(requireContext, R.raw.google_duo)
        mediaPlayer?.start()
    }

    private fun stopAlarm() {
        vibrator?.cancel()

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::timer.isInitialized) {
            timer.cancel()
        }
        stopAlarm()
    }
}
