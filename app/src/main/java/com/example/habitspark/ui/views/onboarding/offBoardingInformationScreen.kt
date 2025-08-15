package com.example.habitspark.ui.views.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.habitspark.data.models.UserModel
import com.example.habitspark.domain.featureGate.UserGroup
import com.example.habitspark.ui.components.charts.spaceDivider
import com.example.habitspark.ui.components.toolTip.infoTooltip
import com.example.habitspark.ui.theme.BackgroundColor
import com.example.habitspark.ui.theme.PrimaryText
import com.example.habitspark.ui.theme.SecondaryText

@Composable
fun offBoardingInformationScreen(
    user: UserModel,
    onNext: () -> Unit
) {
    val primary = PlayerType.fromNameOrNull(user.primaryType) ?: PlayerType.UNKNOWN
    val secondary = PlayerType.fromNameOrNull(user.secondaryType) ?: PlayerType.UNKNOWN
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ){

        Text(
            text = "Onboarding Completed!",
            style = MaterialTheme.typography.titleLarge,
            color = PrimaryText,
            modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            text = "You have successfully completed the onboarding process. Soon you will be able to track your habits and see your progress.",
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryText,
            textAlign = TextAlign.Start
        )

        spaceDivider(40, true, 0.75f)

        Text(
            text = "Account Information",
            style = MaterialTheme.typography.titleLarge,
            color = PrimaryText,
            modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            text = "From the onboarding process, we have gathered the following information about your user type:",
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryText,
        )
        spaceDivider(20)
        Text(
            text = "Primary Type: ${primary.label}\n" +
                    "Secondary Type: ${secondary.label}",
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryText,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row {
            Text(
                text = "User Group: ${user.userGroup}",
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryText,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        spaceDivider(40, true, 0.75f)
        Text(
            text = "We sincerely appreciate your contribution to this thesis study. " +
                    "Your engagement and feedback are invaluable in helping us achieve meaningful results.",
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryText,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.weight(1f)) // 👈 pushes button to bottom

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
            Text("Start Tracking!")
        }
    }
}

@Preview
@Composable
fun OffBoardingInformationScreenPreview() {
    offBoardingInformationScreen(
        user = UserModel(
            primaryType = PlayerType.PLAYER.name,
            secondaryType = PlayerType.ACHIEVER.name
        ),
        onNext = {}
    )
}