package com.example.habitspark.ui.views.habits


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.example.habitspark.data.dataTypes.FormQuestions
import com.example.habitspark.data.dataTypes.InputType
import com.example.habitspark.data.models.DifficultyLevel
import com.example.habitspark.data.models.EntryModel
import com.example.habitspark.data.models.Mood
import com.example.habitspark.ui.theme.PrimaryAccent
import com.example.habitspark.ui.theme.PrimaryText
import com.example.habitspark.ui.theme.SecondaryText
import com.example.habitspark.ui.theme.SurfaceColor
import com.example.habitspark.utils.inputFieldQuestion
import com.google.firebase.Timestamp

data class EntryQuestion(
    val id: String,
    val question: String,
    val inputType: InputType,
    val options: List<String>? = null
)

val entryQuestions = listOf(
    FormQuestions("duration", "Duration (minutes)", InputType.NUMBER),
    FormQuestions("notes", "Notes", InputType.TEXT)
)


@Composable
fun addEntryDialog(
    userId: String,
    habitId: String,
    onDismiss: () -> Unit,
    onSave: (EntryModel) -> Unit
) {
    val answers = remember { mutableStateMapOf<String, String>() }

    var selectedMood by remember { mutableStateOf<Mood?>(null) }
    var selectedDifficulty by remember { mutableStateOf<DifficultyLevel?>(DifficultyLevel.VERY_EASY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val entry = EntryModel(
                    userId = userId,
                    habitId = habitId,
                    description = answers["notes"].orEmpty(),
                    minutesSpent = answers["duration"]?.toIntOrNull(),
                    moodValue = selectedMood?.value,
                    difficultyValue = selectedDifficulty?.value,
                    createdDate = Timestamp.now()
                )
                onSave(entry)
                onDismiss()
            }) {
                Text("Save", color = PrimaryAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SecondaryText)
            }
        },
        title = { Text("New Entry", color = PrimaryText) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                entryQuestions.forEach { question ->
                    inputFieldQuestion(
                        question = question,
                        answer = answers[question.id] ?: "",
                        onAnswerChange = { answers[question.id] = it }
                    )
                }

                // Mood & Difficulty
                Text("Mood", color = SecondaryText)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Mood.entries.forEach { mood ->
                        Button(
                            onClick = { selectedMood = mood },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedMood == mood) PrimaryAccent else SurfaceColor,
                                contentColor = PrimaryText
                            )
                        ) {
                            Text(mood.emoji)
                        }
                    }
                }

                Text("Difficulty", color = SecondaryText)
                val selectedDifficultyLevel = selectedDifficulty ?: DifficultyLevel.VERY_EASY

                Slider(
                    value = selectedDifficultyLevel.value.toFloat(),
                    onValueChange = { value ->
                        selectedDifficulty = DifficultyLevel.fromValue(value.toInt())
                    },
                    valueRange = 0f..5f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = selectedDifficultyLevel.color,
                        activeTrackColor = selectedDifficultyLevel.color,
                        activeTickColor = selectedDifficultyLevel.color
                    )
                )
            }
        },
        containerColor = SurfaceColor,
        titleContentColor = PrimaryText
    )
}