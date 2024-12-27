package com.example.myapplication.presentation

class HistoryItem(
    private val myTeamScore: Int,
    private val opponentScore: Int,
    private val scorer: String,
    private val ownGoal: Boolean = false,
    private val assistant: String?
) {
    override fun toString(): String {
        return when {
            ownGoal && !assistant.isNullOrEmpty() -> "OG. $scorer ($assistant)"
            ownGoal && assistant.isNullOrEmpty() -> "OG. $scorer"
            !ownGoal && !assistant.isNullOrEmpty() -> "$myTeamScore-$opponentScore $scorer ($assistant)"
            else -> "$myTeamScore-$opponentScore $scorer"
        }
    }
}