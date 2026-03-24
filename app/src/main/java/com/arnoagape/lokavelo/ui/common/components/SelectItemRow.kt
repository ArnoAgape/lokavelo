package com.arnoagape.lokavelo.ui.common.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arnoagape.lokavelo.domain.model.RentalStatus

/**
 * Row component supporting selection mode with optional checkbox.
 *
 * Executes either a selection toggle or a click action depending
 * on the current selection state.
 */
@Composable
fun SelectItemRow(
    id: String,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onSelectToggle: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {

    val backgroundColor by animateColorAsState(
        targetValue =
            when {
                isSelected -> MaterialTheme.colorScheme.tertiary
                else ->
                    MaterialTheme.colorScheme.surfaceVariant
            },
        label = ""
    )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
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
}