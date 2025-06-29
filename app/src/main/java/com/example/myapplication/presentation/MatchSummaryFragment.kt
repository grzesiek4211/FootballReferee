package com.example.myapplication.presentation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
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

class MatchSummaryFragment() : Fragment() {

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var summaryContainer: LinearLayout
    private lateinit var sharedScreenshotViewModel: SharedScreenshotViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        sharedScreenshotViewModel = ViewModelProvider(requireActivity()).get(SharedScreenshotViewModel::class.java)
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
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
        summaryContainer.addView(titleTextView, 0)
    }

    override fun onResume() {
        super.onResume()
        captureScreenshot(refreshSummary())
    }

    private fun refreshSummary(): LinearLayout {
        val history = sharedViewModel.history.value ?: History()
        return populateSummary(summaryContainer, history)
    }

    private fun captureScreenshot(view: View) {
        view.post {
            try {
                if (view.width > 0 && view.height > 0) {
                    val bitmap =
                        Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    view.draw(canvas)
                    sharedScreenshotViewModel.bitmap.value = bitmap
                }
            } catch (e: Exception) {
                Log.e("ScreenshotError", "Failed to capture screenshot: ${e.message}", e)
            }
        }
    }

    private fun populateSummary(container: LinearLayout, history: History): LinearLayout {
        container.removeAllViews()
        setTitle()
        for (i in 0..<history.history.size) {
            val item = history.history[i]
            val scoreTextView = TextView(requireContext()).apply {
                val team1Score: Score =
                    item.takeIf { it.team == Team.TEAM1 }?.score ?: history.opponentScore(item)
                val team2Score: Score =
                    item.takeIf { it.team == Team.TEAM2 }?.score ?: history.opponentScore(item)
                text = "${team1Score}-${team2Score} (${item.minute}\')"
                textSize = 18f
                gravity = Gravity.CENTER

                if (item.team == Team.TEAM2) {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                } else {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
            }
            container.addView(scoreTextView, 2 * i + 1)

            val scorerAssistantTextView = TextView(requireContext()).apply {
                text = item.scorerToString()
                textSize = 16f
                gravity = Gravity.CENTER
                if (item.team == Team.TEAM2) {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.pink))
                } else {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.light_gray))
                }
            }
            container.addView(scorerAssistantTextView, 2 * i + 2)
        }
        container.addView(TextView(requireContext()))
        printCanadian(container, history)
        container.addView(TextView(requireContext()))
        return container
    }

    private fun printCanadian(container: LinearLayout, history: History) {
        val canadianClassification = history.toPlayerStatistics()
            .filter { !it.name.contains("NONE") }
            .sortedWith(compareByDescending<PlayerStatistic> { it.goals + it.assists }
                .thenByDescending  { it.goals }
                .thenBy { it.name })

        container.addView(TextView(requireContext()).apply {
            text = "Canadian Classification:"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        })
        canadianClassification.map {
            val candianItemTextView = TextView(requireContext()).apply {
                text = it.toString()
                textSize = 16f
                gravity = Gravity.CENTER
                if (it.team == Team.TEAM2) {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.pink))
                } else {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.light_gray))
                }
            }
            container.addView(candianItemTextView)
        }
    }

    companion object {
        fun newInstance(): MatchSummaryFragment {
            return MatchSummaryFragment()
        }
    }
}
