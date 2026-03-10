package com.arnoagape.lokavelo.ui.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class RentalDatesLayout {
    Vertical,
    Inline
}

@Composable
fun RentalDates(
    start: LocalDate,
    end: LocalDate,
    modifier: Modifier = Modifier,
    layout: RentalDatesLayout = RentalDatesLayout.Vertical
) {

    val formatter = remember {
        DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
    }

    when (layout) {

        RentalDatesLayout.Vertical -> {

            Column(modifier) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = start.format(formatter),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = end.format(formatter),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        RentalDatesLayout.Inline -> {

            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = start.format(formatter),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.width(6.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp)
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    text = end.format(formatter),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}