package com.example.habitspark.data.dataTypes

import androidx.compose.ui.graphics.Color


data class FormQuestions(
    val id: String,
    val question: String,
    val inputType: InputType,
    val options: List<String>? = null // Only for SELECT or BOOLEAN
)

data class PieSlice(
    val label: String,
    val percentage: Float,
    val color: Color
)

data class LegendItem(
    val color: Color,
    val label: String
)

data class LeaderboardRow(
    val userId: String,
    val name: String,
    val minutes: Int,
    val rank: Int
)