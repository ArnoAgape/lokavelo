package com.arnoagape.lokavelo.ui.common.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.arnoagape.lokavelo.R

/**
 * Displays a confirmation dialog when deletion is requested.
 *
 * The dialog is shown only when [show] is true.
 */
@Composable
fun ConfirmDeleteDialog(
    show: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmButtonTitle: String,
    confirmButtonMessage: String
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(confirmButtonTitle) },
            text = { Text(confirmButtonMessage) },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm()
                    onDismiss()
                }) {
                    Text(stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}