package com.example.myapplication.presentation

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class PlayersSetupActivity : AppCompatActivity() {

    private lateinit var playerList: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.players_setup_activity)

        playerList = loadPlayerNamesFromAssets()

        findViewById<Button>(R.id.addButton).setOnClickListener {
            showAddPlayerDialog()
        }

        findViewById<Button>(R.id.deleteButton).setOnClickListener {
            showDeletePlayerDialog()
        }

        findViewById<Button>(R.id.nextButton).setOnClickListener {
            val intent = Intent(this, TeamSetupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showAddPlayerDialog() {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.hint = "New player"

        AlertDialog.Builder(this)
            .setTitle("Add player")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    playerList.add(name)
                    savePlayersToFile(playerList)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeletePlayerDialog() {
        val playersArray = playerList.toTypedArray()
        val selected = mutableListOf<String>()

        AlertDialog.Builder(this)
            .setTitle("Delete player")
            .setMultiChoiceItems(playersArray, null) { _, index, isChecked ->
                val name = playersArray[index]
                if (isChecked) selected.add(name) else selected.remove(name)
            }
            .setPositiveButton("Delete") { _, _ ->
                playerList.removeAll(selected.toSet())
                savePlayersToFile(playerList)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadPlayerNamesFromAssets(): MutableList<String> {
        val file = File(filesDir, "players.json")
        return if (file.exists()) {
            val jsonString = file.readText()
            Gson().fromJson(jsonString, object : TypeToken<MutableList<String>>() {}.type)
        } else {
            val jsonString = assets.open("players.json").bufferedReader().use { it.readText() }
            val list: MutableList<String> = Gson().fromJson(jsonString, object : TypeToken<MutableList<String>>() {}.type)
            savePlayersToFile(list) // Save initial version to filesDir
            list
        }
    }

    private fun savePlayersToFile(players: List<String>) {
        val file = File(filesDir, "players.json")
        file.writeText(Gson().toJson(players))
    }
}

