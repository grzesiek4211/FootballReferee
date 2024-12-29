package com.example.myapplication.presentation

class HistoryItem(
    val score: Score,
    val team: Team,
    private val scorer: String,
    private val ownGoal: Boolean = false,
    val minute: Int,
    private val assistant: String?
) {

    fun deepCopy(): HistoryItem {
        return HistoryItem(
            score = score,
            team = team,
            scorer = scorer,
            ownGoal = ownGoal,
            minute = minute,
            assistant = assistant
        )
    }

    fun scorerToString(): String {
        return when {
            ownGoal && !assistant.isNullOrEmpty() -> "OG. $scorer ($assistant)"
            ownGoal && assistant.isNullOrEmpty() -> "OG. $scorer"
            !ownGoal && !assistant.isNullOrEmpty() -> "$scorer ($assistant)"
            else -> scorer
        }
    }
}

enum class Team {
    TEAM1, TEAM2
}