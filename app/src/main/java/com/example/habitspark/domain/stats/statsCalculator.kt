package com.example.habitspark.domain.stats

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.habitspark.data.models.EntryModel
import java.time.LocalDate
import java.time.ZoneId

object StatsCalculator {

    fun calculateAverageSessionMinutes(entries: List<EntryModel>): Double {
        if (entries.isEmpty()) return 0.0
        val totalMinutes = entries.sumOf { it.minutesSpent }
        return totalMinutes.toDouble() / entries.size
    }

    fun calculateAverageMood(entries: List<EntryModel>): Float {
        val valid = entries.filter { it.moodValue != null }
        if (valid.isEmpty()) return 0f
        val total = valid.sumOf { it.moodValue ?: 0 }
        return total.toFloat() / valid.size
    }

    fun calculateAverageDifficulty(entries: List<EntryModel>): Float {
        val valid = entries.filter { it.difficultyValue != null }
        if (valid.isEmpty()) return 0f
        val total = valid.sumOf { it.difficultyValue ?: 0 }
        return total.toFloat() / valid.size
    }

    fun calculateTotalMinutes(entries: List<EntryModel>): Int {
        return entries.sumOf { it.minutesSpent ?: 0 }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateStreak(entries: List<EntryModel>): Int {
        if (entries.isEmpty()) return 0

        val sortedDates = entries.map {
            it.createdDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        }.distinct().sortedDescending()

        val today = LocalDate.now()
        var streak = 0

        for ((i, date) in sortedDates.withIndex()) {
            if (date == today.minusDays(i.toLong())) {
                streak++
            } else {
                break
            }
        }

        return streak
    }


}