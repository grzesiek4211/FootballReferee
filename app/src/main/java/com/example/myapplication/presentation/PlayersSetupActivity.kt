package com.example.myapplication.presentation

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

        findViewById<Button>(R.id.addButton).setSafeOnClickListener {
            showAddPlayerDialog()
        }

        findViewById<Button>(R.id.deleteButton).setSafeOnClickListener {
            showDeletePlayerDialog()
        }

        findViewById<Button>(R.id.nextButton).setSafeOnClickListener {
            val nextIntent = Intent(this, TeamSetupActivity::class.java)
            nextIntent.putExtra("TIMER_DURATION", intent.getLongExtra("TIMER_DURATION", 0L))
            startActivity(nextIntent)
            finish()
        }
    }

    private fun showAddPlayerDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            hint = getString(R.string.new_player_hint)
            setHintTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
            gravity = Gravity.CENTER
        }

        val titleView = TextView(this).apply {
            text = getString(R.string.dialog_add_title)
            textSize = 16f
            setPadding(0, 32, 0, 16)
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(this@PlayersSetupActivity, android.R.color.white))
        }

        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        builder.setCustomTitle(titleView)
        builder.setView(input)
        builder.setPositiveButton(getString(R.string.add)) { _, _ ->
            val name = input.text.toString().trim()
            if (name.isNotEmpty()) {
                playerList.add(name)
                savePlayersToFile(playerList)
            }
        }
        builder.setNegativeButton(getString(R.string.cancel), null)

        val dialog = builder.create()
        dialog.setOnShowListener {
            val posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            posBtn.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            negBtn.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        }
        dialog.show()
    }

    private fun showDeletePlayerDialog() {
        val playersArray = playerList.toTypedArray()
        val selected = mutableListOf<String>()

        val titleView = TextView(this).apply {
            text = getString(R.string.dialog_delete_title)
            textSize = 16f
            setPadding(0, 32, 0, 16)
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(this@PlayersSetupActivity, android.R.color.white))
        }

        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        builder.setCustomTitle(titleView)
        builder.setMultiChoiceItems(playersArray, null) { _, index, isChecked ->
            val name = playersArray[index]
            if (isChecked) selected.add(name) else selected.remove(name)
        }
        builder.setPositiveButton(getString(R.string.delete)) { _, _ ->
            playerList.removeAll(selected.toSet())
            savePlayersToFile(playerList)
        }
        builder.setNegativeButton(getString(R.string.cancel), null)

        val dialog = builder.create()
        dialog.setOnShowListener {
            val deleteBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val cancelBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            deleteBtn.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            cancelBtn.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))

            deleteBtn.gravity = Gravity.LEFT

            cancelBtn.gravity = Gravity.RIGHT
        }
        dialog.show()
    }

    private fun loadPlayerNamesFromAssets(): MutableList<String> {
        val file = File(filesDir, "players.json")
        return if (file.exists()) {
            val jsonString = file.readText()
            Gson().fromJson(jsonString, object : TypeToken<MutableList<String>>() {}.type)
        } else {
            val jsonString = assets.open("players.json").bufferedReader().use { it.readText() }
            val list: MutableList<String> = Gson().fromJson(jsonString, object : TypeToken<MutableList<String>>() {}.type)
            savePlayersToFile(list)
            list
        }
    }

    private fun savePlayersToFile(players: List<String>) {
        val realPlayers = players.filterNot { it.matches(Regex("Player\\d+")) }.sorted()
        val dummyPlayers = players
            .filter { it.matches(Regex("Player\\d+")) }
            .sortedBy { it.removePrefix("Player").toIntOrNull() ?: Int.MAX_VALUE }

        val sortedPlayers = realPlayers + dummyPlayers
        val file = File(filesDir, "players.json")
        file.writeText(Gson().toJson(sortedPlayers))
    }
}
