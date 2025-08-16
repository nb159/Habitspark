package com.example.habitspark.domain.stats

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.habitspark.data.dataTypes.AchievementScope
import com.example.habitspark.data.dataTypes.ActionOperation
import com.example.habitspark.data.models.EntryModel
import com.example.habitspark.data.models.HabitModel
import com.example.habitspark.data.models.UserMetrics
import com.example.habitspark.data.models.UserModel
import com.example.habitspark.data.repository.EntryRepository
import com.example.habitspark.data.repository.HabitRepository
import com.example.habitspark.data.repository.UserRepository
import com.example.habitspark.domain.stats.StatsCalculator.updateAverageDouble
import com.example.habitspark.domain.stats.StatsCalculator.updateAverageFloat
import com.example.habitspark.utils.calculateCoinsFromEntry
import com.example.habitspark.utils.calculateXPFromEntry
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
suspend fun onEntryAction(
    entry: EntryModel,
    op: ActionOperation,
    firestore: FirebaseFirestore = Firebase.firestore
) {
    val habitRef = firestore.collection("habits").document(entry.habitId)
    val userRef  = firestore.collection("users").document(entry.userId)

    val entryLocalDate: LocalDate = entry.createdDate.toDate()
        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    Log.d("EntryAction", "Processing entry action: $op")

    firestore.runTransaction { tx ->
        val habit = tx.get(habitRef).toObject(HabitModel::class.java) ?: return@runTransaction null
        val user = tx.get(userRef).toObject(UserModel::class.java) ?: return@runTransaction null

        /**
         * ####################
         * UPDATE HABIT STATS
         * ####################
         */
        val habitOldCount = habit.totalEntries
        val habitNewCount = when (op) {
            ActionOperation.ADD -> habitOldCount + 1
            ActionOperation.DELETE -> maxOf(habitOldCount - 1, 0)
        }
        val habitNewTotalMinutes = when(op) {
            ActionOperation.ADD -> habit.totalMinutes + (entry.minutesSpent)
            ActionOperation.DELETE -> maxOf(habit.totalMinutes - (entry.minutesSpent), 0)
        }

        val moodSample = (entry.moodValue).toFloat()
        val difficultySample = (entry.difficultyValue).toFloat()
        val sessionSample = entry.minutesSpent.toDouble()

        val habitNewAvgSession = updateAverageDouble(habit.averageSessionMinutes, sessionSample, habitOldCount, op)
        val habitNewAvgMood = updateAverageFloat (habit.entryMoodAverage, moodSample, habitOldCount, op)
        val habitNewAvgDifficulty = updateAverageFloat (habit.difficultyRatingAverage, difficultySample, habitOldCount, op)

        val habitPatch  = mutableMapOf<String, Any>(
            HabitModel::totalEntries.name to habitNewCount,
            HabitModel::totalMinutes.name to habitNewTotalMinutes,
            HabitModel::averageSessionMinutes.name to habitNewAvgSession,
            HabitModel::entryMoodAverage.name to habitNewAvgMood,
            HabitModel::difficultyRatingAverage.name to habitNewAvgDifficulty,

            HabitModel::updatedAt.name to Timestamp.now(),
        )
        if (op == ActionOperation.ADD) {
            // Streak/lastEntryAt only on ADD (cheap). On DELETE: skip; recompute after.
            val lastHabitDate = habit.lastEntryAt?.toDate()?.toInstant()
                ?.atZone(ZoneId.systemDefault())?.toLocalDate()
            val newStreak = when {
                lastHabitDate == null -> 1
                lastHabitDate == entryLocalDate -> habit.currentStreak
                lastHabitDate == entryLocalDate.minusDays(1) -> habit.currentStreak + 1
                else -> 1
            }
            habitPatch[HabitModel::currentStreak.name] = newStreak
            habitPatch[HabitModel::highestStreak.name] = maxOf(habit.highestStreak, newStreak)
            habitPatch[HabitModel::lastEntryAt.name]   = entry.createdDate
        }

        // On DELETE we do NOT touch streak/lastEntryAt here.
        tx.update(habitRef, habitPatch)

        /**
         * ####################
         * UPDATE USER STATS
         * ####################
         */

        val metrics = user.metrics
        val userOldEntryCount = metrics.totalEntriesLogged
        val userNewEntryCount = when (op) {
            ActionOperation.ADD -> userOldEntryCount + 1
            ActionOperation.DELETE -> maxOf(userOldEntryCount - 1, 0)
        }
        val userNewTotalMinutes = when(op) {
            ActionOperation.ADD -> metrics.totalMinutesSpent + (entry.minutesSpent)
            ActionOperation.DELETE -> maxOf(metrics.totalMinutesSpent - (entry.minutesSpent), 0)
        }

        val userNewAvgSession = updateAverageDouble(metrics.averageSessionMinutes, sessionSample, userOldEntryCount, op)
        val userNewAvgMood = updateAverageFloat(metrics.moodAverage, moodSample, userOldEntryCount, op)
        val userNewAvgDifficulty = updateAverageFloat(metrics.difficultyAverage, difficultySample, userOldEntryCount, op)

        val userNewXp = when (op) {
            ActionOperation.ADD    -> user.xp + calculateXPFromEntry(entry.minutesSpent)
            ActionOperation.DELETE -> (user.xp - calculateXPFromEntry(entry.minutesSpent)).coerceAtLeast(0)
        }

        val userNewCoins = when (op) {
            ActionOperation.ADD    -> user.coin + calculateCoinsFromEntry(entry.minutesSpent)
            ActionOperation.DELETE -> user.coin - calculateCoinsFromEntry(entry.minutesSpent) // allow negative
        }

        val userPatch = mutableMapOf<String, Any>(
            UserModel::metrics.name + "." + UserMetrics::totalEntriesLogged.name to userNewEntryCount,
            UserModel::metrics.name + "." + UserMetrics::totalMinutesSpent.name to userNewTotalMinutes,
            UserModel::metrics.name + "." + UserMetrics::averageSessionMinutes.name to userNewAvgSession,
            UserModel::metrics.name + "." + UserMetrics::moodAverage.name to userNewAvgMood,
            UserModel::metrics.name + "." + UserMetrics::difficultyAverage.name to userNewAvgDifficulty,
            UserModel::xp.name to userNewXp,
            UserModel::coin.name to userNewCoins
        )
        if (op == ActionOperation.ADD) {
            // Streak/lastEntryAt only on ADD (cheap). On DELETE: skip; recompute after.
            val userLastEntryAt = metrics.lastEntryAt?.toDate()
                ?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate() ?: LocalDate.MIN
            val newStreak = when {
                userLastEntryAt == null -> 1
                userLastEntryAt == entryLocalDate -> metrics.streak
                userLastEntryAt == entryLocalDate.minusDays(1) -> metrics.streak + 1
                else -> 1
            }
            val userNewXp = user.xp + calculateXPFromEntry(entry.minutesSpent)
            val userNewCoins = user.coin + calculateCoinsFromEntry(entry.minutesSpent)

            userPatch[UserModel::xp.name] = userNewXp
            userPatch[UserModel::coin.name] = userNewCoins
            userPatch[UserModel::metrics.name + "." + UserMetrics::streak.name] = newStreak
            userPatch[UserModel::metrics.name + "." + UserMetrics::highestStreak.name] = maxOf(habit.highestStreak, newStreak)
            userPatch[UserModel::metrics.name + "." + UserMetrics::lastEntryAt.name]   = entry.createdDate
        }

        tx.update(userRef, userPatch)
        null
    }.await()
    // For DELETE: recompute exact streak and lastEntryAt (habit + user). Efficient, and only what’s needed.
    if (op == ActionOperation.DELETE) {
            recomputeHabitStats(entry.habitId)
            recomputeUserStreak(entry.userId)
    } else if (op == ActionOperation.ADD) {
        achievementStatsManager(
            userId = entry.userId,
            scope = AchievementScope.ON_ENTRY
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun recomputeHabitStats(
    habitId: String,
    habitRepository: HabitRepository = HabitRepository(Firebase.firestore),
    entryRepository: EntryRepository = EntryRepository(Firebase.firestore),
) {
    val entries = entryRepository.getEntriesForHabit(habitId)

    val totalEntries = entries.size
    val totalMinutes = StatsCalculator.calculateTotalMinutes(entries)
    val avgSession   = StatsCalculator.calculateAverageSessionMinutes(entries)
    val avgMood      = StatsCalculator.calculateAverageMood(entries)
    val avgDifficulty= StatsCalculator.calculateAverageDifficulty(entries)
    val streak       = StatsCalculator.calculateStreak(entries)

    val lastEntryAt: Timestamp? = entries.maxByOrNull { it.createdDate.seconds }?.createdDate

    habitRepository.updateHabitFields(
        habitId,
        mapOf(
            HabitModel::totalEntries.name            to totalEntries,
            HabitModel::totalMinutes.name            to totalMinutes,
            HabitModel::averageSessionMinutes.name   to avgSession,
            HabitModel::entryMoodAverage.name        to avgMood,
            HabitModel::difficultyRatingAverage.name to avgDifficulty,
            HabitModel::currentStreak.name           to streak,
            HabitModel::lastEntryAt.name             to (lastEntryAt ?: FieldValue.delete()),
            HabitModel::updatedAt.name               to Timestamp.now()
        )
    )
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun recomputeUserStreak(
    userId: String,
    entryRepository: EntryRepository = EntryRepository(Firebase.firestore),
    userRepository: UserRepository = UserRepository(Firebase.firestore)
) {
    val entries = entryRepository.getEntriesByUserId(userId).await()
    val streak = StatsCalculator.calculateStreak(entries)
    val lastEntryAt: Timestamp? = entries.maxByOrNull { it.createdDate.seconds }?.createdDate

    userRepository.updateUserMetrics(
        userId = userId,
        metrics = mapOf(
            UserModel::metrics.name + "." + UserMetrics::streak.name to streak,
            UserModel::metrics.name + "." + UserMetrics::lastEntryAt.name to (lastEntryAt ?: FieldValue.delete())
        )
    )
}