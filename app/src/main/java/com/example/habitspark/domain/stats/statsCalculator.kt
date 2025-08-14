package com.example.habitspark.domain.stats

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.habitspark.data.dataTypes.ActionOperation
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
        if (entries.isEmpty()) return 0f
        val total = entries.sumOf { it.moodValue }
        return total.toFloat() / entries.size
    }

    fun calculateAverageDifficulty(entries: List<EntryModel>): Float {
        if (entries.isEmpty()) return 0f
        val total = entries.sumOf { it.difficultyValue }
        return total.toFloat() / entries.size
    }

    fun calculateTotalMinutes(entries: List<EntryModel>): Int {
        if (entries.isEmpty()) return 0
        return entries.sumOf { it.minutesSpent }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateStreak(entries: List<EntryModel>): Int {
        if (entries.isEmpty()) return 0

        val dates: Set<LocalDate> = entries.map {
            it.createdDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        }.toSet()

        val today = LocalDate.now()
        val anchor = if (dates.contains(today)) today else dates.maxOrNull()!! // most recent entry day

        var streak = 0
        var d = anchor
        while (dates.contains(d)) {
            streak++
            d = d.minusDays(1)
        }
        return streak
    }

    fun updateAverageDouble(oldAverage: Double, sample: Double, oldCount: Int, op: ActionOperation): Double {
        if (sample == 0.0) return oldAverage // Avoid division by zero
        return when (op) {
            ActionOperation.ADD -> ((oldAverage * oldCount) + sample) / (oldCount + 1).toDouble()
            ActionOperation.DELETE -> if (oldCount <= 1) 0.0 else ((oldAverage * oldCount) - sample) / (oldCount - 1).toDouble()
        }
    }
    fun updateAverageFloat(oldAverage: Float, sample: Float, oldCount: Int, op: ActionOperation): Float {
        if (sample == 0f) return oldAverage // Avoid division by zero
        return when (op) {
            ActionOperation.ADD -> (((oldAverage * oldCount) + sample) / (oldCount + 1).toFloat())
            ActionOperation.DELETE -> if (oldCount <= 1) 0f else (((oldAverage * oldCount) - sample) / (oldCount - 1).toFloat())
        }
    }


}