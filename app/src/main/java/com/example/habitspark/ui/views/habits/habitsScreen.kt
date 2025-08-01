package com.example.habitspark.ui.views.habits

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habitspark.R
import com.example.habitspark.data.models.HabitModel
import com.example.habitspark.data.models.Metrics
import com.example.habitspark.data.models.Mood
import com.example.habitspark.data.models.UserModel
import com.example.habitspark.ui.theme.BackgroundColor
import com.example.habitspark.ui.theme.PrimaryAccent
import com.example.habitspark.ui.theme.PrimaryText
import com.example.habitspark.ui.theme.SecondaryText
import com.example.habitspark.ui.theme.SurfaceColor
import com.example.habitspark.ui.views.user.UserViewModel
import com.example.habitspark.utils.minutesToHoursMinutes
import kotlinx.coroutines.launch


data class EntryDialogState(
    val visible: Boolean = false,
    val habitId: String = ""
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun habitsScreen(
    userId: String,
    onHabitClick: (habitId: String) -> Unit = {},
) {
    val userViewModel: UserViewModel = viewModel()
    val habitViewModel: HabitViewModel = viewModel()
    val entryViewModel: EntryViewModel = viewModel()

    val habits = habitViewModel.habits
    val user by userViewModel.user

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        habitViewModel.fetchHabits(userId)
        userViewModel.getUserById(userId)
    }

    var showHabitDialog by remember { mutableStateOf(false) }
    var showEntryDialog by remember { mutableStateOf(EntryDialogState()) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showHabitDialog = true },
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
            user?.let { userHeader(userModel = it, totalHabitHours = habits.sumOf { it.totalMinutes }) }

            Spacer(modifier = Modifier.height(30.dp))

            habitList(
                habits = habits,
                onHabitClick,
                onHabitDelete = { habit ->
                    habitViewModel.deleteHabit(habit)
                    habitViewModel.fetchHabits(user!!.id)
                },
                onAddEntryClicked = { habitId ->
                    showEntryDialog = EntryDialogState(visible = true, habitId = habitId)
                }
            )
            if (showHabitDialog) {
                addHabitDialog(
                    userId = user!!.id,
                    onDismiss = { showHabitDialog = false },
                    onSave = { habit ->
                        habitViewModel.addHabit(habit)
                        habitViewModel.fetchHabits(user!!.id)
                    }
                )
            }
            if (showEntryDialog.visible) {
                addEntryDialog(
                    userId = user!!.id,
                    habitId = showEntryDialog.habitId,
                    onDismiss = { showEntryDialog = EntryDialogState() },
                    onSave = { entry ->
                        coroutineScope.launch {
                            entryViewModel.addEntry(entry)
                            habitViewModel.fetchHabits(userId)
                            userViewModel.getUserById(userId)
                        }
                    }
                )
            }
        }

    }
}

@Composable
fun userHeader(
    userModel: UserModel,
    totalHabitHours: Int?
) {
    val totalHoursOnHabits = totalHabitHours?.let { minutesToHoursMinutes(it) } ?: "0:00"

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
                            text = "${userModel.coin}",
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
                            text = "${userModel.metrics.streak} Days",
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
                            text = "$totalHoursOnHabits hrs",
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
    habits: SnapshotStateList<HabitModel>,
    onHabitClick: (habitId: String) -> Unit = {},
    onHabitDelete: (habit: HabitModel) -> Unit = {},
    onAddEntryClicked: (habitId: String) -> Unit = {}
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
            habitItem(habit = habit, onHabitClick, onHabitDelete, onAddEntryClicked)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun habitItem(
    habit: HabitModel,
    onHabitClick: (habitId: String) -> Unit = {},
    onHabitDelete: (habit: HabitModel) -> Unit = {},
    onAddEntryClicked: (habitId: String) -> Unit = {},
) {
    val totalHabitHours =  minutesToHoursMinutes(habit.totalMinutes)
    val dismissState = rememberDismissState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                //swipe left
                DismissValue.DismissedToStart -> {
                    onHabitDelete(habit)
                    false
                }
                //swipe right
                DismissValue.DismissedToEnd -> {
                    onAddEntryClicked(habit.id)
                    false
                }
                else -> false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.5f } // 50% swipe required

    )

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart, DismissDirection.StartToEnd),
        background = {
            val direction = dismissState.dismissDirection
            val color = when (direction) {
                DismissDirection.EndToStart -> Color.Red
                DismissDirection.StartToEnd -> Color(0xFF4CAF50) // Green for archive
                null -> Color.Transparent
            }

            val icon = when (direction) {
                DismissDirection.EndToStart -> Icons.Default.Delete
                DismissDirection.StartToEnd -> Icons.Default.Add
                null -> null
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = when (direction) {
                    DismissDirection.EndToStart -> Alignment.CenterEnd
                    DismissDirection.StartToEnd -> Alignment.CenterStart
                    else -> Alignment.Center
                },
                ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        },
        dismissContent = {
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
                    )

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
                            text = "$totalHabitHours hrs • Mood: ${Mood.fromValue(habit.entryMoodAverage)?.emoji}",
                            color = SecondaryText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    IconButton(onClick = { onAddEntryClicked(habit.id) }) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Quick Add",
                            tint = PrimaryAccent
                        )
                    }
                }
            }
        }
    )
}

