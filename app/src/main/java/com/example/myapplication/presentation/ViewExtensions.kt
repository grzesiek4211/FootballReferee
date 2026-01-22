package com.example.myapplication.presentation

import android.os.SystemClock
import android.util.Log
import android.view.View

/**
 * Rozszerzenie dla View, które zapobiega wielokrotnym kliknięciom (np. przez deszcz).
 * Domyślny czas blokady to 1000ms (1 sekunda).
 */

fun View.setSafeOnClickListener(interval: Int = 1000, onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener(interval) {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}

class SafeClickListener(
    private var defaultInterval: Int,
    private val onSafeCLick: (View) -> Unit
) : View.OnClickListener {

    private var lastTimeClicked: Long = 0

    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            Log.d("SafeClick", "Zignorowano szybkie kliknięcie na: ${v.id}")
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSafeCLick(v)
    }
}