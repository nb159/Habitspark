package com.example.habitspark.ui.views.profile

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habitspark.R
import com.example.habitspark.data.dataTypes.HighlightStyle
import com.example.habitspark.data.dataTypes.LeaderboardRow
import com.example.habitspark.data.models.UserModel
import com.example.habitspark.domain.featureGate.Feature
import com.example.habitspark.domain.featureGate.FeatureGate
import com.example.habitspark.ui.components.charts.spaceDivider
import com.example.habitspark.ui.events.StatsEvent
import com.example.habitspark.ui.events.StatsEventBus
import com.example.habitspark.ui.theme.PrimaryText
import com.example.habitspark.ui.theme.SecondaryText
import com.example.habitspark.ui.theme.SurfaceColor
import com.example.habitspark.utils.minutesToHoursMinutes

@Composable
fun leaderBoard(
    user: UserModel,
) {
    val leaderboardViewModel: LeaderboardViewModel = viewModel()

    val timeSpentRows by leaderboardViewModel.timeSpentRows.collectAsState()
    val isPurchasing by leaderboardViewModel.isPurchasing.collectAsState()
    val purchaseMsg by leaderboardViewModel.purchaseMessage.collectAsState()

    LaunchedEffect(Unit) {
        leaderboardViewModel.startAllTimeLeaderboard()
    }

    LaunchedEffect(purchaseMsg) {
        purchaseMsg?.let { msg ->
            StatsEventBus.emit(StatsEvent.HighlightPurchased(msg))
            leaderboardViewModel.clearPurchaseMessage()
        }
    }

    Log.d("LeaderBoard", "Time Spent Rows: $timeSpentRows")

    FeatureGate(user, Feature.HIGHLIGHT_PURCHASE) {
        highlightUpsell(
            currentUser = user,
            style = HighlightStyle.GLOW,
            onBuyClick = { style ->
                leaderboardViewModel.purchaseHighlight(userId = user.id, style = style )
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            isPurchasing = isPurchasing,
        )
    }

    minutesSpentLeaderboard (
        timeSpentRows = timeSpentRows,
      )

}

@Composable
fun minutesSpentLeaderboard (
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
            val highlightActive = isHighlightActive(item)

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

                if (highlightActive) {
                    animatedGlowName(
                        text = item.name,
                        isFirst = isFirst,
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                } else {
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
                }
                if (highlightActive) {
                    animatedGlowName(
                        text = "${minutesToHoursMinutes(item.minutes)} hours",
                        isFirst = isFirst,
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        textAlign = TextAlign.End,
                        modifier = Modifier.widthIn(min = 96.dp)
                    )
                } else {
                    Text(
                        text = "${minutesToHoursMinutes(item.minutes)} hours",
                        fontSize = fontSize,
                        fontWeight = if (isFirst) FontWeight.Medium else FontWeight.Normal,
                        color = color,
                        textAlign = TextAlign.End,
                        modifier = Modifier.widthIn(min = 96.dp)
                    )
                }
            }
            spaceDivider(
                height = 12,
                divide = index < timeSpentRows.size - 1, // Don't add divider after last item
                dividerFraction = 0.1f
            )

        }
    }
}

@Composable
fun highlightUpsell(
    currentUser: UserModel,
    style: HighlightStyle = HighlightStyle.GLOW,
    onBuyClick: (HighlightStyle) -> Unit,
    modifier: Modifier = Modifier,
    isPurchasing: Boolean = false,
) {
    val nowMs = System.currentTimeMillis()
    val expiresAtMs = currentUser.highlightExpiresAt?.toDate()?.time ?: 0L
    val isActive = nowMs < expiresAtMs
    val timeLeft = remember(expiresAtMs) { remainingTimeLabel(expiresAtMs, nowMs) }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isActive) "Highlight active" else "Stand out on the leaderboard",
                    style = MaterialTheme.typography.titleSmall,
                    color = PrimaryText
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when {
                        isActive -> "Expires in $timeLeft"
                        else -> "${style.label} • Costs ${style.cost} coins"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Your coins: ${currentUser.coin}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            val canAfford = currentUser.coin >= style.cost
            val ctaLabel = if (System.currentTimeMillis() <
                (currentUser.highlightExpiresAt?.toDate()?.time ?: 0L)
            ) "Extend (${style.cost} coins)" else "Highlight"

            Button(
                onClick = { onBuyClick(style) },
                enabled = canAfford && !isPurchasing,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (isPurchasing) "Processing…" else ctaLabel)
            }
        }
    }
}

/** e.g., "6d", "3h", "45m" */
private fun remainingTimeLabel(expiresAtMs: Long, nowMs: Long = System.currentTimeMillis()): String {
    val diff = (expiresAtMs - nowMs).coerceAtLeast(0L)
    val totalMinutes = diff / 60_000
    val totalHours = totalMinutes / 60
    val days = totalHours / 24
    val hours = totalHours % 24
    val minutes = totalMinutes % 60

    return when {
        days > 0 -> {
            if (hours > 0) "${days}d ${hours}h" else "${days}d"
        }
        totalHours > 0 -> {
            if (minutes > 0) "${totalHours}h ${minutes}m" else "${totalHours}h"
        }
        else -> "${minutes}m"
    }
}

private fun isHighlightActive(row: LeaderboardRow, nowMs: Long = System.currentTimeMillis()): Boolean {
    val expires = row.highlightExpiresAtMs ?: return false
    val style = row.highlightStyle?.trim().orEmpty()
    return style.isNotEmpty() && style != "NONE" && nowMs < expires}
@Composable
private fun animatedGlowName(
    text: String,
    isFirst: Boolean = false,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    textAlign: TextAlign = TextAlign.Start,
    modifier: Modifier = Modifier,
) {
    // Sweep the gradient horizontally across the text
    val transition = rememberInfiniteTransition(label = "glowSweep")
    val x by transition.animateFloat(
        initialValue = -300f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "xAnim"
    )

    // Use a gold sweep for #1, aurora sweep otherwise
    val gradientColors = if (isFirst) {
        listOf(
            Color(0xFFFFD700), // gold
            Color(0xFFFFC107), // amber
            Color(0xFFFFF3B0), // light gold highlight
            Color(0xFFFFA000), // warm orange-gold
            Color(0xFFFFD700)  // gold
        )
    } else {
        listOf(
            Color(0xFF6C5DD3), // SoftIndigo
            Color(0xFF4C93F7), // ElectricBlue
            Color(0xFF33E1E1), // Teal pop
            Color(0xFF6C5DD3)  // SoftIndigo
        )
    }

    val shadowColor = if (isFirst) Color(0x80FFD700) else Color(0x8033E1E1)

    val brush = Brush.linearGradient(
        colors = gradientColors,
        start = Offset(x, 0f),
        end = Offset(x + 200f, 0f)
    )

    // Build styled text with gradient brush; add a soft glow using Shadow
    val styled = buildAnnotatedString {
        withStyle(
            SpanStyle(
                brush = brush,
                fontSize = fontSize,
                fontWeight = fontWeight,
                shadow = Shadow(
                    color = shadowColor,
                    blurRadius = 18f,
                    offset = Offset(0f, 0f)
                )
            )
        ) { append(text) }
    }

    Text(
        text = styled,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
        textAlign = textAlign,
    )
}