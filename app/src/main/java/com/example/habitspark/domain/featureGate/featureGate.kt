package com.example.habitspark.domain.featureGate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.habitspark.data.dataTypes.PlayerType
import com.example.habitspark.data.models.UserModel

enum class Feature {
    COINS,
    XP,
    LEADERBOARD,
    ACHIEVEMENTS,
    HIGHLIGHT_PURCHASE,
    ADVANCED_HABIT_STATS,
    // add only non-base features here
}

enum class UserGroup(val label: String) {
    A_ALL("Group_A"),
    B_GATED("Group_B"),
    UNKNOWN("Unknown");

    companion object {
        fun fromLabel(label: String) : UserGroup? {
            return entries.firstOrNull { it.label.equals(label, ignoreCase = true) }
        }
    }
}

val TYPE_RULES: Map<PlayerType, Set<Feature>> = mapOf(
    PlayerType.ACHIEVER   to setOf(Feature.ACHIEVEMENTS, Feature.XP, Feature.ADVANCED_HABIT_STATS),
    PlayerType.PLAYER   to setOf(Feature.ACHIEVEMENTS, Feature.LEADERBOARD),
    PlayerType.FREE_SPIRIT to setOf(Feature.ACHIEVEMENTS, Feature.ADVANCED_HABIT_STATS),
    PlayerType.PHILANTHROPIST to setOf(Feature.ADVANCED_HABIT_STATS),
    PlayerType.SOCIALIZER to setOf(Feature.COINS, Feature.HIGHLIGHT_PURCHASE, Feature.LEADERBOARD),
    PlayerType.DISRUPTOR to setOf(),
)

fun effectiveFeatures(user: UserModel): Set<Feature> {
    // A/B override: Group A sees all extras
    if (UserGroup.fromLabel(user.userGroup) == UserGroup.A_ALL) {
        return enumValues<Feature>().toSet() // all extras
    }

    val primary   = PlayerType.fromNameOrNull(user.primaryType)   ?: PlayerType.UNKNOWN
//    val secondary = PlayerType.fromNameOrNull(user.secondaryType) ?: PlayerType.UNKNOWN


    //return TYPE_RULES[primary].orEmpty() union TYPE_RULES[secondary].orEmpty()
    return TYPE_RULES[primary].orEmpty()

    // Optional: per-user overrides if you ever add a field on the user
        // user.enabledFeatures?.forEach { s ->
        //   runCatching { Feature.valueOf(s.trim().uppercase()) }.getOrNull()?.let { add(it) }
        // }

}

fun canAccess(user: UserModel, feature: Feature): Boolean {
    return when (UserGroup.fromLabel(user.userGroup)) {
        UserGroup.A_ALL -> true
        else -> feature in effectiveFeatures(user)
    }
}

@Composable
fun FeatureGate(
    user: UserModel,
    feature: Feature,
    content: @Composable () -> Unit
) {
    if (canAccess(user, feature)) content()
}

@Composable
fun rememberEffectiveFeatures(user: UserModel): Set<Feature> =
    remember(user.id, user.primaryType, user.secondaryType, user.userGroup) {
        effectiveFeatures(user)
    }