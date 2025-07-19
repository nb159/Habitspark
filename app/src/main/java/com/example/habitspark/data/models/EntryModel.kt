package com.example.habitspark.data.models

import androidx.compose.ui.graphics.Color
import com.google.firebase.Timestamp

data class EntryModel(
    val id: String = "",
    val userId: String = "",
    val habitId: String = "",
    val description: String = "",
    val minutesSpent: Int? = null,
    val moodValue: Int? = null,           // Stored as integer from Mood enum
    val difficultyValue: Int? = null,     // Stored as integer from DifficultyLevel enum
    val timestamp: Timestamp = Timestamp.now()
)

enum class Mood(val emoji: String, val value: Int) {
    TERRIBLE("😣", 1),
    BAD("😞", 2),
    OKAY("😐", 3),
    GOOD("😊", 4),
    GREAT("🤩", 5);

    companion object {
        fun fromValue(value: Int): Mood? = entries.firstOrNull { it.value == value }
    }
}

enum class DifficultyLevel(val label: String, val value: Int, val color: Color) {
    VERY_EASY("Very Easy", 0, Color(0xFFB2FF59)),   // Light Green
    EASY("Easy", 1, Color(0xFFCCFF90)),
    MEDIUM("Medium", 2, Color(0xFFFFFF00)),         // Yellow
    HARD("Hard", 3, Color(0xFFFFAB40)),             // Orange
    VERY_HARD("Very Hard", 4, Color(0xFFFF7043)),
    BRUTAL("Brutal", 5, Color(0xFFD32F2F));         // Red

    companion object {
        fun fromValue(value: Int): DifficultyLevel? = entries.firstOrNull { it.value == value }
    }
}