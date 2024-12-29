package com.example.myapplication.presentation


import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R

class ScoreFragment(
    private val team1: List<String>,
    private val team2: List<String>,
) : CurrentTimeFragment(R.layout.score_fragment, R.id.current_time) {

    companion object {
        fun newInstance(team1: ArrayList<String>, team2: ArrayList<String>) =
            ScoreFragment(team1, team2)
    }

    private lateinit var scoreTextViewTeam1: TextView
    private lateinit var scoreTextViewTeam2: TextView
    private lateinit var scoreFragmentView: View

    private lateinit var scoreLeftTextView: TextView
    private lateinit var scoreRightTextView: TextView
    private lateinit var incrementLeft: TextView
    private lateinit var decrementLeft: TextView
    private lateinit var incrementRight: TextView
    private lateinit var decrementRight: TextView
    private lateinit var cancelButton: TextView
    private lateinit var confirmButton: TextView
    private lateinit var scoreEditorView: View

    private lateinit var sharedViewModel: SharedViewModel
    private var history: History = History()
    private var editableHistory = history.copy()
    private var backupScoreTeam1: Score = Score(0)
    private var backupScoreTeam2: Score = Score(0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        initScoreView(view)
        initScoreEditorViewAsInvisible(view)

        return view
    }

    private fun initScoreView(view: View) {
        scoreFragmentView = view.findViewById(R.id.score_layout)
        initScoreTeam1(view)
        initScoreTeam2(view)
    }

    private fun initScoreTeam1(view: View) {
        scoreTextViewTeam1 = view.findViewById(R.id.score_team1)
        initScore(scoreTextViewTeam1, team1, team2, backupScoreTeam1, Team.TEAM1)
    }

    private fun initScoreTeam2(view: View) {
        scoreTextViewTeam2 = view.findViewById(R.id.score_team2)
        initScore(scoreTextViewTeam2, team2, team1, backupScoreTeam2, Team.TEAM2)
    }

    private fun initScore(
        scoreTextView: TextView,
        team: List<String>,
        otherTeam: List<String>,
        currentScore: Score,
        whichTeam: Team
    ) {
        scoreTextView.setOnClickListener {
            incrementScore(team, otherTeam, currentScore, scoreTextView, history, whichTeam)
            updateSharedHistory()
        }
        scoreTextView.setOnLongClickListener {
            editScoreView(scoreFragmentView, scoreEditorView)
            true
        }
        scoreTextView.text = "0"
    }

    private fun incrementScore(
        scoringTeam: List<String>,
        otherTeam: List<String>,
        currentScore: Score,
        scoreTextView: TextView,
        history: History,
        team: Team
    ) {
        var scorer: String
        var assistant: String? = null
        val extendedList = mutableListOf("---NONE---")
        extendedList.addAll(scoringTeam)
        extendedList.addAll(otherTeam)
        showPlayerListDialog("Select goal scorer", extendedList, otherTeam) { selectedScorer ->
            scorer = selectedScorer

            showPlayerListDialog("Select goal assistant", extendedList.filter { it == "---NONE---" || it != scorer }, otherTeam) { selectedAssistant ->
                if (selectedAssistant != "---NONE---") {
                    assistant = selectedAssistant
                    Toast.makeText(requireContext(), "Goal scorer: $scorer ($assistant)", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Goal scorer: $scorer", Toast.LENGTH_SHORT).show()
                }
                currentScore.increment()
                scoreTextView.text = (scoreTextView.text.toString().toInt() + 1).toString()
                history.add(HistoryItem(
                    Score(scoreTextView.text.toString().toInt()),
                    team,
                    scorer,
                    otherTeam.contains(scorer),
                    assistant
                ))
            }
        }
    }

    private fun editScoreView(scoreView: View, scoreEditorView: View) {
        scoreView.visibility = View.GONE
        scoreEditorView.visibility = View.VISIBLE

        scoreLeftTextView.text = backupScoreTeam1.toString()
        scoreRightTextView.text = backupScoreTeam2.toString()

        editableHistory = history.copy()
    }

    private fun initScoreEditorViewAsInvisible(view: View) {
        scoreEditorView = view.findViewById(R.id.score_editor_layout)
        scoreEditorView.visibility = View.GONE

        scoreLeftTextView = scoreEditorView.findViewById(R.id.dialog_score_left)
        scoreRightTextView = scoreEditorView.findViewById(R.id.dialog_score_right)
        incrementLeft = scoreEditorView.findViewById(R.id.increment_left)
        decrementLeft = scoreEditorView.findViewById(R.id.decrement_left)
        incrementRight = scoreEditorView.findViewById(R.id.increment_right)
        decrementRight = scoreEditorView.findViewById(R.id.decrement_right)
        cancelButton = scoreEditorView.findViewById(R.id.cancel_button)
        confirmButton = scoreEditorView.findViewById(R.id.confirm_button)

        incrementLeft.setOnClickListener {
            incrementScore(team1, team2, backupScoreTeam1.copy(), scoreLeftTextView, editableHistory, Team.TEAM1)
        }

        decrementLeft.setOnClickListener {
            decrementScore(scoreLeftTextView, Team.TEAM1)
        }

        incrementRight.setOnClickListener {
            incrementScore(team2, team1, backupScoreTeam2.copy(), scoreRightTextView, editableHistory, Team.TEAM2)
        }

        decrementRight.setOnClickListener {
            decrementScore(scoreRightTextView, Team.TEAM2)
        }

        cancelButton.setOnClickListener {
            this.scoreTextViewTeam1.text = backupScoreTeam1.toString()
            this.scoreTextViewTeam2.text = backupScoreTeam2.toString()
            scoreEditorView.visibility = View.GONE
            scoreFragmentView.visibility = View.VISIBLE
            editableHistory = history.copy()
        }

        confirmButton.setOnClickListener {
            backupScoreTeam1.set(scoreLeftTextView.text.toString().toInt())
            this.scoreTextViewTeam1.text = backupScoreTeam1.toString()
            backupScoreTeam2.set(scoreRightTextView.text.toString().toInt())
            this.scoreTextViewTeam2.text = backupScoreTeam2.toString()

            scoreEditorView.visibility = View.GONE
            scoreFragmentView.visibility = View.VISIBLE
            history = editableHistory.copy()
            updateSharedHistory()
        }
    }

    private fun decrementScore(scoreTextView: TextView, team: Team) {
        val currentScore = scoreTextView.text.toString().toInt()
        if (currentScore != 0) {
            val newScore = currentScore - 1
            editableHistory.removeItem(Score(currentScore), team)
            scoreTextView.text = newScore.toString()
        }
    }

    private fun showPlayerListDialog(
        title: String,
        players: List<String>,
        otherTeam: List<String>,
        onPlayerSelected: (String) -> Unit
    ) {
        val titleView = TextView(requireContext()).apply {
            text = title
            textSize = 14f
            setPadding(32, 24, 32, 0)
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setCustomTitle(titleView)

        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, players) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                val player = players[position]

                if (otherTeam.contains(player)) {
                    view.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                } else {
                    view.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                }

                return view
            }
        }

        builder.setAdapter(adapter) { _, which ->
            val selectedPlayer = players[which]
            onPlayerSelected(selectedPlayer)
        }

        builder.setNegativeButton("Cancel", null)

        builder.create().show()
    }

    private fun updateSharedHistory() {
        sharedViewModel.history = MutableLiveData(history)
    }
}