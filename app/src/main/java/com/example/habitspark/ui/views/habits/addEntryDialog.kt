package com.example.habitspark.ui.views.habits

import android.app.DatePickerDialog
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.habitspark.data.models.DifficultyLevel
import com.example.habitspark.data.models.EntryModel
import com.example.habitspark.data.models.Mood
import com.example.habitspark.ui.theme.ButtonUnselected
import com.example.habitspark.ui.theme.PrimaryAccent
import com.example.habitspark.ui.theme.PrimaryText
import com.example.habitspark.ui.theme.SecondaryText
import com.example.habitspark.ui.theme.SurfaceColor
import com.example.habitspark.ui.views.onboarding.InputType
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

data class EntryQuestion(
    val id: String,
    val question: String,
    val inputType: InputType,
    val options: List<String>? = null
)

val entryQuestions = listOf(
    EntryQuestion("duration", "Duration (minutes)", InputType.NUMBER),
    EntryQuestion("notes", "Notes", InputType.TEXT)
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
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

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
                    timestamp = Timestamp.now()
                )
                Log.d("EntryDialog", "Saving entry: $entry")
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

                        else -> Unit
                    }
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