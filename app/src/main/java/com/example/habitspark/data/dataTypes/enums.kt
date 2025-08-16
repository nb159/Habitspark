package com.example.habitspark.data.dataTypes

import com.google.firebase.Timestamp

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
    ACHIEVER("Achiever", "Loves challenges and mastery"),
    PLAYER("Player", "Is motivated by rewards"),
    SOCIALIZER("Socializer", "Enjoys social interaction"),
    PHILANTHROPIST("Philanthropist", "Values helping others"),
    FREE_SPIRIT("Free Spirit", "Craves creativity and autonomy"),
    DISRUPTOR("Disruptor", "Likes changing or challenging the system"),
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

enum class HighlightStyle(val label: String, val cost: Int, val expiresInDays: Int) {
    GLOW("Glow Name", 20,1 ),
    FRAME("Profile Frame", 75, 1),
    CROWN("Crown Icon", 100, 1)
}

data class HighlightPurchaseResult(
    val newCoins: Int,
    val styleName: String,
    val expiresAt: Timestamp
)

enum class ActionOperation() {
    ADD,
    DELETE,
}
enum class AchievementScope {
    ON_ENTRY,
    ON_HABIT_CREATED
}