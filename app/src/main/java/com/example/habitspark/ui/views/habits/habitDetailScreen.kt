package com.example.habitspark.ui.views.habits

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habitspark.data.models.DifficultyLevel
import com.example.habitspark.data.models.EntryModel
import com.example.habitspark.data.models.HabitModel
import com.example.habitspark.data.models.Mood
import com.example.habitspark.ui.theme.PrimaryText
import com.example.habitspark.ui.theme.SecondaryText
import com.example.habitspark.ui.theme.SurfaceColor

@Composable
fun habitDetailsScreen(
    habitId: String,
    userId: String,
) {
    val entryViewModel: EntryViewModel = viewModel()
    val entries = entryViewModel.entries
    val habit by entryViewModel.habit

    LaunchedEffect(habitId) {
        entryViewModel.fetchEntriesForHabit(habitId)
        entryViewModel.fetchHabit(habitId)
    }

    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {showDialog = true},
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
        ) {
            habit?.let {  habitHeader(it) }
            Spacer(modifier = Modifier.height(30.dp))
            entries?.let {
                entryList(
                    it,
                    onEntryDelete = { entryId: String ->
                        entryViewModel.deleteEntry(entryId, habitId)
                    }
                )
            }
            if (showDialog) {
                addEntryDialog(
                    userId = userId,
                    habitId = habitId,
                    onDismiss = { showDialog = false },
                    onSave = { entry ->
                        Log.d("HabitDetailsScreen", "Saving entry: $entry")
                        entryViewModel.addEntry(entry)
                    }
                )
            }

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
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
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

@Composable
fun entryList(entries: List<EntryModel>, onEntryDelete: (entryId: String) -> Unit ={}) {
    if (entries.isEmpty()) {
        Text("No entries yet", color = SecondaryText)
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(entries) { entry ->
            entryItem(entry, onEntryDelete)
        }
    }

}
@Composable
fun entryItem(entry: EntryModel, onEntryDelete: (entryId: String) -> Unit = {}) {
    val difficulty = entry.difficultyValue?.let { DifficultyLevel.fromValue(it) }
    val mood = entry.moodValue?.let { Mood.fromValue(it) }

    val formattedTimestamp = remember(entry.timestamp) {
        // Format: Jul 19, 10:45 AM
        val sdf = java.text.SimpleDateFormat("MMM dd, h:mm a", java.util.Locale.getDefault())
        sdf.format(entry.timestamp.toDate())
    }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically

            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    Text(
                        text = formattedTimestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryText
                    )

                    // Row 2: Duration / Difficulty / Mood
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val difficultyLabel = difficulty?.label
                        val difficultyColor = difficulty?.color ?: PrimaryText

                        Text(
                            text = buildAnnotatedString {
                                append("${entry.minutesSpent ?: 0} minutes")

                                if (difficultyLabel != null) {
                                    append("  /  ")
                                    withStyle(style = SpanStyle(color = difficultyColor)) {
                                        append(difficultyLabel)
                                    }
                                }

                                mood?.let {
                                    append("  /  ")
                                    append(it.emoji)
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryText
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Row 3: Description
                    if (entry.description.isNotBlank()) {
                        Text(
                            text = entry.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText
                        )
                    }
                }

                IconButton(onClick = { onEntryDelete(entry.id) }) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Entry",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
}
