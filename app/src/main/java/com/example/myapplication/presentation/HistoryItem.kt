package com.example.myapplication.presentation

class HistoryItem(
    val team1Score: Score,
    val team2Score: Score,
    private val scorer: String,
    private val ownGoal: Boolean = false,
    private val assistant: String?
) {

    fun deepCopy(): HistoryItem {
        return HistoryItem(
            team1Score = team1Score.copy(),
            team2Score = team2Score.copy(),
            scorer = scorer,
            ownGoal = ownGoal,
            assistant = assistant
        )
    }

    override fun toString(): String {
        return "$team1Score-$team2Score ${scorerToString()}"
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