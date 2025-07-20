package com.example.habitspark.data.dataTypes

data class FormQuestions(
    val id: String,
    val question: String,
    val inputType: InputType,
    val options: List<String>? = null // Only for SELECT or BOOLEAN
)