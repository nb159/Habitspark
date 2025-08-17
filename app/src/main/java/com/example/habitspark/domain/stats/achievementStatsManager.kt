package com.example.habitspark.domain.stats

import com.example.habitspark.data.dataTypes.AchievementScope
import com.example.habitspark.data.dataTypes.AchievementType
import com.example.habitspark.data.dataTypes.RewardType
import com.example.habitspark.data.models.AchievementModel
import com.example.habitspark.data.models.UserMetrics
import com.example.habitspark.data.models.UserModel
import com.example.habitspark.data.repository.AchievementRepository
import com.example.habitspark.domain.featureGate.Feature
import com.example.habitspark.domain.featureGate.canAccess
import com.example.habitspark.ui.events.StatsEvent
import com.example.habitspark.ui.events.StatsEventBus
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToInt

suspend fun achievementStatsManager(
    userId: String,
    scope: AchievementScope,
    firestore: FirebaseFirestore = Firebase.firestore,
) {
    val allAchievements = AchievementRepository.fetchAchievements()
    val typesToCheck = when (scope) {
        AchievementScope.ON_ENTRY -> setOf(
            AchievementType.ENTRY_COUNT,
            AchievementType.TIME_SPENT,
            AchievementType.STREAK,
            AchievementType.MOOD,
            AchievementType.DIFFICULTY,
        )
        AchievementScope.ON_HABIT_CREATED -> setOf(
            AchievementType.HABIT_COUNT,
        )
    }
    var userFromTransaction: UserModel? = null

    //gets a firestore pointer to that doc -> attaching get() update() etc to it
    val userRef = firestore.collection("users").document(userId)

    val unlockedNow = firestore.runTransaction { tx ->
        val user = tx.get(userRef).toObject(UserModel::class.java) ?: return@runTransaction null
        userFromTransaction = user
        val metrics = user.metrics
        val unlockedAchievements = user.achievements.keys

        val toEvaluate = allAchievements.filter { it.type in typesToCheck && it.id !in unlockedAchievements }

        val newlyCompleted = toEvaluate.filter { achievement ->
            isAchievementCompleted(achievement, metrics)
        }

        val now = Timestamp.now()
        val gainedXp = newlyCompleted.filter { it.rewardType == RewardType.XP }.sumOf { it.reward }
        val gainedCoins = newlyCompleted.filter { it.rewardType == RewardType.COINS }.sumOf { it.reward }

        val newAchievementsMap = newlyCompleted.associate { it.id to now }

        tx.update(
            userRef,
            mapOf(
                UserModel::achievements.name to (user.achievements + newAchievementsMap),
                UserModel::xp.name to (user.xp + gainedXp),
                UserModel::coin.name to (user.coin + gainedCoins)
            )
        )
        newlyCompleted
    }.await()

    userFromTransaction?.let {
            if (canAccess(it, Feature.ACHIEVEMENTS)) {
                if (!unlockedNow.isNullOrEmpty()) {
                    unlockedNow.forEach { a ->
                        val rewardMessage = when (a.rewardType) {
                            RewardType.XP    -> "${a.reward} XP"
                            RewardType.COINS -> "${a.reward} coins"
                        }
                        StatsEventBus.emit(StatsEvent.AchievementUnlocked("🎉 New Achievement: ${a.title} • $rewardMessage"))
                    }
                    StatsEventBus.emit(StatsEvent.UserDataChanged)
                }
            }
    }
}

fun isAchievementCompleted(
    achievement: AchievementModel,
    metrics: UserMetrics,
): Boolean {
    return when (achievement.type) {
        AchievementType.HABIT_COUNT -> metrics.totalHabitsTracked >= achievement.goal
        AchievementType.ENTRY_COUNT -> metrics.totalEntriesLogged >= achievement.goal
        AchievementType.TIME_SPENT -> metrics.totalMinutesSpent >= achievement.goal
        AchievementType.STREAK -> metrics.streak >= achievement.goal
        AchievementType.MOOD ->
            metrics.totalEntriesLogged >=  achievement.requirement &&
            metrics.moodAverage.roundToInt() >= achievement.goal
        AchievementType.DIFFICULTY ->
            metrics.totalEntriesLogged >= achievement.requirement &&
            metrics.difficultyAverage.roundToInt() >= achievement.goal
    }
}