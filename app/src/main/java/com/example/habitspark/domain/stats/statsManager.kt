package com.example.habitspark.domain.stats

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.habitspark.data.models.EntryModel
import com.example.habitspark.data.models.HabitModel
import com.example.habitspark.data.models.UserModel
import com.example.habitspark.data.repository.EntryRepository
import com.example.habitspark.data.repository.HabitRepository
import com.example.habitspark.data.repository.UserRepository
import com.example.habitspark.utils.toLocalDate
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate

class StatsManager(
    private val habitRepository: HabitRepository = HabitRepository(Firebase.firestore),
    private val entryRepository: EntryRepository = EntryRepository(Firebase.firestore),
    private val userRepository: UserRepository = UserRepository(Firebase.firestore)
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun handleNewEntry(entry: EntryModel) = withContext(Dispatchers.IO) {
        val habit = habitRepository.getHabitById(entry.habitId).await() ?: return@withContext
        val user = userRepository.getUserById(entry.userId).await() ?: return@withContext

        val updatedHabit = updateHabitStats(habit, entry)
        val updatedUser = updateUserStats(user)

        habitRepository.updateHabit(updatedHabit)
        userRepository.updateUser(updatedUser)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun updateHabitStats(habit: HabitModel, entry: EntryModel): HabitModel {
        val newTotalEntries = habit.totalEntries + 1
        val newTotalMinutes = habit.totalMinutes + entry.minutesSpent

        val newAvgSessionMinutes = if (newTotalEntries > 0)
            (habit.averageSessionMinutes * habit.totalEntries + entry.minutesSpent) / newTotalEntries
        else 0.0

        val newMood = entry.moodValue?.let {
            (habit.entryMoodAverage * habit.totalEntries + it) / newTotalEntries
        } ?: habit.entryMoodAverage

        val newDifficulty = entry.difficultyValue?.let {
            (habit.difficultyRatingAverage * habit.totalEntries + it) / newTotalEntries
        } ?: habit.difficultyRatingAverage

        val newStreak = calculateStreak(habit.id)

        return habit.copy(
            totalEntries = newTotalEntries,
            totalMinutes = newTotalMinutes,
            averageSessionMinutes = newAvgSessionMinutes,
            entryMoodAverage = newMood,
            difficultyRatingAverage = newDifficulty,
            currentStreak = newStreak,
            highestStreak = maxOf(habit.highestStreak, newStreak)
        )
    }

    private fun updateUserStats(user: UserModel): UserModel {
        return user.copy(
            metrics = user.metrics.copy(
                totalEntriesLogged = user.metrics.totalEntriesLogged + 1,
                // Optional: Update streakDays here, or do separately
            )
        )
    }

    //This is the source of why we require the api
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun calculateStreak(habitId: String): Int {
        val entries = entryRepository.getEntriesForHabit(habitId)
        if (entries.isEmpty()) return 0

        val sortedDates = entries
            .map { it.createdDate.toDate().toLocalDate() }
            .distinct()
            .sortedDescending()

        val today = LocalDate.now()
        var streak = 0

        for ((i, date) in sortedDates.withIndex()) {
            val expectedDate = today.minusDays(i.toLong())
            if (date == expectedDate) {
                streak++
            } else {
                break
            }
        }

        return streak
    }

}
