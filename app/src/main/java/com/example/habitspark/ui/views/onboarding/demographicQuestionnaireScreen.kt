package com.example.habitspark.ui.views.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

enum class InputType {
    TEXT, SELECT, BOOLEAN
}

data class DemographicQuestion(
    val id: String,
    val question: String,
    val inputType: InputType,
    val options: List<String>? = null // Only for SELECT or BOOLEAN
)

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
        DemographicQuestion("userName", "What is your name?", InputType.TEXT),
        DemographicQuestion("age", "What is your age?", InputType.TEXT),
        DemographicQuestion("gender", "What is your gender?", InputType.SELECT, listOf("Male", "Female", "Other")),
        DemographicQuestion("country", "What country are you from?", InputType.TEXT),
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
                        .padding(vertical = 8.dp)
                        .heightIn(min = 100.dp),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally),
                    ) {
                        Text(
                            text = question.question,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(15.dp))

                        when (question.inputType) {
                            InputType.TEXT -> {
                                TextField(
                                    value = answers[question.id] ?: "",
                                    onValueChange = { answers[question.id] = it },
                                    singleLine = true,
                                    placeholder = { Text("Type here...", color = textColor.copy(alpha = 0.5f)) },
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        keyboardType = if (question.id == "age") KeyboardType.Number else KeyboardType.Text
                                    ),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = containerColor,
                                        unfocusedContainerColor = containerColor,
                                        disabledContainerColor = containerColor,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                        focusedTextColor = textColor,
                                        unfocusedTextColor = textColor
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                )
                            }

                            InputType.SELECT, InputType.BOOLEAN -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    question.options?.forEach { option ->
                                        val selected = answers[question.id] == option
                                        OutlinedButton(
                                            onClick = { answers[question.id] = option },
                                            border = BorderStroke(
                                                1.dp,
                                                if (selected) accentColor else textColor.copy(alpha = 0.3f)
                                            )
                                        ) {
                                            Text(
                                                text = option,
                                                color = if (selected) accentColor else textColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
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
