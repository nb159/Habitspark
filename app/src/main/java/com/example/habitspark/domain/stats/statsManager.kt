package com.example.habitspark.domain.stats

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.habitspark.data.dataTypes.AchievementType
import com.example.habitspark.data.dataTypes.RewardType
import com.example.habitspark.data.models.AchievementModel
import com.example.habitspark.data.models.EntryModel
import com.example.habitspark.data.models.HabitModel
import com.example.habitspark.data.models.UserModel
import com.example.habitspark.data.repository.AchievementRepository
import com.example.habitspark.data.repository.EntryRepository
import com.example.habitspark.data.repository.HabitRepository
import com.example.habitspark.data.repository.UserRepository
import com.example.habitspark.ui.events.StatsEventBus
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class StatsManager(
    private val habitRepository: HabitRepository = HabitRepository(Firebase.firestore),
    private val entryRepository: EntryRepository = EntryRepository(Firebase.firestore),
    private val userRepository: UserRepository = UserRepository(Firebase.firestore),
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateStatsFromEntry(habitId: String, userIdOverride: String? = null) = withContext(Dispatchers.IO) {
        val habit = habitRepository.getHabitById(habitId).await()
        val userId = userIdOverride ?: habit?.userId ?: return@withContext
        val user = userRepository.getUserById(userId).await() ?: return@withContext

        if (habit != null) {
            val entries = entryRepository.getEntriesForHabit(habitId)
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
            habitRepository.updateHabit(updatedHabit)
        }

        val allUserEntries = entryRepository.getEntriesByUserId(userId).await()
        val allUserHabits = habitRepository.getUserHabits(userId).await()

        val updatedUser = user.copy(
            metrics = user.metrics.copy(
                streak = StatsCalculator.calculateStreak(allUserEntries),
                totalEntriesLogged = allUserEntries.size,
                totalHabitsTracked = allUserHabits.size,
                difficultyAverage = StatsCalculator.calculateAverageDifficulty(allUserEntries),
                moodAverage = StatsCalculator.calculateAverageMood(allUserEntries),
                totalMinutesSpent = StatsCalculator.calculateTotalMinutes(allUserEntries)
            )
        )

        userRepository.updateUser(updatedUser)

        checkAndUnlockAchievements(updatedUser, allUserHabits, allUserEntries)
    }

    private suspend fun checkAndUnlockAchievements(
        user: UserModel,
        habits: List<HabitModel>,
        entries: List<EntryModel>
    ) {
        val unlockedUserAchievements = user.achievements.keys
        val achievements = AchievementRepository.fetchAchievements()


        val newAchievements = achievements.filter {
            it.id !in unlockedUserAchievements && isAchievementCompleted(it, user, habits, entries)
        }

        if (newAchievements.isNotEmpty()){
            val now = Timestamp.now()

            val newXp = user.xp + newAchievements.filter { it.rewardType == RewardType.XP }.sumOf { it.reward }
            val newCoin = user.coin + newAchievements.filter { it.rewardType == RewardType.COINS }.sumOf { it.reward }
            val timeStampedAchievements = newAchievements.associate { it.id to  now }

            user.copy(
                achievements = user.achievements + timeStampedAchievements,
                xp = newXp,
                coin = newCoin
            ).also { updatedUser ->
                Log.d("StatsManager", "new user: $updatedUser")
                userRepository.updateUser(updatedUser)
            }

            newAchievements.forEach {
                val rewardMessage = when (it.rewardType) {
                    RewardType.XP -> "${it.reward} XP"
                    RewardType.COINS -> "${it.reward} coins"
                }
                StatsEventBus.emit("🎉 New Achievement: ${it.title} • $rewardMessage")
            }
        }

    }
    private fun isAchievementCompleted(
        achievement: AchievementModel,
        user: UserModel,
        habits: List<HabitModel>,
        entries: List<EntryModel>
    ): Boolean{
        return when(achievement.type) {
            AchievementType.HABIT_COUNT -> habits.size >= achievement.goal
            AchievementType.ENTRY_COUNT -> entries.size >= achievement.goal
            AchievementType.TIME_SPENT -> user.metrics.totalMinutesSpent >= achievement.goal
            AchievementType.STREAK -> user.metrics.streak >= achievement.goal
            AchievementType.MOOD -> user.metrics.moodAverage >= achievement.goal
            AchievementType.DIFFICULTY -> user.metrics.difficultyAverage >= achievement.goal
        }
    }
}
