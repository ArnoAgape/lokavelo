package com.arnoagape.lokavelo.ui.screen.bikes.owner.addBike.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.BikeCategory
import com.arnoagape.lokavelo.domain.model.BikeEquipment
import com.arnoagape.lokavelo.domain.model.BikeCondition
import com.arnoagape.lokavelo.domain.model.BikeMotor
import com.arnoagape.lokavelo.domain.model.BikeSize
import com.arnoagape.lokavelo.domain.model.labelRes
import com.arnoagape.lokavelo.ui.theme.LocalSpacing

private const val BRAND_MAX_LENGTH = 40

@Composable
fun CharacteristicsSection(
    category: BikeCategory?,
    brand: String,
    condition: BikeCondition?,
    type: BikeMotor,
    size: BikeSize?,
    accessories: List<BikeEquipment>,
    categoryError: Boolean,
    conditionError: Boolean,
    sizeError: Boolean,
    onCategoryChange: (BikeCategory) -> Unit,
    onBrandChange: (String) -> Unit,
    onStateChange: (BikeCondition) -> Unit,
    onTypeChange: (BikeMotor) -> Unit,
    onSizeChange: (BikeSize) -> Unit,
    onAccessoriesChange: (List<BikeEquipment>) -> Unit
) {
    val spacing = LocalSpacing.current

    SectionCard(
        title = stringResource(R.string.characteristics),
        subtitle = stringResource(R.string.subtitle_characteristics)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {

            Dropdown(
                selected = category,
                onSelected = onCategoryChange,
                isError = categoryError,
                items = BikeCategory.entries,
                label = stringResource(R.string.category),
                itemLabel = { stringResource(it.labelRes()) }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BikeMotorSelector(
                    motorType = type,
                    onMotorTypeChanged = onTypeChange
                )
            }

            OutlinedTextField(
                value = brand,
                onValueChange = { onBrandChange(it) },
                label = { Text(stringResource(R.string.brand)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        stringResource(R.string.hint_brand),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                },
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = "${brand.length}/$BRAND_MAX_LENGTH",
                            color = when {
                                brand.length > BRAND_MAX_LENGTH * 0.9 ->
                                    MaterialTheme.colorScheme.error

                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            )

            Dropdown(
                selected = size,
                onSelected = onSizeChange,
                isError = sizeError,
                items = BikeSize.entries,
                label = stringResource(R.string.size),
                itemLabel = { it.name }
            )

            Dropdown(
                selected = condition,
                onSelected = onStateChange,
                isError = conditionError,
                items = BikeCondition.entries,
                label = stringResource(R.string.condition),
                itemLabel = { stringResource(it.labelRes()) }
            )

            Spacer(Modifier.height(spacing.medium))

            AccessoriesChips(
                selected = accessories,
                onSelectionChanged = onAccessoriesChange
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> Dropdown(
    selected: T?,
    items: List<T>,
    label: String,
    isError: Boolean,
    itemLabel: @Composable (T) -> String,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {

        OutlinedTextField(
            value = selected?.let { itemLabel(it) } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            isError = isError,
            supportingText = if (isError) {
                { Text(stringResource(R.string.required)) }
            } else null,
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
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemLabel(item)) },
                    onClick = {
                        onSelected(item)
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
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@Composable
fun BikeMotorSelector(
    motorType: BikeMotor,
    onMotorTypeChanged: (BikeMotor) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onMotorTypeChanged(BikeMotor.REGULAR) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (motorType == BikeMotor.REGULAR) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (motorType == BikeMotor.REGULAR) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            ) {
                Text(stringResource(R.string.muscular))
            }

            Button(
                onClick = { onMotorTypeChanged(BikeMotor.ELECTRIC) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (motorType == BikeMotor.ELECTRIC) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (motorType == BikeMotor.ELECTRIC) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            ) {
                Text(stringResource(R.string.electric))
            }
        }
    }
}