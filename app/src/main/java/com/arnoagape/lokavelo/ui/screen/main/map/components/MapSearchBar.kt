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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.arnoagape.lokavelo.domain.model.BikeEquipment
import com.arnoagape.lokavelo.domain.model.BikeMotor
import com.arnoagape.lokavelo.domain.model.BikeSize
import com.arnoagape.lokavelo.domain.model.labelRes
import com.arnoagape.lokavelo.ui.common.components.DateRangePickerDialog
import com.arnoagape.lokavelo.ui.common.components.RentalDates
import com.arnoagape.lokavelo.ui.screen.main.map.SearchFilters
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSearchBar(
    filters: SearchFilters,
    maxBikePrice: Float,
    onAddressClick: () -> Unit,
    onCategorySelected: (Set<BikeCategory>) -> Unit,
    onMotorTypeChanged: (Set<BikeMotor>) -> Unit,
    onDatesSelected: (LocalDate, LocalDate) -> Unit,
    onFiltersSelected: (Set<BikeSize>, Set<BikeEquipment>, Float, Float) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    // Top Search Bar
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

            VerticalDivider(modifier = Modifier.padding(vertical = 12.dp))

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
                    RentalDates(
                        start = filters.startDate,
                        end = filters.endDate
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

    // Filter Chips
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 🚲 Catégorie
            item {
                CategoryFilterChip(
                    filters = filters,
                    onCategorySelected = onCategorySelected
                )
            }

            // ⚡ Type de moteur
            item {
                MotorTypeFilterChip(
                    filters = filters,
                    onMotorTypeChanged = onMotorTypeChanged
                )
            }

            // 🎛️ Filtres avancés
            item {
                AdvancedFiltersChip(
                    filters = filters,
                    maxBikePrice = maxBikePrice,
                    onFiltersSelected = onFiltersSelected
                )
            }
        }
    }

    // Date Picker Dialog
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

// ==================== CHIP COMPONENTS ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterChip(
    filters: SearchFilters,
    onCategorySelected: (Set<BikeCategory>) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = filters.bikeCategories.isNotEmpty(),
            onClick = { showSheet = true },
            label = {
                Text(
                    text = when {
                        filters.bikeCategories.size == 1 -> stringResource(
                            filters.bikeCategories.first().labelRes()
                        )
                        filters.bikeCategories.size > 1 -> stringResource(
                            R.string.categories,
                            filters.bikeCategories.size
                        )
                        else -> stringResource(R.string.category)
                    }
                )
            },
            border = null,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                labelColor = MaterialTheme.colorScheme.onSurface
            ),
            leadingIcon = { Text("🚴") }
        )

        if (showSheet) {
            ModalBottomSheet(onDismissRequest = { showSheet = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.category),
                            style = MaterialTheme.typography.titleMedium
                        )
                        TextButton(onClick = { onCategorySelected(emptySet()) }) {
                            Text(stringResource(R.string.reset))
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BikeCategory.entries.forEach { category ->
                            FilterChip(
                                selected = category in filters.bikeCategories,
                                onClick = {
                                    val updated =
                                        if (category in filters.bikeCategories)
                                            filters.bikeCategories - category
                                        else
                                            filters.bikeCategories + category
                                    onCategorySelected(updated)
                                },
                                label = { Text(stringResource(category.labelRes())) }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun MotorTypeFilterChip(
    filters: SearchFilters,
    onMotorTypeChanged: (Set<BikeMotor>) -> Unit
) {
    FilterChip(
        selected = BikeMotor.ELECTRIC in filters.bikeMotor,
        onClick = {
            val newMotorTypes = filters.bikeMotor.toMutableSet()
            if (BikeMotor.ELECTRIC in newMotorTypes) {
                newMotorTypes.remove(BikeMotor.ELECTRIC)
            } else {
                newMotorTypes.add(BikeMotor.ELECTRIC)
            }
            onMotorTypeChanged(newMotorTypes)
        },
        label = { Text(stringResource(R.string.electric)) },
        border = null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        leadingIcon = { Text("⚡") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFiltersChip(
    filters: SearchFilters,
    maxBikePrice: Float,
    onFiltersSelected: (Set<BikeSize>, Set<BikeEquipment>, Float, Float) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val hasActiveFilters =
        filters.bikeSizes.isNotEmpty() ||
                filters.accessories.isNotEmpty() ||
                filters.minPrice > 0f ||
                filters.maxPrice < maxBikePrice

    Box {
        FilterChip(
            selected = hasActiveFilters,
            onClick = { showSheet = true },
            label = {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = stringResource(R.string.filters)
                )
            },
            border = null,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                labelColor = MaterialTheme.colorScheme.onSurface,
                iconColor = MaterialTheme.colorScheme.onSurface
            )
        )

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        // Titre + Réinitialiser
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.filters),
                                style = MaterialTheme.typography.titleMedium
                            )
                            TextButton(
                                onClick = {
                                    onFiltersSelected(
                                        emptySet(),
                                        emptySet(),
                                        0f,
                                        maxBikePrice
                                    )
                                }
                            ) {
                                Text(stringResource(R.string.reset))
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Prix
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.pricing_day_amount),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${filters.minPrice.toInt()} € – ${filters.maxPrice.toInt()} €",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            RangeSlider(
                                value = filters.minPrice..filters.maxPrice,
                                onValueChange = { range ->
                                    onFiltersSelected(
                                        filters.bikeSizes,
                                        filters.accessories,
                                        range.start,
                                        range.endInclusive
                                    )
                                },
                                valueRange = 0f..maxBikePrice,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                colors = SliderDefaults.colors(
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.6f
                                    ),
                                    thumbColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        // Taille
                        Text(
                            text = stringResource(R.string.size),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BikeSize.entries.forEach { size ->
                                FilterChip(
                                    selected = size in filters.bikeSizes,
                                    onClick = {
                                        val newSizes =
                                            if (size in filters.bikeSizes)
                                                filters.bikeSizes - size
                                            else
                                                filters.bikeSizes + size

                                        onFiltersSelected(
                                            newSizes,
                                            filters.accessories,
                                            filters.minPrice,
                                            filters.maxPrice
                                        )
                                    },
                                    label = { Text(size.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Accessoires
                        Text(
                            text = stringResource(R.string.accessories),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BikeEquipment.entries.forEach { accessory ->
                                FilterChip(
                                    selected = accessory in filters.accessories,
                                    onClick = {
                                        val updated =
                                            if (accessory in filters.accessories)
                                                filters.accessories - accessory
                                            else
                                                filters.accessories + accessory

                                        onFiltersSelected(
                                            filters.bikeSizes,
                                            updated,
                                            filters.minPrice,
                                            filters.maxPrice
                                        )
                                    },
                                    label = { Text(stringResource(accessory.labelRes())) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun MapMapSearchBarPreview() {
    LokaveloTheme {
        val filters = SearchFilters(
            addressQuery = "Marseille",
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(3),
            bikeCategories = emptySet(),
            bikeMotor = emptySet()
        )
        MapSearchBar(
            filters = filters,
            maxBikePrice = 150f,
            onAddressClick = {},
            onCategorySelected = {},
            onMotorTypeChanged = {},
            onDatesSelected = { _, _ -> },
            onFiltersSelected = { _, _, _, _ -> }
        )
    }
}