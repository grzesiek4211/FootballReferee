package com.example.myapplication.presentation


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.R

class ScoreFragment : Fragment() {

    private lateinit var scoreLeftTextView: TextView
    private lateinit var scoreRightTextView: TextView
    private lateinit var scoreEditorView: View
    private lateinit var secondFragmentView: View

    private var currentLeftScore: Int = 0
    private var currentRightScore: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.score_fragment, container, false)
        initScoreView(view)
        initLeftScore(view)
        initRightScore(view)
        initScoreEditorViewAsInvisible(view)

        return view
    }

    private fun initScoreView(view: View) {
        secondFragmentView = view.findViewById(R.id.score_layout)
    }

    private fun initLeftScore(view: View) {
        scoreLeftTextView = view.findViewById(R.id.score_left)
        scoreLeftTextView.setOnClickListener {
            incrementLeftScore()
        }
        scoreLeftTextView.setOnLongClickListener {
            editScoreView(secondFragmentView, scoreEditorView)
            true
        }
        scoreLeftTextView.text = currentLeftScore.toString()
    }

    private fun incrementLeftScore() {
        currentLeftScore++
        scoreLeftTextView.text = currentLeftScore.toString()
    }

    private fun initRightScore(view: View) {
        scoreRightTextView = view.findViewById(R.id.score_right)
        scoreRightTextView.setOnClickListener {
            incrementRightScore()
        }
        scoreRightTextView.setOnLongClickListener {
            editScoreView(secondFragmentView, scoreEditorView)
            true
        }
        scoreRightTextView.text = currentRightScore.toString()
    }

    private fun incrementRightScore() {
        currentRightScore++
        scoreRightTextView.text = currentRightScore.toString()
    }

    private fun editScoreView(scoreView: View, scoreEditorView: View) {
        scoreView.visibility = View.GONE
        scoreEditorView.visibility = View.VISIBLE
        val scoreLeftTextView = scoreEditorView.findViewById<TextView>(R.id.dialog_score_left)
        val scoreRightTextView = scoreEditorView.findViewById<TextView>(R.id.dialog_score_right)
        val incrementLeft = scoreEditorView.findViewById<TextView>(R.id.increment_left)
        val decrementLeft = scoreEditorView.findViewById<TextView>(R.id.decrement_left)
        val incrementRight = scoreEditorView.findViewById<TextView>(R.id.increment_right)
        val decrementRight = scoreEditorView.findViewById<TextView>(R.id.decrement_right)
        val cancelButton = scoreEditorView.findViewById<TextView>(R.id.cancel_button)
        val confirmButton = scoreEditorView.findViewById<TextView>(R.id.confirm_button)

        scoreLeftTextView.text = currentLeftScore.toString()
        scoreRightTextView.text = currentRightScore.toString()

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
            scoreEditorView.visibility = View.GONE
            scoreView.visibility = View.VISIBLE
        }

        confirmButton.setOnClickListener {
            currentLeftScore = scoreLeftTextView.text.toString().toInt()
            this.scoreLeftTextView.text = currentLeftScore.toString()
            currentRightScore = scoreRightTextView.text.toString().toInt()
            this.scoreRightTextView.text = currentRightScore.toString()

            scoreEditorView.visibility = View.GONE
            scoreView.visibility = View.VISIBLE
        }
    }

    private fun initScoreEditorViewAsInvisible(view: View) {
        scoreEditorView = view.findViewById(R.id.score_editor_layout)
        scoreEditorView.visibility = View.GONE
    }
}