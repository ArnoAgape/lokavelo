package com.arnoagape.lokavelo.ui.screen.bikes.bikeItem

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.arnoagape.lokavelo.domain.model.RentalStatus
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme

/**
 * Row component supporting selection mode with optional checkbox.
 *
 * Executes either a selection toggle or a click action depending
 * on the current selection state.
 */
@Composable
fun SelectItemRow(
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onSelectToggle: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    badge: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {

    val backgroundColor by animateColorAsState(
        targetValue =
            if (isSelected) MaterialTheme.colorScheme.tertiary
            else MaterialTheme.colorScheme.surfaceVariant,
        label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        if (isSelectionMode) onSelectToggle()
                        else onClick()
                    },
                    onLongClick = { onLongClick() }
                ),
            colors = CardDefaults.elevatedCardColors(
                containerColor = backgroundColor
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }

        badge?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp)
            ) {
                it()
            }
        }
    }
}

@PreviewLightDark
@Composable
fun SelectItemRowPreview() {
    LokaveloTheme {
        Column {

            // Normal
            SelectItemRow(
                isSelectionMode = false,
                isSelected = false,
                onSelectToggle = {},
                onClick = {},
                onLongClick = {}
            ) {
                Text("Vélo normal")
            }

            // Selected
            SelectItemRow(
                isSelectionMode = true,
                isSelected = true,
                onSelectToggle = {},
                onClick = {},
                onLongClick = {}
            ) {
                Text("Vélo sélectionné")
            }

            // Avec badge
            SelectItemRow(
                isSelectionMode = false,
                isSelected = false,
                onSelectToggle = {},
                onClick = {},
                onLongClick = {},
                badge = {
                    RentalStatusBadge(RentalStatus.PENDING)
                }
            ) {
                Text("Avec badge")
            }
        }
    }
}