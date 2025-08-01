package com.example.habitspark.ui.views.achievements

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habitspark.data.dataTypes.AchievementType
import com.example.habitspark.data.models.AchievementModel
import com.example.habitspark.data.models.UserModel
import com.example.habitspark.ui.theme.PrimaryAccent
import com.example.habitspark.ui.theme.PrimaryText
import com.example.habitspark.ui.theme.SecondaryText
import com.example.habitspark.ui.theme.SurfaceColor
import com.example.habitspark.ui.views.user.UserViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

val typeOrder = listOf(
    AchievementType.HABIT_COUNT,
    AchievementType.ENTRY_COUNT,
    AchievementType.TIME_SPENT,
    AchievementType.STREAK,
    AchievementType.MOOD,
    AchievementType.DIFFICULTY
)
@Composable
fun achievementsScreen(
    userId: String,
) {

    val achievementViewModel: AchievementViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()

    val achievements = achievementViewModel.achievements
    val user by userViewModel.user

    val isLoading = achievementViewModel.isLoading

     LaunchedEffect(Unit) {
         achievementViewModel.fetchAchievements()
         userViewModel.getUserById(userId)
     }
    Log.d("AchievementsScreen", "UserId: ${userId}, user: $user")

    val sortedAchievements = achievements.sortedWith(
        compareBy<AchievementModel> { achievement ->
            typeOrder.indexOf(achievement.type)
        }.thenBy { achievement ->
            achievement.reward
        }
    )
    user?.let {
        achievementList(
        achievements = sortedAchievements,
        user = it
    )
    }


}

@Composable
fun achievementList(
    achievements: List<AchievementModel>,
    user: UserModel
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(achievements) { achievement ->
            val currentProgress = getUserProgress(achievement, user)
            val isUnlocked  = user.achievements.containsKey(achievement.id)

            val formatter = SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault())
            val unlockedAt = user.achievements[achievement.id]?.let {
                formatter.format(it.toDate())
            }

            achievementItem(
                achievement = achievement,
                currentProgress = currentProgress,
                isUnlocked = isUnlocked,
                unlockedAt = unlockedAt
            )
        }
    }
}

@Composable
fun achievementItem(
    achievement: AchievementModel,
    currentProgress: Int,
    isUnlocked: Boolean,
    unlockedAt: String? = null
) {
    val progressPercent = (currentProgress.toFloat() / achievement.goal).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) SurfaceColor.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isUnlocked) SecondaryText else PrimaryText
            )
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = progressPercent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isUnlocked) Color.Gray else PrimaryAccent,
                trackColor = SurfaceColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$currentProgress / ${achievement.goal} • ${achievement.reward} XP • ${unlockedAt.let {it}}",
                style = MaterialTheme.typography.labelSmall,
                color = SecondaryText
            )
        }
    }
}

fun getUserProgress(achievement: AchievementModel, user: UserModel): Int {
    return when (achievement.type) {
        AchievementType.HABIT_COUNT     -> user.metrics.totalHabitsTracked
        AchievementType.ENTRY_COUNT     -> user.metrics.totalEntriesLogged
        AchievementType.STREAK          -> user.metrics.streak
        AchievementType.TIME_SPENT      -> user.metrics.totalMinutesSpent
        AchievementType.MOOD            -> user.metrics.moodAverage
        AchievementType.DIFFICULTY      -> user.metrics.difficultyAverage
    }
}