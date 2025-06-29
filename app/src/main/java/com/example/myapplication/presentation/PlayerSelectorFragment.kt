package com.example.myapplication.presentation

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R

class PlayerSelectorFragment : Fragment() {

    private lateinit var team1Selector: TextView
    private lateinit var team2Selector: TextView
    private lateinit var confirmButton: Button

    private var selectedTeam1Player: String? = null
    private var selectedTeam2Player: String? = null

    private lateinit var sharedTeamViewModel: SharedTeamViewModel
    private var team1Players: List<String> = emptyList()
    private var team2Players: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_player_selector, container, false)

        team1Selector = view.findViewById(R.id.team1Selector)
        team2Selector = view.findViewById(R.id.team2Selector)
        confirmButton = view.findViewById(R.id.confirmButton)

        sharedTeamViewModel = ViewModelProvider(requireActivity())[SharedTeamViewModel::class.java]

        // Observe teams and update local lists + UI
        sharedTeamViewModel.team1.observe(viewLifecycleOwner) {
            team1Players = it
            if (selectedTeam1Player == null) {
                team1Selector.text = getString(R.string.select_player)
            }
        }
        sharedTeamViewModel.team2.observe(viewLifecycleOwner) {
            team2Players = it
            if (selectedTeam2Player == null) {
                team2Selector.text = getString(R.string.select_player)
            }
        }

        team1Selector.setOnClickListener {
            if(team1Players.isNotEmpty()) {
                showPlayerSelectionDialog("Select Player", team1Players) { selected ->
                    selectedTeam1Player = selected
                    team1Selector.text = selected
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Team is empty",
                    Toast.LENGTH_SHORT).show()
            }
        }

        team2Selector.setOnClickListener {
            if(team2Players.isNotEmpty()) {
                showPlayerSelectionDialog("Select Player", team2Players) { selected ->
                    selectedTeam2Player = selected
                    team2Selector.text = selected
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Team is empty",
                    Toast.LENGTH_SHORT).show()
            }
        }

        confirmButton.setOnClickListener {
            if (selectedTeam1Player != null && selectedTeam2Player != null) {
                val updatedTeam1 = updateTeam(team1Players, selectedTeam1Player!!, selectedTeam2Player!!)
                val updatedTeam2 = updateTeam(team2Players, selectedTeam2Player!!, selectedTeam1Player!!)
                sharedTeamViewModel.updateTeams(updatedTeam1, updatedTeam2)

                Toast.makeText(
                    requireContext(),
                    "$selectedTeam1Player replaced with $selectedTeam2Player",
                    Toast.LENGTH_SHORT
                ).show()

                selectedTeam1Player = null
                selectedTeam2Player = null
                team1Selector.text = getString(R.string.select_player)
                team2Selector.text = getString(R.string.select_player)
            } else if (team2Players.isEmpty() || team1Players.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Feature unavailable - team is empty",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please select both players",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return view
    }

    private fun showPlayerSelectionDialog(
        title: String,
        players: List<String>,
        onSelect: (String) -> Unit
    ) {
        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.dialog_item,
            players
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                view.setBackgroundColor(resources.getColor(android.R.color.black, null))
                return view
            }
        }

        val titleView = TextView(requireContext()).apply {
            text = title
            setPadding(32, 24, 32, 24)
            gravity = Gravity.CENTER
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.BLACK)
        }

        AlertDialog.Builder(requireContext())
            .setCustomTitle(titleView)
            .setAdapter(adapter) { _, which ->
                onSelect(players[which])
            }
            .show()
    }

    private fun updateTeam(team: List<String>, playerToRemove: String, playerToAdd: String): List<String> =
        team.toMutableList().also {
            it.remove(playerToRemove)
            it.add(playerToAdd)
        }
}
