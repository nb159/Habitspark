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
    val achievements: Map<String, Timestamp> = emptyMap(),
    val coin: Int = 0,
    val metrics: Metrics = Metrics(),
    val createdDate: Timestamp = Timestamp.now(),
)

data class Metrics(
    val streak: Int = 0,
    val totalEntriesLogged: Int = 0,
    val totalHabitsTracked: Int = 0,
    val totalMinutesSpent: Int = 0,
    val moodAverage: Int = 0,
    val difficultyAverage: Int = 0,
)
