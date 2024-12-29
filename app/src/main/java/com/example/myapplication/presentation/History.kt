package com.example.myapplication.presentation

class History(val history: MutableList<HistoryItem> = mutableListOf()) {
    fun add(historyItem: HistoryItem) {
        history.add(historyItem)
    }

    fun opponentScore(item: HistoryItem) =
        history.subList(0, history.indexOf(item)).lastOrNull { it.team != item.team }?.score ?: Score(0)

    fun removeItem(score: Score, team: Team) {
        history.remove(history.first { it.team == team && it.score == score })
    }

    fun copy(): History {
        val copiedHistory = history.map { it.deepCopy() }.toMutableList()
        return History(copiedHistory)
    }
}