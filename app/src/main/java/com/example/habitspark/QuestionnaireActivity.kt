package com.example.habitspark

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.habitspark.ui.theme.HabitSparkTheme
import com.example.habitspark.ui.views.onboarding.DemographicData
import com.example.habitspark.ui.views.onboarding.PlayerTypeResult

import com.example.habitspark.ui.views.onboarding.demographicQuestionnaireScreen
import com.example.habitspark.ui.views.onboarding.playerTypeQuestionnaireScreen


class QuestionnaireActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabitSparkTheme {
                var onboardingStep by remember { mutableStateOf(1) }

                // You can store these however you like, mutableState or regular vars
                var demographicData by remember { mutableStateOf<DemographicData?>(null) }
                var playerTypeData by remember { mutableStateOf<PlayerTypeResult?>(null) }

                when (onboardingStep) {
                    1 -> demographicQuestionnaireScreen(
                        onBoadingStep = onboardingStep,
                        toalOnBoadingSteps = 3,
                        onNext = {
                            demographicData = it
                            onboardingStep++
                        }
                    )

                    2 -> playerTypeQuestionnaireScreen(
                        onBoadingStep = onboardingStep,
                        toalOnBoadingSteps = 3,
                        onNext = {
                            playerTypeData = it
                            onboardingStep++
                        }
                    )
                }
            }
        }
    }
}
