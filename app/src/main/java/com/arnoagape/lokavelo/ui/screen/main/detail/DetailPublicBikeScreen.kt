package com.arnoagape.lokavelo.ui.screen.main.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.BikeCategory
import com.arnoagape.lokavelo.domain.model.BikeCondition
import com.arnoagape.lokavelo.domain.model.BikeEquipment
import com.arnoagape.lokavelo.domain.model.BikeLocation
import com.arnoagape.lokavelo.domain.model.User
import com.arnoagape.lokavelo.domain.model.labelRes
import com.arnoagape.lokavelo.ui.common.components.DateRangePickerDialog
import com.arnoagape.lokavelo.ui.common.components.ErrorOverlay
import com.arnoagape.lokavelo.ui.common.components.ErrorType
import com.arnoagape.lokavelo.ui.common.components.LoadingOverlay
import com.arnoagape.lokavelo.ui.common.components.photo.PhotoItem
import com.arnoagape.lokavelo.ui.common.components.photo.PhotosContent
import com.arnoagape.lokavelo.ui.screen.main.detail.sections.OwnerCard
import com.arnoagape.lokavelo.ui.screen.main.detail.sections.PriceBreakdownCard
import com.arnoagape.lokavelo.ui.screen.bikes.owner.addBike.sections.SubmitButton
import com.arnoagape.lokavelo.ui.screen.bikes.owner.detailBike.sections.AccessoriesRow
import com.arnoagape.lokavelo.ui.screen.bikes.owner.detailBike.sections.DetailCard
import com.arnoagape.lokavelo.ui.screen.bikes.owner.detailBike.sections.DetailRow
import com.arnoagape.lokavelo.ui.theme.LocalSpacing
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import com.arnoagape.lokavelo.ui.utils.toEuroString
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPublicBikeScreen(
    bikeId: String,
    viewModel: DetailPublicBikeViewModel,
    startDate: LocalDate?,
    endDate: LocalDate?,
    onBack: () -> Unit,
    onContactClick: (String, Long, Long) -> Unit
) {

    val state by viewModel.state.collectAsState()

    LaunchedEffect(bikeId) {
        viewModel.setBikeId(bikeId)
        viewModel.setInitialDates(startDate, endDate)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.bike?.title ?: stringResource(R.string.detail_bike),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            stringResource(R.string.cd_go_back)
                        )
                    }
                }
            )
        },

        bottomBar = {
            SubmitButton(
                enabled = state.bike != null,
                onClick = {
                    val bike = state.bike ?: return@SubmitButton

                    val start = state.startDate
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()

                    val end = state.endDate
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()

                    onContactClick(
                        bike.id,
                        start,
                        end
                    )
                },
                isLoading = state.bike == null,
                submitText = stringResource(R.string.button_contact)
            )
        }

    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
                .fillMaxSize()
        ) {

            when {
                state.isLoading -> {
                    LoadingOverlay()
                }

                state.error != null -> {
                    ErrorOverlay(
                        type = ErrorType.GENERIC,
                        onRetry = { viewModel.setBikeId(bikeId) },
                    )
                }

                else -> {
                    DetailPublicBikeContent(
                        modifier = Modifier.fillMaxSize(),
                        onDatesSelected = { start, end ->
                            viewModel.updateDates(start, end)
                        },
                        state = state
                    )
                }
            }
        }
    }
}

@Composable
fun DetailPublicBikeContent(
    modifier: Modifier = Modifier,
    onDatesSelected: (LocalDate, LocalDate) -> Unit,
    state: DetailPublicBikeState
) {

    val spacing = LocalSpacing.current
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {

        DateRangePickerDialog(
            onDismiss = { showDatePicker = false },
            onDatesSelected = { start, end ->
                onDatesSelected(start, end)
                showDatePicker = false
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(
            start = spacing.medium,
            end = spacing.medium
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.large)
    ) {
        item { Spacer(modifier = Modifier.height(spacing.extraSmall)) }

        state.bike?.let { bike ->
            // Images
            item {
                DetailCard(title = stringResource(R.string.pictures)) {
                    PhotosContent(
                        photos = state.bike.photoUrls.map { url ->
                            PhotoItem.Remote(
                                id = url,
                                url = url
                            )
                        },
                        onAddPhotoClick = {},
                        onRemovePhoto = {},
                        onPhotoEdited = { _, _ -> },
                        onMovePhoto = { _, _ -> },
                        isEditable = false
                    )
                }
            }

            // Title / Price / City
            item {
                DetailCard {

                    Text(
                        bike.title,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        "${state.bike.location.city} ${bike.location.postalCode}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(2.dp))

                    Text(
                        stringResource(
                            R.string.price_per_day,
                            bike.priceInCents.toEuroString()
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Description
            item {
                DetailCard(stringResource(R.string.description)) {
                    Text(
                        text = bike.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Owner
            state.owner?.let { owner ->
                item {
                    DetailCard {
                        OwnerCard(owner)
                    }
                }
            }

            // Characteristics
            item {
                DetailCard(title = stringResource(R.string.characteristics)) {

                    // Category
                    DetailRow(
                        label = stringResource(R.string.category),
                        value = bike.category?.let { stringResource(it.labelRes()) } ?: ""
                    )

                    // Electric
                    DetailRow(
                        label = stringResource(R.string.electric_bike),
                        value = if (bike.electric)
                            stringResource(R.string.yes)
                        else
                            stringResource(R.string.no)
                    )

                    // Brand
                    DetailRow(
                        label = stringResource(R.string.brand),
                        value = bike.brand
                    )

                    // Size
                    DetailRow(
                        stringResource(R.string.size),
                        bike.size?.name ?: ""
                    )

                    // Condition
                    DetailRow(
                        label = stringResource(R.string.condition),
                        value = bike.condition?.let { stringResource(it.labelRes()) } ?: ""
                    )

                    // Accessories
                    if (bike.accessories.isNotEmpty()) {
                        AccessoriesRow(
                            label = stringResource(R.string.accessories),
                            accessories = bike.accessories
                        )
                    }
                }
            }

            // Address
            item {
                DetailCard(title = stringResource(R.string.location)) {
                    DetailRow(stringResource(R.string.address_line), bike.location.street)
                    DetailRow(stringResource(R.string.zip_code), bike.location.postalCode)
                    DetailRow(stringResource(R.string.city), bike.location.city)
                }
            }

            // Pricing and dates
            item {
                DetailCard(title = stringResource(R.string.price_details)) {

                    PriceBreakdownCard(
                        bike = bike,
                        startDate = state.startDate,
                        endDate = state.endDate
                    )

                }
            }

            item { Spacer(modifier = Modifier.height(spacing.extraSmall)) }
        }
    }
}

@PreviewLightDark
@Composable
private fun DetailPublicBikeScreenPreview() {
    LokaveloTheme {
        val fakeBike =
            Bike(
                id = "1",
                title = "Vélo gravel Origine Trail Explore",
                description = "Vélo en super état avec fourche suspendue",
                category = BikeCategory.GRAVEL,
                brand = "Origine",
                condition = BikeCondition.LIKE_NEW,
                accessories = listOf(
                    BikeEquipment.HANDLEBAR_BAG, BikeEquipment.MUDGUARD, BikeEquipment.BELL,
                    BikeEquipment.REFLECTIVE_VEST
                ),
                priceInCents = 2500,
                priceTwoDaysInCents = 1250,
                priceWeekInCents = 10000,
                priceMonthInCents = 25000,
                depositInCents = 50000,
                location = BikeLocation(
                    street = "4 bd Longchamp",
                    postalCode = "13001",
                    city = "Marseille"
                )
            )

        val previewState = DetailPublicBikeState(
            bike = fakeBike,
            owner = User(
                displayName = "John Doe",
                bio = "I love cycling :)"
            )
        )

        DetailPublicBikeContent(
            state = previewState,
            onDatesSelected = { _, _ -> }
        )
    }
}