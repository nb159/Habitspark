package com.example.habitspark.data.dataTypes

enum class InputType {
    TEXT, SELECT, BOOLEAN, NUMBER
}

enum class GoalType(val label: String) {
    HOURS("Hours"),
    REPETITIONS("Repetitions");

    companion object {
        fun fromLabel(label: String): GoalType? =
            values().firstOrNull { it.label.equals(label, ignoreCase = true) }
    }
}