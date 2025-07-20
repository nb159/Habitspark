package com.example.habitspark.ui.views.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.habitspark.data.dataTypes.FormQuestions
import com.example.habitspark.data.dataTypes.InputType
import com.example.habitspark.utils.inputFieldQuestion

data class DemographicData(
    val userName: String,
    val age: String,
    val gender: String,
    val country: String,
) {
    companion object {
        fun fromMap(map: Map<String, String>): DemographicData {
            return DemographicData(
                userName = map["userName"].orEmpty(),
                age = map["age"].orEmpty(),
                gender = map["gender"].orEmpty(),
                country = map["country"].orEmpty(),
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun demographicQuestionnaireScreen(
    onBoadingStep: Int = 0,
    toalOnBoadingSteps: Int = 3,
    onNext: (DemographicData) -> Unit
) {
    val questions = listOf(
        FormQuestions("userName", "What is your name?", InputType.TEXT),
        FormQuestions("age", "What is your age?", InputType.NUMBER),
        FormQuestions("gender", "What is your gender?", InputType.SELECT, listOf("Male", "Female", "Other")),
        FormQuestions("country", "What country are you from?", InputType.TEXT),
    )

    val answers = remember { mutableStateMapOf<String, String>() }

    val containerColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val accentColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Demographic Questions ($onBoadingStep/$toalOnBoadingSteps)",
                style = MaterialTheme.typography.titleLarge,
                color = textColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            questions.forEach { question ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp) // ✅ padding on sides too
                        .heightIn(min = 100.dp),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        inputFieldQuestion(
                            question = question,
                            answer = answers[question.id] ?: "",
                            onAnswerChange = { answer ->
                                answers[question.id] = answer
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // calls static function to set up the return object
                    val demographicData = DemographicData.fromMap(answers)
                    onNext(demographicData)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Next")
            }
        }
    }
}
