package com.example.myapplication.presentation

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import java.time.Instant

private const val TEAM_PLAYERS_NUMBER = 7

class TeamSetupActivity : AppCompatActivity() {

    companion object {
        val playerNames: List<String> = listOf(
            "Bartłomiej M.", "Bartosz G.", "Damian W.", "Dave Pe", "Eryk S.", "Filip N.", "Filip O.", "Grzegorz K.",
            "Hubert J.", "Igor S.", "Jakub W.", "Jan O.", "Jan S.", "Janusz G.", "Jarek J.", "Joannis P.",
            "Julian T.", "Kacper K.", "Kamil S.", "Karol S.", "Konrad O.", "Konrad R.", "Konrad S.", "Krzysztof Sz.",
            "Kuba G.", "Maciej P.", "Marek Cz.", "Marek S.", "Martin S.", "Mateusz Ł.", "Mateusz M.", "Mateusz R.",
            "Michał Z.", "Olaf Ś.", "Przemek P.", "Przemo M.", "Robert S.", "Sebastian F.", "Tomasz K.",
            "Vitalii V.", "Wojtek G.",
            "Player1", "Player2", "Player3", "Player4", "Player5", "Player6", "Player7",
            "Player8", "Player9", "Player10", "Player11", "Player12", "Player13", "Player14"
        )
    }

    private val team1: MutableList<String> = mutableListOf()
    private val team2: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.team_setup_activity)

        val team1RecyclerView: RecyclerView = findViewById(R.id.myTeamRecyclerView)
        val team2RecyclerView: RecyclerView = findViewById(R.id.opponentRecyclerView)
        val addTeam1Members: Button = findViewById(R.id.addMyTeamMembers)
        val addTeam2Members: Button = findViewById(R.id.addOpponentTeamMembers)
        val startButton: Button = findViewById(R.id.startButton)

        val team1Adapter = TeamAdapter(team1)
        val team2Adapter = TeamAdapter(team2)

        team1RecyclerView.layoutManager = LinearLayoutManager(this)
        team1RecyclerView.adapter = team1Adapter

        team2RecyclerView.layoutManager = LinearLayoutManager(this)
        team2RecyclerView.adapter = team2Adapter

        val spacingInDp = resources.getDimensionPixelSize(R.dimen.recycler_spacing)
        team1RecyclerView.addItemDecoration(SpacingItemDecoration(spacingInDp))
        team2RecyclerView.addItemDecoration(SpacingItemDecoration(spacingInDp))

        addTeam1Members.setOnClickListener {
            showPlayerSelectionDialog(
                title = getString(R.string.select_my_team),
                team = team1,
                adapter = team1Adapter,
                otherTeam = team2
            )
        }

        addTeam2Members.setOnClickListener {
            showPlayerSelectionDialog(
                title = getString(R.string.select_opponents),
                team = team2,
                adapter = team2Adapter,
                otherTeam = team1
            )
        }

        startButton.setOnClickListener {
                intent.setClass(this, MainActivity::class.java)
                intent.putStringArrayListExtra("MY_TEAM", ArrayList(team1))
                intent.putStringArrayListExtra("OPPONENT_TEAM", ArrayList(team2))
                intent.putExtra("MATCH_START_TIME", Instant.now().toString())
                startActivity(intent)
                finish()  // Close this activity to prevent going back to it
        }
    }

    private fun showPlayerSelectionDialog(
        title: String,
        team: MutableList<String>,
        adapter: TeamAdapter,
        otherTeam: MutableList<String>
    ) {
        val availablePlayers = playerNames.filter { it !in otherTeam }
        val selectedPlayers = mutableListOf<String>()
        selectedPlayers.addAll(team)

        val titleView = TextView(this).apply {
            text = title
            textSize = 14f
            setPadding(0, 16, 0, 0)
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(this@TeamSetupActivity, android.R.color.white))
        }

        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        builder.setCustomTitle(titleView)

        val checkedItems = BooleanArray(availablePlayers.size) { index ->
            availablePlayers[index] in team
        }

        builder.setMultiChoiceItems(
            availablePlayers.toTypedArray(),
            checkedItems
        ) { dialog, which, isChecked ->
            val selectedItem = availablePlayers[which]
            val alertDialog = dialog as AlertDialog

            if (isChecked) {
                if (selectedPlayers.size < TEAM_PLAYERS_NUMBER) {
                    selectedPlayers.add(selectedItem)
                } else {
                    checkedItems[which] = false
                    alertDialog.listView.setItemChecked(which, false)
                    Toast.makeText(
                        this,
                        getString(R.string.toast_team_setup, TEAM_PLAYERS_NUMBER),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                selectedPlayers.remove(selectedItem)
            }

            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.text = getString(
                R.string.add_players_button_text,
                selectedPlayers.size,
                TEAM_PLAYERS_NUMBER
            )
            positiveButton.isEnabled = selectedPlayers.size == TEAM_PLAYERS_NUMBER
        }

        builder.setPositiveButton(
            getString(R.string.add_players_button_text, 0, TEAM_PLAYERS_NUMBER)
        ) { _, _ ->
            if (selectedPlayers.isNotEmpty()) {
                team.clear()
                team.addAll(selectedPlayers)
                adapter.notifyDataSetChanged()
            }
        }

        builder.setNegativeButton(getString(R.string.cancel_button), null)

        val dialog = builder.create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            positiveButton.text = getString(
                R.string.add_players_button_text,
                selectedPlayers.size,
                TEAM_PLAYERS_NUMBER
            )
            positiveButton.isEnabled = selectedPlayers.size == TEAM_PLAYERS_NUMBER
            positiveButton.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            positiveButton.setPadding(32, 0, 32, 0)
            positiveButton.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))

            negativeButton.gravity = Gravity.END or Gravity.CENTER_VERTICAL
            negativeButton.setPadding(32, 0, 32, 0)
            negativeButton.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        }


        dialog.show()
    }

}
