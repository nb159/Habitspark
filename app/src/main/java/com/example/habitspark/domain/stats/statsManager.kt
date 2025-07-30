package com.example.habitspark.domain.stats

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
    suspend fun updateStatsFromEntry(habitId: String, userIdOverride: String? = null) = withContext(Dispatchers.IO) {
        val habit = habitRepository.getHabitById(habitId).await()
        val userId = userIdOverride ?: habit?.userId ?: return@withContext
        val user = userRepository.getUserById(userId).await() ?: return@withContext

        Log.d("StatsManager", "Updating stats for habit: $habit")
        if (habit != null) {
            val entries = entryRepository.getEntriesForHabit(habitId)
            val streak = StatsCalculator.calculateStreak(entries)
            Log.d("StatsManager", "entries: $entries")


            val updatedHabit = habit.copy(
                totalEntries = entries.size,
                totalMinutes = StatsCalculator.calculateTotalMinutes(entries),
                averageSessionMinutes = StatsCalculator.calculateAverageSessionMinutes(entries),
                entryMoodAverage = StatsCalculator.calculateAverageMood(entries),
                difficultyRatingAverage = StatsCalculator.calculateAverageDifficulty(entries),
                currentStreak = streak,
                highestStreak = maxOf(habit.highestStreak, streak),
            )
            Log.d("StatsManager", "$updatedHabit")
            habitRepository.updateHabit(updatedHabit)
        }

        val allUserEntries = entryRepository.getEntriesByUserId(userId).await()
        val allUserHabits = habitRepository.getUserHabits(userId).await()

        val updatedUser = user.copy(
            metrics = user.metrics.copy(
                streak = StatsCalculator.calculateStreak(allUserEntries),
                totalEntriesLogged = allUserEntries.size,
                totalHabitsTracked = allUserHabits.size,
            )
        )
        Log.d("StatsManager", "$updatedUser")

        userRepository.updateUser(updatedUser)
    }

    suspend fun updateUserStats(userId: String) = withContext(Dispatchers.IO) {
        val user = userRepository.getUserById(userId).await() ?: return@withContext

        val entries = entryRepository.getEntriesByUserId(userId).await()
        val updatedUser = user.copy(
            metrics = user.metrics.copy(
                totalEntriesLogged = entries.size,
            )
        )
        Log.d("StatsManager", "Updating user stats for $userId: ${updatedUser.metrics.totalEntriesLogged} entries logged")

        userRepository.updateUser(updatedUser)

    }


}
