package com.example.habitspark.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.habitspark.data.dataTypes.FormQuestions
import com.example.habitspark.ui.theme.ButtonUnselected
import com.example.habitspark.ui.theme.PrimaryAccent
import com.example.habitspark.ui.theme.PrimaryText
import com.example.habitspark.ui.theme.SecondaryText
import com.example.habitspark.ui.theme.SurfaceColor
import com.example.habitspark.data.dataTypes.InputType


// Creates input fields for questions
// used in questionnaires
@Composable
fun inputFieldQuestion(
    question: FormQuestions,
    answer: String,
    onAnswerChange: (String) -> Unit,
){
    when (question.inputType) {
        InputType.TEXT -> {
            OutlinedTextField(
                value = answer ?: "",
                onValueChange = { onAnswerChange(it) },
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
                        onClick = { onAnswerChange(option) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (answer == option) PrimaryAccent else SurfaceColor,
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
                value = answer,
                onValueChange = { onAnswerChange(it.filter { c -> c.isDigit() }) },
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