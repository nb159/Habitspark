package com.example.habitspark.utils

fun calculateLevelFromXP(totalXp: Int): Int {
    var level = 1
    var xpForNext = xpForLevel(level)

    while (totalXp >= xpForNext) {
        level++
        xpForNext = xpForLevel(level)
    }

    return level
}

fun xpForLevel(level: Int): Int {
    return (50 * level) + (level * level * 10) // linear + quadratic
}

fun calculateEntryXP(minutesSpent: Int): Int {
    return (minutesSpent / 10) // 1 XP per 10 minutes
}