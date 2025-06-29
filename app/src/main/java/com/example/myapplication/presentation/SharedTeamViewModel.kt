package com.example.myapplication.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedTeamViewModel : ViewModel() {
    private val _team1 = MutableLiveData<List<String>>(emptyList())
    private val _team2 = MutableLiveData<List<String>>(emptyList())

    val team1: LiveData<List<String>> get() = _team1
    val team2: LiveData<List<String>> get() = _team2

    fun initTeams(initialTeam1: List<String>, initialTeam2: List<String>) {
        if (_team1.value.isNullOrEmpty() && _team2.value.isNullOrEmpty()) {
            _team1.value = initialTeam1
            _team2.value = initialTeam2
        }
    }

    fun updateTeams(newTeam1: List<String>, newTeam2: List<String>) {
        _team1.value = newTeam1
        _team2.value = newTeam2
    }
}