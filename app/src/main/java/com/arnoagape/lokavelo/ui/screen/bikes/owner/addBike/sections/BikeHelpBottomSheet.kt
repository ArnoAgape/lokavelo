package com.arnoagape.lokavelo.ui.screen.bikes.owner.addBike.sections

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
                        text = stringResource(R.string.help_photos_paragraph)
                    )
                }

                item {
                    HelpSection(
                        emoji = "🏷️",
                        title = stringResource(R.string.help_characteristics_title),
                        text = stringResource(R.string.help_characteristics_paragraph)
                    )
                }

                item {
                    HelpSection(
                        emoji = "📝",
                        title = stringResource(R.string.help_title_desc_title),
                        text = stringResource(R.string.help_title_desc_paragraph)
                    )
                }

                item {
                    HelpSection(
                        emoji = "📍",
                        title = stringResource(R.string.help_address_title),
                        text = stringResource(R.string.help_address_paragraph)
                    )
                }

                item {
                    HelpSection(
                        emoji = "💰",
                        title = stringResource(R.string.help_pricing_title),
                        text = stringResource(R.string.help_pricing_paragraph)
                    )
                }

                item {
                    HelpSection(
                        emoji = "🔐",
                        title = stringResource(R.string.help_deposit_title),
                        text = stringResource(R.string.help_deposit_paragraph)
                    )
                }

                item {
                    HelpSection(
                        emoji = "📅",
                        title = stringResource(R.string.help_min_rental_title),
                        text = stringResource(R.string.help_min_rental_paragraph)
                    )
                }

                item {
                    HelpSection(
                        emoji = "✅",
                        title = stringResource(R.string.help_before_publish_title),
                        text = stringResource(R.string.help_before_publish_paragraph)
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
    text: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // Title
        Row(
            verticalAlignment = Alignment.CenterVertically
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

        // Paragraph
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 40.dp)
        )
    }
}