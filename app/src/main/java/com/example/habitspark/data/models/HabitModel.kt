package com.example.habitspark.data.models

import com.example.habitspark.data.dataTypes.GoalType
import com.google.firebase.Timestamp

data class HabitModel(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val goalType: GoalType = GoalType.HOURS, // "hours" | "count" | "completion"
    val goalTarget: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
    val totalEntries: Int = 0,
    val totalMinutes: Int = 0,
    val currentStreak: Int = 0,
    val highestStreak: Int = 0,
    val averageSessionMinutes: Double = 0.0,
    val difficultyRatingAverage: Float = 0f, // User-rated difficulty (0-5 scale?)
    val entryMoodAverage: Float = 0f, // Average mood score from entries
    val iconUrl: String = "",
    val createdDate: Timestamp = Timestamp.now()
)