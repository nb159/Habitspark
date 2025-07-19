package com.example.habitspark.ui.views.habits

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.habitspark.data.models.HabitModel
import com.example.habitspark.ui.theme.ButtonUnselected
import com.example.habitspark.ui.theme.HabitSparkTheme
import com.example.habitspark.ui.theme.PrimaryAccent
import com.example.habitspark.ui.theme.PrimaryText
import com.example.habitspark.ui.theme.SecondaryText
import com.example.habitspark.ui.theme.SurfaceColor
import com.example.habitspark.ui.views.onboarding.InputType
import com.google.firebase.Timestamp

data class HabitQuestion(
    val id: String,
    val question: String,
    val inputType: InputType,
    val options: List<String>? = null
)

val habitQuestions = listOf(
    HabitQuestion("name", "Habit name", InputType.TEXT),
    HabitQuestion("description", "Description", InputType.TEXT),
    HabitQuestion("goalType", "Goal type", InputType.SELECT, listOf("Hours", "Check-ins")),
    HabitQuestion("goalTarget", "Goal target", InputType.NUMBER)
)

@Composable
fun addHabitDialog(
    userId: String,
    onDismiss: () -> Unit,
    onSave: (HabitModel) -> Unit,
) {
    val answers = remember { mutableStateMapOf<String, String>() }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(
                onClick = {
                    val habit = HabitModel(
                         name = answers["name"].orEmpty(),
                        description = answers["description"].orEmpty(),
                        goalType = answers["goalType"].orEmpty(),
                        goalTarget = answers["goalTarget"]?.toIntOrNull() ?: 0,
                        userId = userId,
                        createdAt = Timestamp.now()
                    )
                    onSave(habit)
                    onDismiss()
                }
            ) {
                Text("Save", color = PrimaryAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SecondaryText)
            }
        },
        title = {
            Text("New Habit", color = PrimaryText)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                habitQuestions.forEach { question ->
                    when (question.inputType) {
                        InputType.TEXT -> {
                            OutlinedTextField(
                                value = answers[question.id] ?: "",
                                onValueChange = { answers[question.id] = it },
                                label = { Text(question.question) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = PrimaryText,
                                    unfocusedTextColor = PrimaryText,
                                    focusedBorderColor = PrimaryAccent,
                                    unfocusedBorderColor = ButtonUnselected,
                                    cursorColor = PrimaryAccent,
                                    focusedLabelColor = SecondaryText,
                                    unfocusedLabelColor = SecondaryText
                                )

                            )
                        }
                        InputType.SELECT -> {
                            Text(question.question, color = SecondaryText)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                question.options?.forEach { option ->
                                    Button(
                                        onClick = { answers[question.id] = option },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (answers[question.id] == option) PrimaryAccent else SurfaceColor,
                                            contentColor = PrimaryText
                                        )
                                    ) {
                                        Text(option)
                                    }
                                }
                            }
                        }

                        InputType.NUMBER -> {
                            OutlinedTextField(
                                value = answers[question.id] ?: "",
                                onValueChange = { answers[question.id] = it.filter { c -> c.isDigit() } },
                                label = { Text(question.question) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = PrimaryText,
                                    unfocusedTextColor = PrimaryText,
                                    focusedBorderColor = PrimaryAccent,
                                    unfocusedBorderColor = ButtonUnselected,
                                    cursorColor = PrimaryAccent,
                                    focusedLabelColor = SecondaryText,
                                    unfocusedLabelColor = SecondaryText
                                )
                            )
                        }
                        else -> {}
                    }
                }
            }
        },
        containerColor = SurfaceColor,
        titleContentColor = PrimaryText
    )
}

@Preview(showBackground = true)
@Composable
fun addHabitDialogPreview() {
    HabitSparkTheme {
        var open by remember { mutableStateOf(true) }

        if (open) {
            addHabitDialog(
                onDismiss = { open = false },
                onSave = { habit -> Log.d("Preview", "Habit created: $habit") },
                userId = "previewUser123"
            )
        }
    }
}