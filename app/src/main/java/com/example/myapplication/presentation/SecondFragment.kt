package com.example.myapplication.presentation


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.R

class SecondFragment : Fragment() {

    private lateinit var scoreLeftTextView: TextView
    private lateinit var scoreRightTextView: TextView
    private lateinit var scoreEditorLayout: View
    private lateinit var secondFragmentLayout: View

    private var scoreLeft: Int = 0
    private var scoreRight: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_second, container, false)

        scoreLeftTextView = view.findViewById(R.id.score_left)
        scoreRightTextView = view.findViewById(R.id.score_right)
        scoreEditorLayout = view.findViewById(R.id.score_editor_layout)
        secondFragmentLayout = view.findViewById(R.id.score_layout)

        // Set initial values
        scoreLeftTextView.text = scoreLeft.toString()
        scoreRightTextView.text = scoreRight.toString()

        // Set up the score editor layout but hide it initially
        scoreEditorLayout.visibility = View.GONE

        // Set click listeners
        scoreLeftTextView.setOnClickListener {
            incrementLeftScore()
        }

        scoreRightTextView.setOnClickListener {
            incrementRightScore()
        }

        scoreLeftTextView.setOnLongClickListener {
            showScoreDialog(scoreLeft, scoreRight, secondFragmentLayout, scoreEditorLayout)
            true
        }

        scoreRightTextView.setOnLongClickListener {
            showScoreDialog(scoreLeft, scoreRight, secondFragmentLayout, scoreEditorLayout)
            true
        }

        return view
    }

    private fun incrementLeftScore() {
        scoreLeft++
        scoreLeftTextView.text = scoreLeft.toString()
    }

    private fun incrementRightScore() {
        scoreRight++
        scoreRightTextView.text = scoreRight.toString()
    }

    private fun showScoreDialog(currentLeftScore: Int, currentRightScore: Int, secondFragmentLayout: View, scoreEditorLayout: View) {
        secondFragmentLayout.visibility = View.GONE
        scoreEditorLayout.visibility = View.VISIBLE
//        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_score, null)
        val scoreLeftTextView = scoreEditorLayout.findViewById<TextView>(R.id.dialog_score_left)
        val scoreRightTextView = scoreEditorLayout.findViewById<TextView>(R.id.dialog_score_right)
        val incrementLeft = scoreEditorLayout.findViewById<TextView>(R.id.increment_left)
        val decrementLeft = scoreEditorLayout.findViewById<TextView>(R.id.decrement_left)
        val incrementRight = scoreEditorLayout.findViewById<TextView>(R.id.increment_right)
        val decrementRight = scoreEditorLayout.findViewById<TextView>(R.id.decrement_right)
        val cancelButton = scoreEditorLayout.findViewById<TextView>(R.id.cancel_button)
        val confirmButton = scoreEditorLayout.findViewById<TextView>(R.id.confirm_button)

        // Set initial values
        scoreLeftTextView.text = currentLeftScore.toString()
        scoreRightTextView.text = currentRightScore.toString()

//        val dialogBuilder = AlertDialog.Builder(requireContext())
//            .setView(dialogView)
//
//        val dialog = dialogBuilder.create()

        incrementLeft.setOnClickListener {
            val newScore = scoreLeftTextView.text.toString().toInt() + 1
            scoreLeftTextView.text = newScore.toString()
        }

        decrementLeft.setOnClickListener {
            val newScore = scoreLeftTextView.text.toString().toInt() - 1
            scoreLeftTextView.text = newScore.toString()
        }

        incrementRight.setOnClickListener {
            val newScore = scoreRightTextView.text.toString().toInt() + 1
            scoreRightTextView.text = newScore.toString()
        }

        decrementRight.setOnClickListener {
            val newScore = scoreRightTextView.text.toString().toInt() - 1
            scoreRightTextView.text = newScore.toString()
        }

        cancelButton.setOnClickListener {
            this.scoreLeftTextView.text = currentLeftScore.toString()
            this.scoreRightTextView.text = currentRightScore.toString()
//            dialog.dismiss()
            scoreEditorLayout.visibility = View.GONE
            secondFragmentLayout.visibility = View.VISIBLE
        }

        confirmButton.setOnClickListener {
            scoreLeft = scoreLeftTextView.text.toString().toInt()
            this.scoreLeftTextView.text = scoreLeft.toString()
            scoreRight = scoreRightTextView.text.toString().toInt()
            this.scoreRightTextView.text = scoreRight.toString()

//            dialog.dismiss()
            scoreEditorLayout.visibility = View.GONE
            secondFragmentLayout.visibility = View.VISIBLE
        }

//        dialog.show()
    }
}