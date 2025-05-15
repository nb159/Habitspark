package com.example.habitspark.ui.views.habits

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habitspark.data.models.HabitModel
import com.example.habitspark.data.repository.HabitRepository
import com.example.habitspark.ui.theme.BackgroundColor
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await

@Composable
fun habitDetailsScreen(
    habitId: String,
) {
    val viewModel: EntryViewModel = viewModel()
    val entries by viewModel.entries.collectAsState()
    var habit by remember { mutableStateOf<HabitModel?>(null) }

    LaunchedEffect(habitId) {
        viewModel.fetchEntriesForHabit(habitId)
        habit = HabitRepository.getHabit(habitId).await()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Entry")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Log.d("HabitsDetailScreen", "habit: ${habit.toString()}")
            habit?.let {  habitHeader(it) }
//            entries?.let { entryList(it) }

        }
    }
}

@Composable
fun habitHeader(habit: HabitModel) {
    val progressText = when (habit.goalType) {
        "hours" -> "${habit.totalHours} / ${habit.goalTarget} hrs"
        "repetitions" -> "${habit.totalEntries} / ${habit.goalTarget} times"
        "completion" -> if (habit.totalEntries > 0) "Completed" else "Not started"
        else -> ""
    }

    val progressPercentage = when (habit.goalType) {
        "hours" -> (habit.totalHours / habit.goalTarget).coerceAtMost(1.0) * 100
        "repetitions" -> (habit.totalEntries.toDouble() / habit.goalTarget).coerceAtMost(1.0) * 100
        else -> 0.0
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Goal: $progressText",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Progress: ${"%.0f".format(progressPercentage)}%",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Total Time: ${String.format("%.1f", habit.totalHours)} hrs",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Current Streak: ${habit.currentStreak} days",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

