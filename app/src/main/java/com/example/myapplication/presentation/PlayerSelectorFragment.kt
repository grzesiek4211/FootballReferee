package com.example.myapplication.presentation

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
import com.example.myapplication.R
import android.graphics.Color

class PlayerSelectorFragment(
    private var team1Players: List<String>,
    private var team2Players: List<String>,
) : Fragment() {

    private lateinit var team1Selector: TextView
    private lateinit var team2Selector: TextView
    private lateinit var confirmButton: Button

    private var selectedTeam1Player: String? = null
    private var selectedTeam2Player: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_player_selector, container, false)

        team1Selector = view.findViewById(R.id.team1Selector)
        team2Selector = view.findViewById(R.id.team2Selector)
        confirmButton = view.findViewById(R.id.confirmButton)

        team1Selector.setOnClickListener {
            showPlayerSelectionDialog("Select Player", team1Players) { selected ->
                selectedTeam1Player = selected
                team1Selector.text = selected
            }
        }

        team2Selector.setOnClickListener {
            showPlayerSelectionDialog("Select Player", team2Players) { selected ->
                selectedTeam2Player = selected
                team2Selector.text = selected
            }
        }

        confirmButton.setOnClickListener {
            if (selectedTeam1Player != null && selectedTeam2Player != null) {
                team1Players = updateTeam(team1Players, selectedTeam1Player!!, selectedTeam2Player!!)
                team2Players = updateTeam(team2Players, selectedTeam2Player!!, selectedTeam1Player!!)

                Toast.makeText(
                    requireContext(),
                    "$selectedTeam1Player replaced with $selectedTeam2Player",
                    Toast.LENGTH_SHORT
                ).show()

                // Reset UI and selections
                selectedTeam1Player = null
                selectedTeam2Player = null
                team1Selector.text = getString(R.string.select_player)
                team2Selector.text = getString(R.string.select_player)

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
