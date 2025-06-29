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


    fun toPlayerStatistics(): List<PlayerStatistic> {
        val stats = mutableMapOf<String, PlayerStatistic>()

        for (item in history) {
            if (!item.ownGoal) {
                val scorerName = item.scorer
                val current = stats.getOrDefault(scorerName, PlayerStatistic(scorerName, 0, 0))
                stats[scorerName] = current.copy(goals = current.goals + 1)
            }

            val assistantName = item.assistant
            if (!assistantName.isNullOrBlank()) {
                val current = stats.getOrDefault(assistantName, PlayerStatistic(assistantName, 0, 0))
                stats[assistantName] = current.copy(assists = current.assists + 1)
            }
        }

        return stats.values.toList()
    }
}