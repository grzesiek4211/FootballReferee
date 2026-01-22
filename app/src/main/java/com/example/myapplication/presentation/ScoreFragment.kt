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
import java.time.Instant

class ScoreFragment(
    private val matchStartTime: Instant
) : CurrentTimeFragment(R.layout.score_fragment, R.id.current_time) {

    companion object {
        fun newInstance(matchStartTime: Instant) = ScoreFragment(matchStartTime)
    }

    private lateinit var scoreTextViewTeam1: TextView
    private lateinit var scoreTextViewTeam2: TextView
    private lateinit var scoreFragmentView: View

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var sharedTeamViewModel: SharedTeamViewModel
    private var history: History = History()
    private var editableHistory = history.copy()
    private var backupScoreTeam1: Score = Score(0)
    private var backupScoreTeam2: Score = Score(0)

    private var team1: List<String> = emptyList()
    private var team2: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        sharedTeamViewModel = ViewModelProvider(requireActivity())[SharedTeamViewModel::class.java]

        scoreFragmentView = view.findViewById(R.id.score_layout)
        scoreTextViewTeam1 = view.findViewById(R.id.score_team1)
        scoreTextViewTeam2 = view.findViewById(R.id.score_team2)

        scoreTextViewTeam1.text = "0"
        scoreTextViewTeam2.text = "0"

        // FIX 1: Inicjalizujemy Listenery OD RAZU, nawet jeśli listy są puste.
        // Dzięki temu klikanie zawsze będzie działać.
        setupClickListeners()

        // FIX 2: Obserwujemy tylko po to, żeby zaktualizować zmienne team1 i team2 w tle.
        observeTeams()

        return view
    }

    private fun observeTeams() {
        sharedTeamViewModel.team1.observe(viewLifecycleOwner) { newTeam1 ->
            team1 = newTeam1
        }
        sharedTeamViewModel.team2.observe(viewLifecycleOwner) { newTeam2 ->
            team2 = newTeam2
        }
    }

    private fun setupClickListeners() {
        initScore(scoreTextViewTeam1, Team.TEAM1)
        initScore(scoreTextViewTeam2, Team.TEAM2)
    }

    private fun initScore(scoreTextView: TextView, whichTeam: Team) {
        scoreTextView.setSafeOnClickListener {
            // Używamy aktualnych wartości team1 i team2 w momencie kliknięcia
            if (whichTeam == Team.TEAM1) {
                incrementScore(team1, team2, backupScoreTeam1, scoreTextView, history, whichTeam)
            } else {
                incrementScore(team2, team1, backupScoreTeam2, scoreTextView, history, whichTeam)
            }
            updateSharedHistory()
        }
        scoreTextView.setOnLongClickListener {
            editScoreViewAsDialog()
            true
        }
    }

    private fun incrementScore(
        scoringTeam: List<String>,
        otherTeam: List<String>,
        currentScore: Score,
        scoreTextView: TextView,
        history: History,
        team: Team
    ) {
        // Jeśli listy są puste, kod wejdzie w sekcję 'else' na dole i po prostu doda punkt.
        if (scoringTeam.isNotEmpty() || otherTeam.isNotEmpty()) {
            var scorer: String
            var assistant: String? = null
            val extendedScorerList = mutableListOf("---NONE---")
            extendedScorerList.addAll(scoringTeam)
            extendedScorerList.addAll(otherTeam)
            val extendedAssistantList = mutableListOf("---NONE---")
            extendedAssistantList.addAll(scoringTeam)

            showPlayerListDialog("Select goal scorer", extendedScorerList, otherTeam) { selectedScorer ->
                scorer = selectedScorer

                showPlayerListDialog("Select goal assistant", extendedAssistantList.filter { it == "---NONE---" || it != scorer }, otherTeam) { selectedAssistant ->
                    if (selectedAssistant != "---NONE---") {
                        assistant = selectedAssistant
                        Toast.makeText(requireContext(), "Goal scorer: $scorer ($assistant)", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Goal scorer: $scorer", Toast.LENGTH_SHORT).show()
                    }

                    applyScoreIncrement(currentScore, scoreTextView, history, team, scorer, otherTeam.contains(scorer), assistant)
                }
            }
        } else {
            // Brak zdefiniowanych graczy - po prostu zwiększamy wynik (tak jak chciałeś)
            applyScoreIncrement(currentScore, scoreTextView, history, team, "", false, null)
        }
    }

    // Pomocnicza funkcja, żeby nie powtarzać kodu dodawania punktu
    private fun applyScoreIncrement(
        currentScore: Score,
        scoreTextView: TextView,
        history: History,
        team: Team,
        scorer: String,
        isOwnGoal: Boolean,
        assistant: String?
    ) {
        currentScore.increment()
        val newScoreValue = (scoreTextView.text.toString().toInt() + 1)
        scoreTextView.text = newScoreValue.toString()

        history.add(
            HistoryItem(
                Score(newScoreValue),
                team,
                scorer,
                isOwnGoal,
                java.time.Duration.between(matchStartTime, Instant.now()).toMinutes().toInt() + 1,
                assistant
            )
        )
        updateSharedHistory()
    }

    private fun editScoreViewAsDialog() {
        val scoreEditorView = layoutInflater.inflate(R.layout.score_fragment_editable, null)

        val scoreLeftTextView = scoreEditorView.findViewById<TextView>(R.id.dialog_score_left)
        val scoreRightTextView = scoreEditorView.findViewById<TextView>(R.id.dialog_score_right)
        val incrementLeft = scoreEditorView.findViewById<TextView>(R.id.increment_left)
        val decrementLeft = scoreEditorView.findViewById<TextView>(R.id.decrement_left)
        val incrementRight = scoreEditorView.findViewById<TextView>(R.id.increment_right)
        val decrementRight = scoreEditorView.findViewById<TextView>(R.id.decrement_right)
        val cancelButton = scoreEditorView.findViewById<TextView>(R.id.cancel_button)
        val confirmButton = scoreEditorView.findViewById<TextView>(R.id.confirm_button)

        scoreLeftTextView.text = backupScoreTeam1.toString()
        scoreRightTextView.text = backupScoreTeam2.toString()
        editableHistory = history.copy()

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(scoreEditorView)
        val dialog = builder.create()

        incrementLeft.setSafeOnClickListener {
            incrementScore(team1, team2, backupScoreTeam1.copy(), scoreLeftTextView, editableHistory, Team.TEAM1)
        }

        decrementLeft.setSafeOnClickListener {
            decrementScore(scoreLeftTextView, Team.TEAM1)
        }

        incrementRight.setSafeOnClickListener {
            incrementScore(team2, team1, backupScoreTeam2.copy(), scoreRightTextView, editableHistory, Team.TEAM2)
        }

        decrementRight.setSafeOnClickListener {
            decrementScore(scoreRightTextView, Team.TEAM2)
        }

        cancelButton.setSafeOnClickListener {
            dialog.dismiss()
        }

        confirmButton.setSafeOnClickListener {
            backupScoreTeam1.set(scoreLeftTextView.text.toString().toInt())
            backupScoreTeam2.set(scoreRightTextView.text.toString().toInt())
            scoreTextViewTeam1.text = backupScoreTeam1.toString()
            scoreTextViewTeam2.text = backupScoreTeam2.toString()
            history = editableHistory.copy()
            updateSharedHistory()
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.black)
        dialog.show()
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
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setCustomTitle(titleView)

        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, players) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                val player = players[position]

                view.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                if (otherTeam.contains(player)) {
                    view.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                } else {
                    view.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                }

                return view
            }
        }

        builder.setAdapter(adapter) { _, which ->
            onPlayerSelected(players[which])
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.black)
        dialog.show()
    }

    private fun updateSharedHistory() {
        sharedViewModel.history.postValue(history)
    }
}