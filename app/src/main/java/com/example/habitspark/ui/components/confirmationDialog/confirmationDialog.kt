package com.example.habitspark.ui.components.confirmationDialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.habitspark.ui.theme.PrimaryText

@Composable
fun confirmationDialog(
    onDismiss: () -> Unit,
    onProceed: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Are you sure?",
            )
        },
        text = {
            Text(
                text = "This action cannot be undone.",
                color = PrimaryText
                )
        },
        confirmButton = {
            TextButton(
                onClick =  onProceed
            ) {
                Text("Proceed")
            }
        },
        dismissButton = {
            TextButton(
                onClick =  onDismiss
            ) {
                Text("Cancel")
            }
        }
    )

}
