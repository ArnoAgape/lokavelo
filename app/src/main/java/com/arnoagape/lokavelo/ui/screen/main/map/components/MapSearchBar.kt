package com.arnoagape.lokavelo.ui.screen.main.map.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.BikeCategory
import com.arnoagape.lokavelo.domain.model.labelRes
import com.arnoagape.lokavelo.ui.common.components.DateRangePickerDialog
import com.arnoagape.lokavelo.ui.common.components.RentalDates
import com.arnoagape.lokavelo.ui.screen.main.map.SearchFilters
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    filters: SearchFilters,
    onAddressClick: () -> Unit,
    onCategorySelected: (BikeCategory?) -> Unit,
    onElectricSelected: (Boolean?) -> Unit,
    onDatesSelected: (LocalDate, LocalDate) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 8.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {

            // 📍 Adresse
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onAddressClick() }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = filters.addressQuery ?: stringResource(R.string.search),
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            VerticalDivider()

            // 📅 Dates
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.width(12.dp))

                if (filters.startDate != null && filters.endDate != null) {

                    val startDate = filters.startDate
                    val endDate = filters.endDate

                    RentalDates(
                        start = startDate,
                        end = endDate
                    )

                } else {

                    Text(
                        text = stringResource(R.string.period),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item {

                var showCategorySheet by remember { mutableStateOf(false) }

                Box {

                    FilterChip(
                        selected = filters.bikeCategory != null,
                        onClick = { showCategorySheet = true },
                        label = {
                            Text(
                                text = stringResource(
                                    filters.bikeCategory?.labelRes() ?: R.string.category
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    )

                    if (showCategorySheet) {

                        ModalBottomSheet(
                            onDismissRequest = { showCategorySheet = false }
                        ) {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {

                                Text(
                                    text = stringResource(R.string.category),
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(Modifier.height(16.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {

                                    FilterChip(
                                        selected = filters.bikeCategory == null,
                                        onClick = {
                                            onCategorySelected(null)
                                            showCategorySheet = false
                                        },
                                        label = { Text(stringResource(R.string.all)) }
                                    )

                                    BikeCategory.entries.forEach { category ->

                                        FilterChip(
                                            selected = filters.bikeCategory == category,
                                            onClick = {
                                                onCategorySelected(category)
                                                showCategorySheet = false
                                            },
                                            label = {
                                                Text(stringResource(category.labelRes()))
                                            }
                                        )
                                    }
                                }

                                Spacer(Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }

            item {

                var showElectricSheet by remember { mutableStateOf(false) }

                Box {

                    FilterChip(
                        selected = filters.electricOnly != null,
                        onClick = { showElectricSheet = true },
                        label = {
                            Text(
                                when (filters.electricOnly) {
                                    true -> "Électrique"
                                    false -> "Musculaire"
                                    null -> "Motorisation"
                                }
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    )

                    if (showElectricSheet) {

                        ModalBottomSheet(
                            onDismissRequest = { showElectricSheet = false }
                        ) {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {

                                Text(
                                    text = stringResource(R.string.motorization),
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(Modifier.height(16.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {

                                    // Tous
                                    FilterChip(
                                        selected = filters.electricOnly == null,
                                        onClick = {
                                            onElectricSelected(null)
                                            showElectricSheet = false
                                        },
                                        label = { Text(stringResource(R.string.all)) }
                                    )

                                    // Électrique
                                    FilterChip(
                                        selected = filters.electricOnly == true,
                                        onClick = {
                                            onElectricSelected(true)
                                            showElectricSheet = false
                                        },
                                        label = { Text(stringResource(R.string.electric)) }
                                    )

                                    // Musculaire
                                    FilterChip(
                                        selected = filters.electricOnly == false,
                                        onClick = {
                                            onElectricSelected(false)
                                            showElectricSheet = false
                                        },
                                        label = { Text(stringResource(R.string.muscular)) }
                                    )
                                }

                                Spacer(Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // ---- Date Picker ----

    if (showDatePicker) {

        DateRangePickerDialog(
            onDismiss = { showDatePicker = false },
            onDatesSelected = { start, end ->
                onDatesSelected(start, end)
                showDatePicker = false
            }
        )
    }
}

@PreviewLightDark
@Composable
private fun MapSearchBarPreview() {
    LokaveloTheme {
        val filters = SearchFilters(
            addressQuery = "Marseille",
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(3),
            bikeCategory = BikeCategory.GRAVEL,
            electricOnly = true
        )
        SearchBar(
            filters = filters,
            onAddressClick = {},
            onCategorySelected = {},
            onElectricSelected = {},
            onDatesSelected = { _, _ -> }
        )
    }
}