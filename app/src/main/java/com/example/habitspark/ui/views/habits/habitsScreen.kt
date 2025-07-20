package com.example.habitspark.ui.views.habits

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habitspark.R
import com.example.habitspark.data.models.HabitModel
import com.example.habitspark.data.models.Metrics
import com.example.habitspark.data.models.UserModel
import com.example.habitspark.ui.theme.BackgroundColor
import com.example.habitspark.ui.theme.PrimaryAccent
import com.example.habitspark.ui.theme.PrimaryText
import com.example.habitspark.ui.theme.SecondaryText
import com.example.habitspark.ui.theme.SurfaceColor

@Composable
fun habitsScreen(
    user: UserModel,
    onHabitClick: (habitId: String) -> Unit = {},
) {
    val habitViewModel: HabitViewModel = viewModel()
    val habits = habitViewModel.habits

    LaunchedEffect(Unit) {
        habitViewModel.fetchHabits(user.id)
    }

    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = PrimaryAccent,
                contentColor = PrimaryText
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Habit")
            }
        },
        containerColor = BackgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            userHeader(userModel = user, totalHabitHours = habits?.sumOf { it.totalHours })

            Spacer(modifier = Modifier.height(30.dp))

            habitList(userId = user.id, habits = habits, onHabitClick)
            if (showDialog) {
                addHabitDialog(
                    userId = user.id,
                    onDismiss = { showDialog = false },
                    onSave = { habit -> habitViewModel.addHabit(habit) }
                )
            }
        }

    }
}

@SuppressLint("UnrememberedMutableState")
@Preview(showBackground = true)
@Composable
fun habitsScreenPreview() {
    val dummyUser = UserModel(
        name = "SparkUser",
        age = 25,
        email = "spark@demo.com",
        gender = "Non-binary",
        country = "Dreamland",
        level = 3,
        xp = 420,
        primaryType = "Achiever",
        secondaryType = "Explorer",
        achievements = listOf("First Steps", "Daily Streak 5"),
        currency = 1500,
        habits = listOf(),
        metrics = Metrics(totalHabitsTracked = 5, totalEntriesLogged = 40, streakDays = 10)
    )

    habitsScreen(
        user = dummyUser,
    )
}

@Composable
fun userHeader(
    userModel: UserModel,
    totalHabitHours: Double?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Avatar box
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(PrimaryAccent, shape = RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Main information split into 2 columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = userModel.name,
                        color = PrimaryText,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Level ${userModel.level} • ${userModel.xp} XP",
                        color = SecondaryText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.coin_stack),
                            contentDescription = "Currency",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${userModel.currency}",
                            color = PrimaryText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.streak),
                            contentDescription = "Streak",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${userModel.metrics.streakDays} Days",
                            color = PrimaryText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.clock),
                            contentDescription = "Total Hours",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${String.format("%.1f", totalHabitHours)} hrs",
                            color = PrimaryText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun habitList(
    userId: String,
    habits: SnapshotStateList<HabitModel>,
    onHabitClick: (habitId: String) -> Unit = {}
) {
    if (habits.isEmpty()) {
        Text(
            text = "No habits found. Start tracking your progress!",
            color = SecondaryText,
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(habits) { habit ->
            habitItem(userId = userId ,habit = habit, onHabitClick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun habitItem(
    userId: String,
    habit: HabitModel,
    onHabitClick: (habitId: String) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = { onHabitClick(habit.id) },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(PrimaryAccent, shape = RoundedCornerShape(8.dp))
            ) {
                // Habit Icon Placeholder
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = habit.name,
                    color = PrimaryText,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Total: ${habit.totalHours} hrs • Difficulty: ${habit.difficultyRatingAverage}",
                    color = SecondaryText,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = { Log.d("HabitItem", "Clicked on ${habit.name}")}) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Quick Add",
                    tint = PrimaryAccent
                )
            }
        }
    }
}
