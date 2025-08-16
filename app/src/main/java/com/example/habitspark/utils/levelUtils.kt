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

fun calculateXPFromEntry(minutesSpent: Int): Int {
    return (minutesSpent / 10) // 1 XP per 10 minutes
}

fun calculateCoinsFromEntry(minutesSpent: Int): Int {
    val m = maxOf(0, minutesSpent)

    // Base rate: 1 coin per 12 min for the first 120 min
    val effective = minOf(m, 120)
    val base = effective / 12

    // Overtime: after 120 min, 1 coin per 24 min (half rate)
    val overtime = maxOf(0, m - 120) / 24

    // Long-session bonus: +1 coin per full 30-min block, up to +3 (30/60/90 min)
    // Block-based to avoid split abuse
    val longBonus = minOf(m / 30, 3)

    return base + overtime + longBonus
}