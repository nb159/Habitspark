package com.example.habitspark.data.models

import com.example.habitspark.data.dataTypes.AchievementType
import com.example.habitspark.data.dataTypes.RewardType

data class AchievementModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: AchievementType = AchievementType.HABIT_COUNT,
    val goal: Int = 0,
    val requirement: Int = 0,
    val rewardType: RewardType = RewardType.XP,
    val reward: Int = 0,
)
