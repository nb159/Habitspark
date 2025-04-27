package com.example.habitspark.data.models

data class UserModel(
    val username: String = "",
    val age: Int = 0,
    val email: String = "",
    val gender : String = "",
    val country: String = "",
    val level: Int = 1,
    val xp: Int = 0,
    val primaryType: String = "",
    val secondaryType: String = "",
    val achievements: List<String> = emptyList(),
    val currency: Int = 0,
    val habits: List<String> = emptyList(),
    val metrics: Metrics = Metrics(),
)

data class Metrics(
    val totalHabitsTracked: Int = 0,
    val totalEntriesLogged: Int = 0,
    val streakDays: Int = 0
)
