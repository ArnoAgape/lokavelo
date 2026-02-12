package com.arnoagape.polyscribe.ui.common.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnoagape.polyscribe.R

/**
 * Displays a button that opens a confirmation dialog before executing an action.
 *
 * @param buttonColor Button color configuration.
 * @param onConfirmButton Callback executed when the user confirms the action.
 * @param actionButton Text displayed on the main button.
 * @param confirmButtonTitle Title of the confirmation dialog.
 * @param confirmButtonMessage Message shown inside the dialog.
 */
@Composable
fun ConfirmDialogButton(
    buttonColor: ButtonColors,
    onConfirmButton: () -> Unit,
    actionButton: String,
    confirmButtonTitle: String,
    confirmButtonMessage: String
) {

    var showDialog by remember { mutableStateOf(false) }

    Button(
        onClick = { showDialog = true },
        modifier = Modifier
            .fillMaxWidth(),
        colors = buttonColor
    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = actionButton
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },

            title = {
                Text(text = confirmButtonTitle)
            },

            text = {
                Text(text = confirmButtonMessage)
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onConfirmButton()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("OK")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}