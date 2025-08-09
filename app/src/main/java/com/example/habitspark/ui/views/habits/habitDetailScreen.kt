package com.example.habitspark.ui.views.habits

import android.os.Build
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
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habitspark.R
import com.example.habitspark.data.dataTypes.GoalType
import com.example.habitspark.data.models.DifficultyLevel
import com.example.habitspark.data.models.EntryModel
import com.example.habitspark.data.models.HabitModel
import com.example.habitspark.data.models.Mood
import com.example.habitspark.ui.components.charts.barChart
import com.example.habitspark.ui.components.confirmationDialog.confirmationDialog
import com.example.habitspark.ui.theme.PrimaryText
import com.example.habitspark.ui.theme.SecondaryText
import com.example.habitspark.ui.theme.SurfaceColor
import com.example.habitspark.utils.minutesToDecimalHours
import com.example.habitspark.utils.minutesToHoursMinutes
import com.example.habitspark.utils.textIconValue
import ir.ehsannarmani.compose_charts.extensions.format
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun habitDetailsScreen(
    habitId: String,
    userId: String,
) {
    val entryViewModel: EntryViewModel = viewModel()
    val entries by entryViewModel.entriesListener.collectAsState()
    val habit by entryViewModel.habit.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    var selectedView by remember { mutableStateOf("Entries") }
    var entryToDelete by remember { mutableStateOf<EntryModel?>(null) }

    LaunchedEffect(habitId) {
        entryViewModel.startEntriesForHabitListener(habitId)
        entryViewModel.startHabitListener(habitId)
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
                entryList(entries) { entry -> entryToDelete = entry }
            } else {
                habitStatistics(habit, entries)
            }

            if (showDialog) {
                addEntryDialog(
                    userId = userId,
                    habitId = habitId,
                    onDismiss = { showDialog = false },
                    onSave = { entry ->
                        coroutineScope.launch {
                            entryViewModel.addEntry(entry)
                        }
                    },
                )
            }

            if (entryToDelete != null) {
                confirmationDialog(
                    onDismiss = { entryToDelete = null },
                    onProceed = {
                        entryViewModel.deleteEntry(entryToDelete!!)
                        entryToDelete = null

                    }
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
    val completed =
        habit.totalMinutes >= habit.goalTarget * 60 ||
        habit.totalEntries >= habit.goalTarget

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
                    textIconValue(
                        iconRes = R.drawable.target,
                        tint = if (completed) Color.Green else Color.White,
                        value = progressText,
                        valueColor = if (completed) Color.Green else PrimaryText)
                    textIconValue(iconRes = R.drawable.progress, tint = Color.White, value = "${progressPercentage.toInt()}%")
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    textIconValue(iconRes = R.drawable.clock, tint = Color.White, value = "${minutesToHoursMinutes(habit.totalMinutes)} hrs")
                    textIconValue(iconRes = R.drawable.streak, value = "${habit.currentStreak} days")
                }
            }
        }
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
    val difficulty = entry.difficultyValue.let { DifficultyLevel.fromValue(it) }
    val mood = entry.moodValue.let { Mood.fromValue(it) }

    val formattedTimestamp = remember(entry.createdDate) {
        // Format: Jul 19, 10:45 AM
        val sdf = java.text.SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun habitStatistics(habit: HabitModel?, entries: List<EntryModel>) {
    if (habit == null) return


    val dailyMinutes: List<Pair<String, List<Double>>> = (0..6).map { daysAgo ->
        val date = LocalDate.now().minusDays((6 - daysAgo).toLong())
        val label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

        val filteredEntries = entries.filter {
            it.createdDate.toDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate() == date
        }

        val totalMinutes = (filteredEntries.sumOf { it.minutesSpent }).toDouble()

        label to listOf(totalMinutes)
    }
    val maxMinutes = dailyMinutes.maxOfOrNull { it.second.firstOrNull() ?: 0.0 } ?: 0.0

    val averageDifficulty = DifficultyLevel.fromValue(habit.difficultyRatingAverage.toInt())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            textIconValue(
                text = "Highest ",
                iconRes = R.drawable.streak,
                contentDescription = "Highest Streak",
                value = ": ${habit.highestStreak} Days",
                tint = Color.Unspecified
            )
            textIconValue(
                text = "Average ",
                iconRes = R.drawable.clock,
                contentDescription = "Average Session in Minutes",
                value = ": ${habit.averageSessionMinutes.format(1)} Minutes",
                tint = Color.White
            )

        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            textIconValue(
                text = "Average Mood: ",
                contentDescription = "Average Mood",
                value = Mood.fromValue(habit.entryMoodAverage)?.emoji ?: "N/A",
            )

            textIconValue(
                text = "Average Difficulty: ",
                contentDescription = "Average Mood",
                value = averageDifficulty?.label ?: "N/A",
                valueColor = averageDifficulty?.color ?: PrimaryText
            )


        }

        Spacer(modifier = Modifier.height(6.dp))
        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            thickness = 1.dp
        )
        Spacer(modifier = Modifier.height(6.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
            ) {
                Text(
                    text = "Weekly Habit Activity in Minutes",
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(15.dp))
                barChart(
                    dailyMinutes,
                    4,
                    yAxisMaxMinValues = Pair((maxMinutes+20), 0.0)
                )
            }
        }
    }
}
