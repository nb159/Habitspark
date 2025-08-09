package com.example.habitspark.data.models

import com.google.firebase.Timestamp

data class UserModel(
    val id: String = "",
    val name: String = "",
    val age: Int = 0,
    val email: String = "",
    val gender: String = "",
    val country: String = "",
    val xp: Int = 0,
    val primaryType: String = "",
    val secondaryType: String = "",
    val achievements: Map<String, Timestamp> = emptyMap(),
    val coin: Int = 0,
    val metrics: UserMetrics = UserMetrics(),
    val createdDate: Timestamp = Timestamp.now(),
)

data class UserMetrics(
    val streak: Int = 0,
    val highestStreak: Int = 0,
    val totalEntriesLogged: Int = 0,
    val totalHabitsTracked: Int = 0,
    val totalMinutesSpent: Int = 0,
    val moodAverage: Float = 0f,
    val difficultyAverage: Float = 0f,
    val averageSessionMinutes: Double = 0.0,
    val lastEntryAt: Timestamp? = null
)
