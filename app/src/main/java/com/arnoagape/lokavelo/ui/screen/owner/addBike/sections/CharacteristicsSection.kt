package com.arnoagape.lokavelo.ui.screen.owner.addBike.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.BikeCategory
import com.arnoagape.lokavelo.domain.model.BikeEquipment
import com.arnoagape.lokavelo.domain.model.BikeCondition
import com.arnoagape.lokavelo.domain.model.labelRes
import com.arnoagape.lokavelo.ui.theme.LocalSpacing

@Composable
fun CharacteristicsSection(
    category: BikeCategory?,
    brand: String,
    state: BikeCondition?,
    isElectric: Boolean,
    accessories: List<BikeEquipment>,
    onCategoryChange: (BikeCategory) -> Unit,
    onBrandChange: (String) -> Unit,
    onStateChange: (BikeCondition) -> Unit,
    onElectricChange: (Boolean) -> Unit,
    onAccessoriesChange: (List<BikeEquipment>) -> Unit
) {
    val spacing = LocalSpacing.current

    SectionCard(
        title = stringResource(R.string.characteristics),
        subtitle = stringResource(R.string.subtitle_characteristics)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {

            CategoryDropdown(
                selected = category,
                onSelected = onCategoryChange
            )

            OutlinedTextField(
                value = brand,
                onValueChange = onBrandChange,
                label = { Text(stringResource(R.string.brand)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                modifier = Modifier.fillMaxWidth()
            )

            StateDropdown(
                selected = state,
                onSelected = onStateChange
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.electric_bike))

                Spacer(modifier = Modifier.weight(1f))

                Switch(
                    checked = isElectric,
                    onCheckedChange = onElectricChange
                )
            }

            AccessoriesChips(
                selected = accessories,
                onSelectionChanged = onAccessoriesChange
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    selected: BikeCategory?,
    onSelected: (BikeCategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {

        OutlinedTextField(
            value = selected?.let { stringResource(it.labelRes()) } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.category)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = true
                )
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            BikeCategory.entries.forEach { category ->
                DropdownMenuItem(
                    text = { Text(stringResource(category.labelRes())) },
                    onClick = {
                        onSelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StateDropdown(
    selected: BikeCondition?,
    onSelected: (BikeCondition) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {

        OutlinedTextField(
            value = selected?.let { stringResource(it.labelRes()) } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.condition)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = true
                )
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            BikeCondition.entries.forEach { condition ->
                DropdownMenuItem(
                    text = { Text(stringResource(condition.labelRes())) },
                    onClick = {
                        onSelected(condition)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun AccessoriesChips(
    selected: List<BikeEquipment>,
    onSelectionChanged: (List<BikeEquipment>) -> Unit
) {
    val spacing = LocalSpacing.current
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.accessories),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = if (isExpanded)
                Icons.Default.ExpandLess
            else
                Icons.Default.ExpandMore,
            contentDescription = stringResource(R.string.cd_expand_accessories)
        )
    }
    AnimatedVisibility(visible = isExpanded) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalArrangement = Arrangement.spacedBy(spacing.small)
        ) {
            BikeEquipment.entries.forEach { equipment ->

                val isSelected = equipment in selected

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val updated = if (isSelected) {
                            selected - equipment
                        } else {
                            selected + equipment
                        }
                        onSelectionChanged(updated)
                    },
                    label = {
                        Text(stringResource(equipment.labelRes()))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}