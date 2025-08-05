package com.example.habitspark.utils

import android.util.Log

fun calculateLevelFromXP(totalXp: Int): Int {
    var level = 1
    var xpForNext = xpForNextLevel(level)

    while (totalXp >= xpForNext) {
        level++
        xpForNext = xpForNextLevel(level)
    }

    return level
}

fun xpForNextLevel(level: Int): Int {
    return (50 * level) + (level * level * 10) // linear + quadratic
}

fun calculateEntryXP(minutesSpent: Int): Int {
    return (minutesSpent / 10) // 1 XP per 10 minutes
}