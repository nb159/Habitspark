package com.example.habitspark.ui.views.habits

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habitspark.R
import com.example.habitspark.data.models.HabitModel
import com.example.habitspark.data.models.Mood
import com.example.habitspark.data.models.UserModel
import com.example.habitspark.domain.featureGate.Feature
import com.example.habitspark.domain.featureGate.FeatureGate
import com.example.habitspark.ui.components.confirmationDialog.confirmationDialog
import com.example.habitspark.ui.events.StatsEvent
import com.example.habitspark.ui.events.StatsEventBus
import com.example.habitspark.ui.theme.BackgroundColor
import com.example.habitspark.ui.theme.PrimaryAccent
import com.example.habitspark.ui.theme.PrimaryText
import com.example.habitspark.ui.theme.SecondaryText
import com.example.habitspark.ui.theme.SurfaceColor
import com.example.habitspark.ui.views.user.UserViewModel
import com.example.habitspark.utils.calculateLevelFromXP
import com.example.habitspark.utils.minutesToHoursMinutes
import com.example.habitspark.utils.textIconValue
import com.example.habitspark.utils.xpForNextLevel
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

    val habits by habitViewModel.habitListener.collectAsState()
    val user by userViewModel.userListener.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        habitViewModel.startHabits(userId)
        userViewModel.startUser(userId)

        StatsEventBus.events.collect { event ->
            if (event is StatsEvent.UserDataChanged) {
                userViewModel.getUserById(userId)
            }
        }
    }

    var showHabitDialog by remember { mutableStateOf(false) }
    var showEntryDialog by remember { mutableStateOf(EntryDialogState()) }
    var habitToDelete by remember { mutableStateOf<HabitModel?>(null) }

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
            user?.let { compactUserHeader(user = it) }

            Spacer(modifier = Modifier.height(30.dp))

            habitList(
                habits = habits,
                onHabitClick,
                onHabitDelete = { habit ->
                    habitToDelete = habit
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
                        }
                    }
                )
            }

            if (habitToDelete != null) {
                confirmationDialog(
                    onDismiss = { habitToDelete = null },
                    onProceed = {
                        coroutineScope.launch {
                            Log.d("HabitsScreen", "Deleting habit: ${habitToDelete}")
                            habitViewModel.deleteHabit(habitToDelete!!)
                            habitToDelete = null
                        }

                    }
                )
            }
        }

    }
}

@Composable
fun compactUserHeader(
    user: UserModel
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
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(0.7f)

            ) {
                Text(
                    text = "Welcome Back",
                    color = SecondaryText,
                    style = MaterialTheme.typography.titleSmall.copy(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Light)
                )
                Text(
                    text = user.name,
                    color = PrimaryText,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                FeatureGate(user, Feature.XP) {
                    userLevelAndProgressBar(user.xp)
                }

            }

            Spacer(modifier = Modifier.width(60.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.weight(0.3f)
            ) {
                FeatureGate(user, Feature.COINS) {
                    textIconValue(
                        iconRes = R.drawable.coin_stack,
                        contentDescription = "Currency",
                        size = 18.dp,
                        value = "${user.coin}",
                    )
                }

                textIconValue(
                    iconRes = R.drawable.streak,
                    contentDescription = "Streak",
                    size = 18.dp,
                    value = "${user.metrics.streak} Days",
                )
                textIconValue(
                    iconRes = R.drawable.clock,
                    contentDescription = "Total Hours",
                    tint = Color.White,
                    size = 18.dp,
                    value = "${minutesToHoursMinutes(user.metrics.totalMinutesSpent)} hrs",
                )

            }
        }
    }
}

@Composable
fun userLevelAndProgressBar(
    xp: Int
) {
    /**
     * Calculates the user's level based on their total XP.
     * eg. Current level 7 with 800 XP
     * xpforNextLevel(7) = 840 => total XP needed to get to level 8
     * xpforNextLevel(6) = 660 => total XP needed to get to level 7
     */
    val userLevel = calculateLevelFromXP(xp)
    val xpForNextLevel = xpForNextLevel(userLevel) //total xp needed to get to next level
    val xpForPreviousLevel = xpForNextLevel(userLevel - 1) //total xp needed to get to current level eg.
    val TotalXpBetweenLevel = xpForNextLevel - xpForPreviousLevel //total xp needed to get to next level from current level

    val xpRemainingForNextLevel = xp - xpForPreviousLevel //xp remaining to get to next level

    val normalizedProgress = xpRemainingForNextLevel.toFloat() / TotalXpBetweenLevel.toFloat()

    val animatedProgress by animateFloatAsState(
        targetValue = normalizedProgress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ), label = ""
    )

    Text(
        text = "Level $userLevel • $xp XP",
        color = SecondaryText,
        style = MaterialTheme.typography.bodySmall
    )
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color.Green.copy(alpha = 0.5f),
            trackColor = SurfaceColor
        )

        Text(
            text = "$xpForNextLevel XP",
            color = SecondaryText,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.End) // aligns under the end of the progress bar
                .padding(top = 2.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun habitList(
    habits: List<HabitModel>,
    onHabitClick: (habitId: String) -> Unit = {},
    onHabitDelete: (habit: HabitModel) -> Unit = {},
    onAddEntryClicked: (habitId: String) -> Unit = {}
) {
    if (habits.isEmpty()) {
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
                text = "Start your Habit",
                color = SecondaryText,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.weight(0.7f)) // Fills remaining space below

        }
        return
    }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                habits.sortedByDescending { it.updatedAt },
                key = { it.id }
            ) { habit ->
                habitItem(
                    modifier = Modifier.animateItemPlacement(),
                    habit = habit,
                    onHabitClick = onHabitClick,
                    onHabitDelete = onHabitDelete,
                    onAddEntryClicked = onAddEntryClicked,
             )
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun habitItem(
    modifier: Modifier = Modifier,
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
        modifier = modifier,
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
                            text = buildAnnotatedString {
                                append("$totalHabitHours hrs • ")
                                append("Mood Avg.: ${Mood.fromValue(habit.entryMoodAverage)?.emoji ?: "N/A"}")
                            },
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

