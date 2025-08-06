package com.example.habitspark.data.models

import androidx.compose.ui.graphics.Color
import com.google.firebase.Timestamp
import kotlin.math.roundToInt

data class EntryModel(
    val id: String = "",
    val userId: String = "",
    val habitId: String = "",
    val description: String = "",
    val minutesSpent: Int = 0,
    val moodValue: Int? = null,           // Stored as integer from Mood enum
    val difficultyValue: Int? = null,     // Stored as integer from DifficultyLevel enum
    val createdDate: Timestamp = Timestamp.now()
)

enum class Mood(val emoji: String, val value: Int, val color: Color) {
    TERRIBLE("😣", 1, Color(0xFFD32F2F)),   // Dark Red
    BAD("😞", 2, Color(0xFFF57C00)),        // Orange
    OKAY("😐", 3, Color(0xFFFFC107)),       // Yellow
    GOOD("😊", 4, Color(0xFF81C784)),       // Light Green
    GREAT("🤩", 5, Color(0xFF4DD0E1));      // Cyan

    companion object {
        fun fromValue(value: Int): Mood? = entries.firstOrNull { it.value == value }
        fun fromValue(value: Float): Mood? = entries.firstOrNull { it.value == value.roundToInt() }
    }
}

enum class DifficultyLevel(val label: String, val value: Int, val color: Color) {
    VERY_EASY("Very Easy", 1, Color(0xFFB2FF59)),   // Light Green
    EASY("Easy", 2, Color(0xFFCCFF90)),
    MEDIUM("Medium", 3, Color(0xFFFFFF00)),         // Yellow
    HARD("Hard", 4, Color(0xFFFFAB40)),             // Orange
    VERY_HARD("Very Hard", 5, Color(0xFFFF7043)),
    BRUTAL("Brutal", 6, Color(0xFFD32F2F));         // Red

    companion object {
        fun fromValue(value: Int): DifficultyLevel? = entries.firstOrNull { it.value == value }
    }
}