package com.example.habitspark.ui.views.habits

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habitspark.R
import com.example.habitspark.data.dataTypes.GoalType
import com.example.habitspark.data.models.DifficultyLevel
import com.example.habitspark.data.models.EntryModel
import com.example.habitspark.data.models.HabitModel
import com.example.habitspark.data.models.Mood
import com.example.habitspark.ui.theme.PrimaryText
import com.example.habitspark.ui.theme.SecondaryText
import com.example.habitspark.ui.theme.SurfaceColor
import com.example.habitspark.utils.minutesToDecimalHours
import com.example.habitspark.utils.minutesToHoursMinutes
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun habitDetailsScreen(
    habitId: String,
    userId: String,
) {
    val entryViewModel: EntryViewModel = viewModel()
    val entries = entryViewModel.entries
    val habit by entryViewModel.habit

    val coroutineScope = rememberCoroutineScope()

    var selectedView by remember { mutableStateOf("Entries") }

    LaunchedEffect(habitId) {
        entryViewModel.fetchEntriesForHabit(habitId)
        entryViewModel.fetchHabit(habitId)
    }

    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (selectedView != "Entries") return@Scaffold
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
            ViewSwitcher(selectedView = selectedView, onViewChange = { selectedView = it })
            Spacer(modifier = Modifier.height(8.dp))

            if (selectedView == "Entries") {
                entryList(entries, onEntryDelete = { entry -> entryViewModel.deleteEntry(entry) })
            } else {
                habitStatistics(habit)
            }

            if (showDialog) {
                addEntryDialog(
                    userId = userId,
                    habitId = habitId,
                    onDismiss = { showDialog = false },
                    onSave = { entry ->
                        coroutineScope.launch {
                            entryViewModel.addEntry(entry)
                            entryViewModel.fetchEntriesForHabit(habitId)
                        }
                    },
                )
            }

        }
    }
}

@Composable
fun habitHeader(habit: HabitModel) {

    val progressText = when(habit.goalType) {
        GoalType.HOURS -> {
            val formattedHours = minutesToDecimalHours(habit.totalMinutes)
            "$formattedHours / ${habit.goalTarget} hrs"
        }
        GoalType.REPETITIONS -> "${habit.totalEntries} / ${habit.goalTarget} times"
    }

    val progressPercentage = when (habit.goalType) {
        GoalType.HOURS -> {
            ((habit.totalMinutes.toDouble() / 60) / habit.goalTarget.toDouble()) * 100
        }
        GoalType.REPETITIONS -> (habit.totalEntries.toDouble() / habit.goalTarget).coerceAtMost(1.0) * 100
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = habit.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Stats Section (icons + values)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconRow(iconRes = R.drawable.target, value = progressText, Color.White)  // goal
                    IconRow(iconRes = R.drawable.progress, value = "${progressPercentage.toInt()}%", Color.White)  // progress
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconRow(iconRes = R.drawable.clock, value = "${minutesToHoursMinutes(habit.totalMinutes)} hrs", Color.White)  // time
                    IconRow(iconRes = R.drawable.streak, value = "${habit.currentStreak} days")  // streak
                }
            }
        }
    }
}

@Composable
fun IconRow(@DrawableRes iconRes: Int, value: String, tint: Color = Color.Unspecified) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryText
        )
    }
}

@Composable
fun ViewSwitcher(
    selectedView: String,
    onViewChange: (String) -> Unit
) {
    val options = listOf("Entries", "Statistics")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        options.forEach { label ->
            val isSelected = selectedView == label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onViewChange(label) }
                    .padding(vertical = 6.dp, horizontal = 12.dp)
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color.White else SecondaryText,
                    style = if (isSelected)
                        MaterialTheme.typography.bodyMedium
                    else
                        MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .width(24.dp)
                        .background(if (isSelected) Color.White else Color.Transparent)
                )
            }
        }
    }
}


@Composable
fun entryList(entries: List<EntryModel>, onEntryDelete: (entry: EntryModel) -> Unit ={}) {
    if (entries.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.3f)) // Pushes content slightly down
            Text(
                text = "One Day... or Day One",
                color = SecondaryText,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Start your first entry",
                color = SecondaryText,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.weight(0.7f)) // Fills remaining space below

        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(entries) { entry ->
            entryItem(entry, onEntryDelete)
        }
    }

}
@Composable
fun entryItem(entry: EntryModel, onEntryDelete: (entry: EntryModel) -> Unit = {}) {
    val difficulty = entry.difficultyValue?.let { DifficultyLevel.fromValue(it) }
    val mood = entry.moodValue?.let { Mood.fromValue(it) }

    val formattedTimestamp = remember(entry.createdDate) {
        // Format: Jul 19, 10:45 AM
        val sdf = java.text.SimpleDateFormat("MMM dd, h:mm a", java.util.Locale.getDefault())
        sdf.format(entry.createdDate.toDate())
    }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp),
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
                                append("${entry.minutesSpent} minutes")

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

                IconButton(onClick = { onEntryDelete(entry) }) {
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

@Composable
fun habitStatistics(habit: HabitModel?) {
    if (habit == null) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Example statistics, replace with actual calculations
        Text(
            text = "Total Entries: ${habit.totalEntries}",
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryText
        )
        Text(
            text = "Average Duration: ${minutesToHoursMinutes(habit.totalMinutes / habit.totalEntries)} hrs",
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryText
        )
        Text(
            text = "Current Streak: ${habit.currentStreak} days",
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryText
        )
    }
}