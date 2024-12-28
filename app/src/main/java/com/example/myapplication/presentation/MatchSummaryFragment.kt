package com.example.myapplication.presentation

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R

class MatchSummaryFragment : Fragment() {

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var summaryContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        val view = inflater.inflate(R.layout.match_summary_fragment, container, false)
        summaryContainer = view.findViewById(R.id.summary_container)

        return view
    }

    override fun onResume() {
        super.onResume()
        refreshSummary()
    }

    private fun refreshSummary() {
        val history = sharedViewModel.history.value ?: History()
        populateSummary(summaryContainer, history)
    }

    private fun populateSummary(container: LinearLayout, history: History) {
        container.removeAllViews()
        for (i in 0..<history.history.size) {
            val item = history.history[i]
            val scoreTextView = TextView(requireContext()).apply {
                text = "${item.team1Score}-${item.team2Score}"
                textSize = 18f
                gravity = Gravity.CENTER

                val previousTeam2Score = if(i > 0) {
                    history.history[i - 1].team2Score.value
                } else {
                    0
                }
                if (item.team2Score.value > previousTeam2Score) {
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                } else {
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                }
            }
            container.addView(scoreTextView, 2 * i)

            val scorerAssistantTextView = TextView(requireContext()).apply {
                text = item.scorerToString()
                textSize = 16f
                gravity = Gravity.CENTER
            }
            container.addView(scorerAssistantTextView, 2 * i + 1)
        }
    }

    companion object {
        fun newInstance(): MatchSummaryFragment {
            return MatchSummaryFragment()
        }
    }
}
