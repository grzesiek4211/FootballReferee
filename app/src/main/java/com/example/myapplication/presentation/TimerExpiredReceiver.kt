package com.example.myapplication.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class TimerExpiredReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_TIMER_EXPIRED = "com.example.myapplication.ACTION_TIMER_EXPIRED"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("TimerExpiredReceiver", "Alarm received! Starting AlarmNotificationService.")
        // Sprawdź, czy akcja jest tą, którą oczekujemy (choć z AlarmManagera powinna być)
        if (intent.action == ACTION_TIMER_EXPIRED) {
            // Uruchom nowy Foreground Service, który zajmie się alarmem (powiadomienie, dźwięk, wibracje)
            val alarmIntent = Intent(context, AlarmNotificationService::class.java)

            // Na nowszych wersjach Androida musisz uruchomić Service jako foreground service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(alarmIntent)
            } else {
                context.startService(alarmIntent)
            }
        }
    }
}