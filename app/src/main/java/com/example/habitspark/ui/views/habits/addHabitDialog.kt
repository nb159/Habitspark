package com.example.habitspark.ui.views.habits

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.habitspark.data.dataTypes.FormQuestions
import com.example.habitspark.data.dataTypes.GoalType
import com.example.habitspark.data.dataTypes.InputType
import com.example.habitspark.data.models.HabitModel
import com.example.habitspark.ui.theme.HabitSparkTheme
import com.example.habitspark.ui.theme.PrimaryAccent
import com.example.habitspark.ui.theme.PrimaryText
import com.example.habitspark.ui.theme.SecondaryText
import com.example.habitspark.ui.theme.SurfaceColor
import com.example.habitspark.utils.inputFieldQuestion
import com.google.firebase.Timestamp


val habitQuestions = listOf(
    FormQuestions("name", "Habit name", InputType.TEXT),
    FormQuestions("description", "Description", InputType.TEXT),
    FormQuestions("goalType", "Goal type", InputType.SELECT, listOf(GoalType.HOURS.label, GoalType.REPETITIONS.label)),
    FormQuestions("goalTarget", "Goal target", InputType.NUMBER)
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
                        goalType =  GoalType.fromLabel(answers["goalType"] ?: "") ?: GoalType.HOURS,
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
                    inputFieldQuestion(
                        question = question,
                        answer = answers[question.id].orEmpty(),
                        onAnswerChange = { answer ->
                            answers[question.id] = answer
                        }
                    )
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