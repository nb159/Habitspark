package com.example.habitspark.data.dataTypes

enum class InputType {
    TEXT, SELECT, BOOLEAN, NUMBER
}

enum class GoalType(val label: String) {
    HOURS("Hours"),
    REPETITIONS("Repetitions");

    companion object {
        fun fromLabel(label: String): GoalType? =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) }
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
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) }
    }
}

enum class RewardType(val label: String) {
    XP("XP"),
    COINS("Coins");

    companion object {
        fun fromLabel(label: String): RewardType? =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) }
    }
}

enum class PlayerType(val label: String, val description: String) {
    ACHIEVER("Achiever", "Love challenges and mastery"),
    PLAYER("Player", "Motivated by rewards"),
    SOCIALIZER("Socializer", "Enjoy social interaction"),
    PHILANTHROPIST("Philanthropist", "Value helping others"),
    FREE_SPIRIT("Free Spirit", "Crave creativity and autonomy"),
    DISRUPTOR("Disruptor", "Like changing or challenging the system"),
    UNKNOWN("Unknown", "No specific type");

    companion object {
        fun fromLabel(label: String): PlayerType? {
            return entries.firstOrNull { it.label.equals(label, ignoreCase = true) }
        }

        fun fromNameOrNull(name: String): PlayerType? {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
        }
    }
}