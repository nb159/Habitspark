package com.example.habitspark.domain.stats

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.habitspark.data.models.EntryModel
import com.example.habitspark.data.repository.EntryRepository
import com.example.habitspark.data.repository.HabitRepository
import com.example.habitspark.data.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class StatsManager(
    private val habitRepository: HabitRepository = HabitRepository(Firebase.firestore),
    private val entryRepository: EntryRepository = EntryRepository(Firebase.firestore),
    private val userRepository: UserRepository = UserRepository(Firebase.firestore)
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun handleNewEntry(entry: EntryModel) = withContext(Dispatchers.IO) {
        val habit = habitRepository.getHabitById(entry.habitId).await() ?: return@withContext
        val user = userRepository.getUserById(entry.userId).await() ?: return@withContext

        val entries = entryRepository.getEntriesForHabit(entry.habitId)
        val streak = StatsCalculator.calculateStreak(entries)

        val updatedHabit = habit.copy(
            totalEntries = entries.size,
            totalMinutes = StatsCalculator.calculateTotalMinutes(entries),
            averageSessionMinutes = StatsCalculator.calculateAverageSessionMinutes(entries),
            entryMoodAverage = StatsCalculator.calculateAverageMood(entries),
            difficultyRatingAverage = StatsCalculator.calculateAverageDifficulty(entries),
            currentStreak = streak,
            highestStreak = maxOf(habit.highestStreak, streak),
        )

        val updatedUser = user.copy(
            metrics = user.metrics.copy(
                totalEntriesLogged = user.metrics.totalEntriesLogged + 1
            )
        )

        habitRepository.updateHabit(updatedHabit)
        userRepository.updateUser(updatedUser)
    }


}
