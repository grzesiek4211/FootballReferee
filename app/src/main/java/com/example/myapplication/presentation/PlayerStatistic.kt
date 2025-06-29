package com.example.myapplication.presentation

data class PlayerStatistic(
    val name: String,
    val goals: Int,
    val assists: Int,
    val team: Team
) {
    override fun toString(): String {
        return "$name G: $goals A: $assists"
    }
}