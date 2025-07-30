package com.example.habitspark.data.models

import com.google.firebase.Timestamp

data class UserModel(
    val id: String = "",
    val name: String = "",
    val age: Int = 0,
    val email: String = "",
    val gender: String = "",
    val country: String = "",
    val level: Int = 1,
    val xp: Int = 0,
    val primaryType: String = "",
    val secondaryType: String = "",
    val achievements: List<String> = emptyList(),
    val currency: Int = 0,
    val habits: List<String> = emptyList(),
    val metrics: Metrics = Metrics(),
    val createdDate: Timestamp = Timestamp.now(),
)

data class Metrics(
    val totalHabitsTracked: Int = 0,
    val totalEntriesLogged: Int = 0,
    val streak: Int = 0
)
