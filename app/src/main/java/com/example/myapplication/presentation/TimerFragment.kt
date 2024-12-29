package com.example.myapplication.presentation


import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
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
    }

    private lateinit var timerTextView: TextView
    private lateinit var buttonPausePlay: Button
    private lateinit var buttonRestart: Button
    private lateinit var timer: CountDownTimer
    private var isPaused = false
    private var initialTimerValue: Long = DEFAULT_TIME_DURATION_MINUTES * 60 * 1000
    private var timeRemaining: Long = initialTimerValue


    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        arguments?.let {
            timeRemaining = it.getLong("TIMER_DURATION", DEFAULT_TIME_DURATION_MINUTES * 60 * 1000)
            initialTimerValue = it.getLong("TIMER_DURATION", DEFAULT_TIME_DURATION_MINUTES * 60 * 1000)
        }
        val view = super.onCreateView(inflater, container, savedInstanceState)

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
