package com.example.habitspark.domain.stats

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.habitspark.data.dataTypes.AchievementScope
import com.example.habitspark.data.dataTypes.ActionOperation
import com.example.habitspark.data.models.HabitModel
import com.example.habitspark.data.models.UserMetrics
import com.example.habitspark.data.models.UserModel
import com.example.habitspark.data.repository.EntryRepository
import com.example.habitspark.data.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

@RequiresApi(Build.VERSION_CODES.O)
suspend fun onHabitAction(
    habit: HabitModel,
    op: ActionOperation,
    firestore: FirebaseFirestore = Firebase.firestore
) {

    val userRef = firestore.collection("users").document(habit.userId)

    when (op)  {
        ActionOperation.ADD -> {
            userRef.update(UserModel::metrics.name + "." + UserMetrics::totalHabitsTracked.name, FieldValue.increment(1)).await()
            achievementStatsManager(userId = habit.userId, scope = AchievementScope.ON_HABIT_CREATED)

            return
        }
        ActionOperation.DELETE -> {
            // If habit is deleted, we need to recompute user metrics
            Log.d("StatsManager", "wtf")
            recomputeUserMetrics(habit.userId)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun recomputeUserMetrics(
    userId: String,
    entryRepository: EntryRepository = EntryRepository(Firebase.firestore),
    userRepository: UserRepository = UserRepository(Firebase.firestore),
) {
    val allUserEntries = entryRepository.getEntriesByUserId(userId).await()

    if (allUserEntries.isEmpty()) {
        // If no entries, reset metrics to default values
        userRepository.updateUserMetrics(
            userId = userId,
            metrics = mapOf(
                UserModel::metrics.name + "." + UserMetrics::averageSessionMinutes.name to 0.0,
                UserModel::metrics.name + "." + UserMetrics::difficultyAverage.name to 0f,
                UserModel::metrics.name + "." + UserMetrics::moodAverage.name to 0f,
                UserModel::metrics.name + "." + UserMetrics::streak.name to 0,
                UserModel::metrics.name + "." + UserMetrics::totalEntriesLogged.name to 0,
                UserModel::metrics.name + "." + UserMetrics::totalMinutesSpent.name to 0,
                UserModel::metrics.name + "." + UserMetrics::totalHabitsTracked.name to 0,

                UserModel::metrics.name + "." + UserMetrics::lastEntryAt.name to FieldValue.delete()
            )
        )
    Log.d("StatsManager", "entriesEmpty:")
        return
    }

    Log.d("StatsManager", "entries: ${allUserEntries}")
    val newUserAverageSession = StatsCalculator.calculateAverageSessionMinutes(allUserEntries)
    val newUserDifficultyAverage = StatsCalculator.calculateAverageDifficulty(allUserEntries)
    val newUserMoodAverage = StatsCalculator.calculateAverageMood(allUserEntries)
    val newUserStreak = StatsCalculator.calculateStreak(allUserEntries)
    val newUserTotalMinutes = StatsCalculator.calculateTotalMinutes(allUserEntries)
    val newUserTotalEntries = allUserEntries.size

    val lastEntryAt: Timestamp? = allUserEntries.maxByOrNull { it.createdDate.seconds }?.createdDate

    userRepository.updateUserMetrics(
        userId = userId,
        metrics = mapOf(
            UserModel::metrics.name + "." + UserMetrics::averageSessionMinutes.name to newUserAverageSession,
            UserModel::metrics.name + "." + UserMetrics::difficultyAverage.name to newUserDifficultyAverage,
            UserModel::metrics.name + "." + UserMetrics::moodAverage.name to newUserMoodAverage,
            UserModel::metrics.name + "." + UserMetrics::streak.name to newUserStreak,
            UserModel::metrics.name + "." + UserMetrics::totalEntriesLogged.name to newUserTotalEntries,
            UserModel::metrics.name + "." + UserMetrics::totalMinutesSpent.name to newUserTotalMinutes,
            UserModel::metrics.name + "." + UserMetrics::totalHabitsTracked.name to FieldValue.increment(-1),

            UserModel::metrics.name + "." + UserMetrics::lastEntryAt.name to (lastEntryAt ?: FieldValue.delete())
        )
    )
}