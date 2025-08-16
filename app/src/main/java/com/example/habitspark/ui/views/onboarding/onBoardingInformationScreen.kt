package com.example.habitspark.ui.views.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.habitspark.data.dataTypes.PlayerType
import com.example.habitspark.ui.components.charts.spaceDivider
import com.example.habitspark.ui.theme.PrimaryText

@Composable
fun introScreen(onNext: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        item {
            Text(
                text = "Welcome To HabitSpark!",
                style = MaterialTheme.typography.titleLarge,
                color = PrimaryText,
                modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        item {
            Text(
                text = "HabitSpark is a habit-tracking app that blends personalization with gamification. " +
                        "Track your habits while unlocking a motivational system built just for you!",
                style = MaterialTheme.typography.bodyLarge,
                color = PrimaryText,
                modifier = Modifier,
            )
            spaceDivider(50, true, 0.75f)
        }

        item {
            Text(
                text = "How HabitSpark Personalizes Your Journey",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        item {
            Text(
                text = "During onboarding, you'll answer a few questions to help us determine your player type.\n",
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryText,
            )
        }

        item {
            Text(
                text = "HabitSpark adapts to your motivational style using research-backed user types. Based on your answers, we tailor your experience to match what drives you," +
                        " whether that’s rewards, progress, creativity, social connection, or impact.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            spaceDivider(50, true, 0.75f)
        }

        item {
            Text(
                text = "What Type of User Are You",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        item {
            Text(
                text = "You'll meet 6 motivational styles. No type is better than another, they're just different ways people stay driven.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 32.dp),
                textAlign = TextAlign.Start
            )
        }

        PlayerType.entries
            .filter { it != PlayerType.UNKNOWN }
            .forEach { type ->
                item {
                    Text(
                        text = "• ${type.label} — ${type.description}".trimIndent(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryText,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
            }

        item {
            spaceDivider(50, true, 0.75f)

            Text(
                text = "Why This App Exists",
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryText,
                modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        item {
            Text(
                text = "HabitSpark is part of a thesis research study aiming to explore whether personalization in self-tracking applications can improve user engagement, enjoyment, " +
                        "and long-term retention.\n\nBy using this app, you’re contributing to a deeper understanding of how tailoring digital experiences to individuals might make " +
                        "habit tracking more effective and enjoyable.",
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryText,
            )
        }
        item {
            Text(
                text = "\nNote: After a day and half (36 hours) of creating your account, a feedback section will become available in the drawer menu. There, you can open a short Google Form to share your experience. " +
                        "Your input will help evaluate the effectiveness of personalization in HabitSpark.",
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryText,
            )
            spaceDivider(50, true, 0.75f)
        }
        item {
            Text(
                text = "About A/B Testing",
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryText,
                modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        item {
            Text(
                text = "To properly evaluate the effect of personalization, this app uses A/B testing. Some users will experience a version with all personalization features included, while others will receive a more general version.\n\nThe app will automatically assign you to one of these groups in order to keep the test balanced across users. This ensures that the results of the study are fair and meaningful.",
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryText,
                modifier = Modifier.padding(bottom = 32.dp),
                textAlign = TextAlign.Start
            )
            spaceDivider(50, true, 0.75f)
        }

        item {
            Text(
                text = "By Clicking 'Get Started', you agree to participate in this research study. Your data will remain anonymous and will only be used for academic purposes. " +
                        "You can withdraw at any time by uninstalling the app.",
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryText,
                modifier = Modifier.padding(bottom = 32.dp),
                textAlign = TextAlign.Start
            )
0        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text("Get Started")
            }
        }
    }
}

@Preview
@Composable
fun IntroScreenPreview() {
    introScreen(onNext = {})
}
