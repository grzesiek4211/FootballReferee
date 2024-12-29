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

        setTitle()

        return view
    }

    private fun setTitle() {
        val titleTextView = TextView(requireContext()).apply {
            text = "Summary"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        }
        summaryContainer.addView(titleTextView, 0)
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
        setTitle()
        for (i in 0..<history.history.size) {
            val item = history.history[i]
            val scoreTextView = TextView(requireContext()).apply {
                val team1Score: Score = item.takeIf { it.team == Team.TEAM1 }?.score ?: history.opponentScore(item)
                val team2Score: Score = item.takeIf { it.team == Team.TEAM2 }?.score ?: history.opponentScore(item)
                text = "${team1Score}-${team2Score}"
                textSize = 18f
                gravity = Gravity.CENTER

                if (item.team == Team.TEAM2) {
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                } else {
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                }
            }
            container.addView(scoreTextView, 2 * i + 1)

            val scorerAssistantTextView = TextView(requireContext()).apply {
                text = item.scorerToString()
                textSize = 16f
                gravity = Gravity.CENTER
            }
            container.addView(scorerAssistantTextView, 2 * i + 2)
        }
        container.addView(TextView(requireContext()))
        container.addView(TextView(requireContext()))
    }

    companion object {
        fun newInstance(): MatchSummaryFragment {
            return MatchSummaryFragment()
        }
    }
}
