package com.example.habitspark

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.habitspark.data.models.UserModel
import com.example.habitspark.data.repository.UserPreferencesManager
import com.example.habitspark.data.repository.UserRepository
import com.example.habitspark.ui.theme.HabitSparkTheme
import com.example.habitspark.ui.views.onboarding.DemographicData
import com.example.habitspark.ui.views.onboarding.PlayerTypeResult

import com.example.habitspark.ui.views.onboarding.demographicQuestionnaireScreen
import com.example.habitspark.ui.views.onboarding.introScreen
import com.example.habitspark.ui.views.onboarding.playerTypeQuestionnaireScreen
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch


class QuestionnaireActivity : ComponentActivity() {
    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabitSparkTheme {

                var onboardingStep by remember { mutableStateOf(1) }

                // You can store these however you like, mutableState or regular vars
                var demographicData by remember { mutableStateOf<DemographicData?>(null) }
                var playerTypeData by remember { mutableStateOf<PlayerTypeResult?>(null) }

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

                    3 -> playerTypeQuestionnaireScreen(
                        onBoadingStep = onboardingStep,
                        toalOnBoadingSteps = 3,
                        onNext = {
                            playerTypeData = it
                            onboardingStep++
                        }
                    )

                    4 -> {
                        val user = UserModel(
                            name = demographicData?.userName.orEmpty(),
                            age = demographicData?.age?.toIntOrNull() ?: 0,
                            gender = demographicData?.gender.orEmpty(),
                            country = demographicData?.country.orEmpty(),
                            primaryType = playerTypeData?.primaryType.orEmpty(),
                            secondaryType = playerTypeData?.secondaryType.orEmpty(),
                        )

                        val userRepository = UserRepository(Firebase.firestore)
                        userRepository.addUser(user)
                            .addOnSuccessListener { documentReference ->
                                lifecycleScope.launch {
                                    UserPreferencesManager.saveUserId(
                                        context = this@QuestionnaireActivity,
                                        userId = documentReference.id
                                    )
                                    val intent = Intent(this@QuestionnaireActivity, MainActivity::class.java)
                                    startActivity(intent)

                                }
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error adding document", e)
                            }

                    }
                }
            }
        }
    }
}
