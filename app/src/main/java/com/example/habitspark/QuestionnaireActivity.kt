package com.example.habitspark

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.habitspark.data.models.UserModel
import com.example.habitspark.data.repository.MetaRepository
import com.example.habitspark.data.repository.UserPreferencesManager
import com.example.habitspark.data.repository.UserRepository
import com.example.habitspark.domain.featureGate.UserGroup
import com.example.habitspark.ui.theme.BackgroundColor
import com.example.habitspark.ui.theme.HabitSparkTheme
import com.example.habitspark.ui.theme.PrimaryText
import com.example.habitspark.ui.views.onboarding.DemographicData
import com.example.habitspark.ui.views.onboarding.PlayerTypeResult
import com.example.habitspark.ui.views.onboarding.demographicQuestionnaireScreen
import com.example.habitspark.ui.views.onboarding.introScreen
import com.example.habitspark.ui.views.onboarding.offBoardingInformationScreen
import com.example.habitspark.ui.views.onboarding.playerTypeQuestionnaireScreen
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class QuestionnaireActivity : ComponentActivity() {
    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabitSparkTheme {

                val userRepository = UserRepository(Firebase.firestore)
                val metaRepository = MetaRepository(Firebase.firestore)

                var onboardingStep by remember { mutableStateOf(1) }

                // You can store these however you like, mutableState or regular vars
                var demographicData by remember { mutableStateOf<DemographicData?>(null) }
                var playerTypeData by remember { mutableStateOf<PlayerTypeResult?>(null) }
                var user by remember { mutableStateOf<UserModel?>(null) }


                when (onboardingStep) {
                    1 -> introScreen(
                        onNext = {
                            onboardingStep++
                        }
                    )
                    2 -> demographicQuestionnaireScreen(
                        onBoadingStep = onboardingStep,
                        toalOnBoadingSteps = 3,
                        onNext = {
                            demographicData = it
                            onboardingStep++
                        }
                    )

                    3 -> {
                        playerTypeQuestionnaireScreen(
                            onBoadingStep = onboardingStep,
                            toalOnBoadingSteps = 3,
                            onNext = {
                                playerTypeData = it
                                onboardingStep++
                            }
                        )
                    }

                    4 -> {
                        var started by remember { mutableStateOf(false) }
                        if (!started) {
                            started = true
                            lifecycleScope.launch {
                                try {
                                    val group = metaRepository.assignUserGroup()

                                    val userInformation = UserModel(
                                        name = demographicData?.userName.orEmpty(),
                                        age = demographicData?.age?.toIntOrNull() ?: 0,
                                        gender = demographicData?.gender.orEmpty(),
                                        country = demographicData?.country.orEmpty(),
                                        primaryType = playerTypeData?.primaryType?.name.orEmpty(),
                                        secondaryType = playerTypeData?.secondaryType?.name.orEmpty(),
                                        userGroup = group.label,
                                    )

                                    val docRef = userRepository.addUser(userInformation).await()

                                    UserPreferencesManager.saveUserId(
                                        context = this@QuestionnaireActivity,
                                        userId = docRef.id
                                    )
                                    user = userInformation.copy(id = docRef.id)

                                    onboardingStep++
                                } catch (e: Exception) {
                                    Log.w("Onboarding", "User creation failed", e)
                                    // optional: show an error UI/message here
                                }
                            }
                        }

                        LoadingStep(message = "Creating your account…")
                    }

                    5 -> offBoardingInformationScreen (
                        user = user!!,
                        onNext = {
                            val intent = Intent(this@QuestionnaireActivity, MainActivity::class.java)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}
@Composable
private fun LoadingStep(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text(message, color = PrimaryText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
