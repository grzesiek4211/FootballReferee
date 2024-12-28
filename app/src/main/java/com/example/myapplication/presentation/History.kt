package com.example.myapplication.presentation

class History(val history: MutableList<HistoryItem> = mutableListOf()) {
    fun add(historyItem: HistoryItem) {
        history.add(historyItem)
    }

    fun removeItem(scoreTeam1: Score, scoreTeam2: Score) {
        history.remove(history.first { it.team1Score == scoreTeam1 && it.team2Score == scoreTeam2 })
    }

    fun copy(): History {
        val copiedHistory = history.map { it.deepCopy() }.toMutableList()
        return History(copiedHistory)
    }
}