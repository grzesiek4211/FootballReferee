package com.example.myapplication.presentation

class Score(var value: Int) {
    fun increment() {
        value++
    }

    fun set(value: Int) {
        this.value = value
    }

    fun copy(): Score {
        return Score(value)
    }

    override fun toString(): String {
        return "$value"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Score

        return value == other.value
    }

    override fun hashCode(): Int {
        return value
    }
}