package com.arnoagape.lokavelo.ui.screen.bikes.owner.addBike

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arnoagape.lokavelo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BikeHelpBottomSheet(
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Guide de remplissage",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }

            // Scrollable content
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    HelpSection(
                        emoji = "📸",
                        title = stringResource(R.string.help_photos_title),
                        items = listOf(
                            stringResource(R.string.help_photos_item_1),
                            stringResource(R.string.help_photos_item_2),
                            stringResource(R.string.help_photos_item_3),
                            stringResource(R.string.help_photos_item_4),
                            stringResource(R.string.help_photos_item_5)
                        )
                    )
                }

                item {
                    HelpSection(
                        emoji = "📝",
                        title = stringResource(R.string.help_title_desc_title),
                        items = listOf(
                            stringResource(R.string.help_title_desc_item_1),
                            stringResource(R.string.help_title_desc_item_2),
                            stringResource(R.string.help_title_desc_item_3),
                            stringResource(R.string.help_title_desc_item_4),
                            stringResource(R.string.help_title_desc_item_5)
                        )
                    )
                }

                item {
                    HelpSection(
                        emoji = "🏷️",
                        title = stringResource(R.string.help_characteristics_title),
                        items = listOf(
                            stringResource(R.string.help_characteristics_item_1),
                            stringResource(R.string.help_characteristics_item_2),
                            stringResource(R.string.help_characteristics_item_3),
                            stringResource(R.string.help_characteristics_item_4),
                            stringResource(R.string.help_characteristics_item_5),
                            stringResource(R.string.help_characteristics_item_6)
                        )
                    )
                }

                item {
                    HelpSection(
                        emoji = "📍",
                        title = stringResource(R.string.help_address_title),
                        items = listOf(
                            stringResource(R.string.help_address_item_1),
                            stringResource(R.string.help_address_item_2),
                            stringResource(R.string.help_address_item_3),
                            stringResource(R.string.help_address_item_4)
                        )
                    )
                }

                item {
                    HelpSection(
                        emoji = "💰",
                        title = stringResource(R.string.help_pricing_title),
                        items = listOf(
                            stringResource(R.string.help_pricing_item_1),
                            stringResource(R.string.help_pricing_item_2),
                            stringResource(R.string.help_pricing_item_3),
                            stringResource(R.string.help_pricing_item_4),
                            stringResource(R.string.help_pricing_item_5),
                            stringResource(R.string.help_pricing_item_6)
                        )
                    )
                }

                item {
                    HelpSection(
                        emoji = "🔐",
                        title = stringResource(R.string.help_deposit_title),
                        items = listOf(
                            stringResource(R.string.help_deposit_item_1),
                            stringResource(R.string.help_deposit_item_2),
                            stringResource(R.string.help_deposit_item_3),
                            stringResource(R.string.help_deposit_item_4),
                            stringResource(R.string.help_deposit_item_5)
                        )
                    )
                }

                item {
                    HelpSection(
                        emoji = "📅",
                        title = stringResource(R.string.help_min_rental_title),
                        items = listOf(
                            stringResource(R.string.help_min_rental_item_1),
                            stringResource(R.string.help_min_rental_item_2),
                            stringResource(R.string.help_min_rental_item_3),
                            stringResource(R.string.help_min_rental_item_4),
                            stringResource(R.string.help_min_rental_item_5)
                        )
                    )
                }

                item {
                    HelpSection(
                        emoji = "✅",
                        title = stringResource(R.string.help_before_publish_title),
                        items = listOf(
                            stringResource(R.string.help_before_publish_item_1),
                            stringResource(R.string.help_before_publish_item_2),
                            stringResource(R.string.help_before_publish_item_3),
                            stringResource(R.string.help_before_publish_item_4),
                            stringResource(R.string.help_before_publish_item_5),
                            stringResource(R.string.help_before_publish_item_6)
                        )
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

            }
        }
    }
}

@Composable
private fun HelpSection(
    emoji: String,
    title: String,
    items: List<String>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Title with emoji
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 40.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "•",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}