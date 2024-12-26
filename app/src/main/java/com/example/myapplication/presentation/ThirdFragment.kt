package com.example.myapplication.presentation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.R

class ThirdFragment : Fragment() {

    private lateinit var stopLayout: View
    private lateinit var confirmationStopLayout: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_third, container, false)

        stopLayout = view.findViewById(R.id.stop_layout)
        confirmationStopLayout = view.findViewById(R.id.confirmation_stop_layout)

        val stopButton: Button = view.findViewById(R.id.stopButton)
        stopButton.setOnClickListener {
            showConfirmationStopDialog(stopLayout, confirmationStopLayout)
            true
        }

        return view
    }

    private fun showConfirmationStopDialog(stopLayout: View, confirmationStopLayout: View) {
        stopLayout.visibility = View.GONE
        confirmationStopLayout.visibility = View.VISIBLE

        val cancelButton =
            confirmationStopLayout.findViewById<TextView>(R.id.stop_confirmation_cancel_button)
        val confirmButton =
            confirmationStopLayout.findViewById<TextView>(R.id.stop_confirmation_confirm_button)

        cancelButton.setOnClickListener {
            confirmationStopLayout.visibility = View.GONE
            stopLayout.visibility = View.VISIBLE
        }

        confirmButton.setOnClickListener {
            val intent = Intent(activity, TimerSetupActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }
}

