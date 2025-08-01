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

enum class AchievementType(val label: String) {
    HABIT_COUNT("Habit Count"),
    ENTRY_COUNT("Entry Count"),
    STREAK("Streak"),
    TIME_SPENT("Time Spent"),
    MOOD("Mood"),
    DIFFICULTY("Difficulty");

    companion object {
        fun fromLabel(label: String): AchievementType? =
            values().firstOrNull { it.label.equals(label, ignoreCase = true) }
    }
}

enum class RewardType(val label: String) {
    XP("XP"),
    COINS("Coins");

    companion object {
        fun fromLabel(label: String): RewardType? =
            values().firstOrNull { it.label.equals(label, ignoreCase = true) }
    }
}