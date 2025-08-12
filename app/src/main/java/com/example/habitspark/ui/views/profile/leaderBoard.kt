package com.example.habitspark.ui.views.profile

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habitspark.R
import com.example.habitspark.data.dataTypes.LeaderboardRow
import com.example.habitspark.ui.components.charts.spaceDivider
import com.example.habitspark.ui.views.user.UserViewModel
import com.example.habitspark.utils.minutesToHoursMinutes

@Composable
fun leaderBoard() {
    val leaderboardViewModel: LeaderboardViewModel = viewModel()

    val timeSpentRows by leaderboardViewModel.timeSpentRows.collectAsState()


    LaunchedEffect(Unit) {
        leaderboardViewModel.startAllTimeLeaderboard()
    }

    Log.d("LeaderBoard", "Time Spent Rows: $timeSpentRows")


      minutesSpentLeaderbaord(
        timeSpentRows = timeSpentRows,
      )

}

@Composable
fun minutesSpentLeaderbaord(
    timeSpentRows: List<LeaderboardRow>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Total Hours Spent habits",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        spaceDivider(
            height = 16
        )

        timeSpentRows.forEachIndexed { index, item ->
            val isFirst = index == 0
            val fontSize = when (index) {
                0 -> 22.sp
                1 -> 20.sp
                else -> 18.sp
            }
            val fontWeight = if (isFirst) FontWeight.SemiBold else FontWeight.Normal
            val color = if (isFirst) Color(0xFFFFD700) else MaterialTheme.colorScheme.onBackground

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank column (fixed width so everything lines up)
                Box(
                    modifier = Modifier.width(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFirst) {
                        Icon(
                            painter = painterResource(id = R.drawable.cup),
                            contentDescription = "Winner",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "${item.rank}.",
                            fontSize = fontSize,
                            fontWeight = fontWeight,
                            color = color
                        )
                    }
                }

                Text(
                    text = item.name,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )

                Text(
                    text = "${minutesToHoursMinutes(item.minutes)} hours",
                    fontSize = fontSize,
                    fontWeight = if (isFirst) FontWeight.Medium else FontWeight.Normal,
                    color = color,
                    textAlign = TextAlign.End,
                    modifier = Modifier.widthIn(min = 96.dp)
                )
            }
            spaceDivider(
                height = 12,
                divide = index < timeSpentRows.size - 1, // Don't add divider after last item
                dividerFraction = 0.1f
            )

        }
    }
}