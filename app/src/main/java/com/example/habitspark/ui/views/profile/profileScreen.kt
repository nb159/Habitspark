package com.example.habitspark.ui.views.profile

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habitspark.R
import com.example.habitspark.data.dataTypes.LegendItem
import com.example.habitspark.data.dataTypes.PieSlice
import com.example.habitspark.data.dataTypes.PlayerType
import com.example.habitspark.data.models.DifficultyLevel
import com.example.habitspark.data.models.EntryModel
import com.example.habitspark.data.models.Mood
import com.example.habitspark.data.models.UserModel
import com.example.habitspark.domain.featureGate.Feature
import com.example.habitspark.domain.featureGate.FeatureGate
import com.example.habitspark.domain.featureGate.UserGroup
import com.example.habitspark.ui.components.charts.barChart
import com.example.habitspark.ui.components.charts.dataLegend
import com.example.habitspark.ui.components.charts.pieChart
import com.example.habitspark.ui.components.charts.spaceDivider
import com.example.habitspark.ui.components.toolTip.infoTooltip
import com.example.habitspark.ui.components.viewSwitcher.ViewSwitcher
import com.example.habitspark.ui.theme.PrimaryText
import com.example.habitspark.ui.theme.SecondaryText
import com.example.habitspark.ui.theme.SurfaceColor
import com.example.habitspark.ui.views.habits.EntryViewModel
import com.example.habitspark.ui.views.user.UserViewModel
import com.example.habitspark.utils.minutesToDecimalHours
import com.example.habitspark.utils.minutesToHoursMinutes
import com.example.habitspark.utils.textIconValue
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun profileScreen(
    userId: String
) {
    val userViewModel: UserViewModel = viewModel()
    val entryViewModel: EntryViewModel = viewModel()

    val user by userViewModel.userListener.collectAsState()
    val entries = entryViewModel.entries

    var selectedView by remember { mutableStateOf("Statistics") }

    LaunchedEffect(Unit) {
        userViewModel.startUser(userId)
        entryViewModel.fetchEntriesByUserId(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        user?.let {
            userHeader(user = it)
            FeatureGate(it, Feature.LEADERBOARD) {
                ViewSwitcher(
                    selectedView = selectedView,
                    options = listOf("Statistics", "Leader board"),
                    onViewChange = { selectedView = it }
                )
            }
            spaceDivider(20, false)

            if (selectedView == "Statistics") {
                userStatistics(
                    user = it,
                    entries = entries
                )
            } else {
                leaderBoard(user = it)
            }


        }
    }

}

@Composable
fun userHeader(
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
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Light
                    )
                )
                Text(
                    text = user.name,
                    color = PrimaryText,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${user.gender} • ${user.age} • ${user.country}",
                    color = SecondaryText,
                    style = MaterialTheme.typography.bodyMedium
                )

            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun userStatistics(
    user: UserModel,
    entries: List<EntryModel>
) {
    userType(user = user)
    spaceDivider(40, true)
    accountInformation(user = user)
    spaceDivider(40, true)
    accountSummaries(entries = entries)
}


@Composable
fun userType(
    user: UserModel
) {
    val primary = PlayerType.fromNameOrNull(user.primaryType) ?: PlayerType.UNKNOWN
    val secondary = PlayerType.fromNameOrNull(user.secondaryType) ?: PlayerType.UNKNOWN
    val userGroup = UserGroup.fromLabel(user.userGroup) ?: UserGroup.UNKNOWN

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Profile Type",
                color = PrimaryText,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row {
                        Text(
                            text = "1: ${primary.label}",
                            color = PrimaryText,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        infoTooltip(content = primary.description)
                    }

                    spaceDivider(4)

                    Row {
                        Text(
                            text = "2: ${secondary.label}",
                            color = SecondaryText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        infoTooltip(content = secondary.description, iconSize = 14.dp)
                    }
                    spaceDivider(10)
                    Log.d("UserGroup", "User Group: ${user.userGroup}")

                    Text(
                        text = "User Group: ${userGroup.label}",
                        color = SecondaryText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun accountInformation(
    user: UserModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Text(
            text = "Activity Summary",
            color = PrimaryText,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                textIconValue(
                    text = "Habits Tracked: ",
                    value = user.metrics.totalHabitsTracked.toString()
                )
                textIconValue(
                    text = "Difficulty Avg.: ",
                    value = DifficultyLevel.fromValue(user.metrics.difficultyAverage.roundToInt())?.label
                        ?: "N/A"
                )

            }
            spaceDivider(8)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                textIconValue(
                    text = "Entries Logged: ",
                    value = user.metrics.totalEntriesLogged.toString()
                )

                textIconValue(
                    text = "Mood Avg.: ",
                    value = Mood.fromValue(user.metrics.moodAverage)?.emoji ?: "N/A"
                )
            }
            spaceDivider(height = 10)

            textIconValue(
                text = "Total Hours Tracked: ",
                value = minutesToHoursMinutes(user.metrics.totalMinutesSpent),
            )
            textIconValue(
                text = "Average entry Time: ",
                value = minutesToHoursMinutes(user.metrics.averageSessionMinutes.toInt()),
            )

            spaceDivider(height = 20, divide = true, dividerFraction = 0.2f)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                textIconValue(
                    text = "Current ",
                    iconRes = R.drawable.streak,
                    size = 16.dp,
                    value = user.metrics.streak.toString()
                )
                textIconValue(
                    text = "Longest ",
                    iconRes = R.drawable.streak,
                    size = 16.dp,
                    value = user.metrics.highestStreak.toString()
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun accountSummaries(
    entries: List<EntryModel>
) {

    ASMoodSummary(entries = entries)
    spaceDivider(20)
    ASDifficultySummary(entries = entries)
    spaceDivider(20, true, 0.2f)
    ASWeeklyTimeSummary(entries = entries)

}

@Composable
fun ASMoodSummary(
    entries: List<EntryModel>
) {
    val entriesMoodPercentages: List<PieSlice> = entries
        .mapNotNull { it.moodValue?.let { v -> Mood.fromValue(v) } }
        .groupingBy { it }
        .eachCount()
        .map { (mood, count) ->
            PieSlice(
                label = mood.emoji,
                percentage = (count.toFloat() / entries.size) * 100,
                color = mood.color
            )
        }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Text(
            text = "Mood Summary",
            color = PrimaryText,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        Row(
            modifier = Modifier

                .padding(16.dp),
        ) {
            if (entriesMoodPercentages.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(0.75f)
                ) {
                    pieChart(
                        data = entriesMoodPercentages,
                        labelSize = 40f
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))

                dataLegend(
                    items = Mood.entries.map {
                        LegendItem(color = it.color, label = it.emoji)
                    },
                    modifier = Modifier.weight(0.25f),
                    fontSize = 16f
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No mood data available",
                        color = SecondaryText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ASDifficultySummary(
    entries: List<EntryModel>
) {
    val entriesDifficultyPercentages: List<PieSlice> = entries
        .mapNotNull { it.difficultyValue?.let { v -> DifficultyLevel.fromValue(v) } }
        .groupingBy { it }
        .eachCount()
        .map { (difficulty, count) ->
            PieSlice(
                label = difficulty.label,
                percentage = (count.toFloat() / entries.size) * 100,
                color = difficulty.color
            )
        }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Text(
            text = "Difficulty Summary",
            color = PrimaryText,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        Row(
            modifier = Modifier
                .padding(16.dp),
        ) {
            if (entriesDifficultyPercentages.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(0.7f)
                ) {
                    pieChart(
                        data = entriesDifficultyPercentages,
                        labelSize = 40f
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))

                dataLegend(
                    items = DifficultyLevel.entries.map {
                        LegendItem(color = it.color, label = it.label)
                    },
                    modifier = Modifier.weight(0.3f),
                    fontSize = 14f,
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No difficulty data available",
                        color = SecondaryText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ASWeeklyTimeSummary(
    entries: List<EntryModel>
) {


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Text(
            text = "Weekly Activity in Hours",
            color = PrimaryText,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            if (entries.isNotEmpty()) {
                val dailyMinutes: List<Pair<String, List<Double>>> = (0..6).map { daysAgo ->
                    val date = LocalDate.now().minusDays((6 - daysAgo).toLong())
                    val label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

                    val filteredEntries = entries.filter {
                        it.createdDate.toDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate() == date
                    }

                    val totalHours =
                        minutesToDecimalHours(filteredEntries.sumOf { it.minutesSpent }).toDouble()

                    label to listOf(totalHours)
                }
                val maxHours = dailyMinutes.maxOfOrNull { it.second.firstOrNull() ?: 0.0 } ?: 0.0
                barChart(
                    dailyMinutes,
                    4,
                    yAxisMaxMinValues = Pair((maxHours + 3), 0.0)
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No Weekly Activity data available",
                        color = SecondaryText,
                        style = MaterialTheme.typography.bodyMedium,

                    )
                }
            }
        }
    }
}