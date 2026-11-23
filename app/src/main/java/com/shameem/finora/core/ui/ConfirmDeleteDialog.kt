package com.shameem.finora.core.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors

/** Destructive confirmation (iOS `confirmationDialog` with a Delete button and a message). */
@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FinoraColors.ScreenBackground,
        title = { AppText(title, size = 18, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary) },
        text = { AppText(message, size = 14, color = FinoraColors.TextSecondary) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                AppText("Delete", size = 15, weight = AppFontWeight.Bold, color = FinoraColors.Negative)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                AppText("Cancel", size = 15, weight = AppFontWeight.SemiBold, color = FinoraColors.TextPrimary)
            }
        },
    )
}
