package com.arnoagape.lokavelo.ui.screen.owner.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.BikeCategory
import com.arnoagape.lokavelo.domain.model.BikeCondition
import com.arnoagape.lokavelo.domain.model.BikeEquipment
import com.arnoagape.lokavelo.domain.model.BikeLocation
import com.arnoagape.lokavelo.domain.model.labelRes
import com.arnoagape.lokavelo.ui.common.Event
import com.arnoagape.lokavelo.ui.common.EventsEffect
import com.arnoagape.lokavelo.ui.screen.owner.addBike.sections.PhotosContent
import com.arnoagape.lokavelo.ui.screen.owner.addBike.sections.PhotosSection
import com.arnoagape.lokavelo.ui.screen.owner.detail.sections.AccessoriesRow
import com.arnoagape.lokavelo.ui.screen.owner.detail.sections.DetailCard
import com.arnoagape.lokavelo.ui.screen.owner.detail.sections.DetailRow
import com.arnoagape.lokavelo.ui.theme.LocalSpacing
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import java.text.NumberFormat
import java.util.Locale

/**
 * Displays the detail view of a file.
 *
 * @param viewModel The ViewModel providing file data and state.
 */
@Composable
fun DetailBikeScreen(
    bikeId: String,
    viewModel: DetailBikeViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val resources = LocalResources.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(bikeId) {
        viewModel.setBikeId(bikeId)
    }

    EventsEffect(viewModel.eventsFlow) { event ->
        when (event) {
            is Event.ShowMessage -> {
                snackbarHostState.showSnackbar(
                    message = resources.getString(event.message),
                    duration = SnackbarDuration.Short
                )
            }

            else -> Unit
        }
    }

    DetailBikeContent(state = state)

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailBikeContent(
    state: DetailScreenState
) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when (val ui = state.uiState) {

            is DetailBikeUiState.Success -> {
                DetailItem(bike = ui.bike)
            }

            is DetailBikeUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is DetailBikeUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.error_generic))
                }
            }
        }
    }
}

@Composable
fun DetailItem(bike: Bike) {

    val spacing = LocalSpacing.current

    // Prix
    val formattedPrice = remember(bike.priceInCents) {
        val priceInEuros = bike.priceInCents / 100.0
        NumberFormat
            .getCurrencyInstance(Locale.FRANCE)
            .format(priceInEuros)
    }

    // Caution
    val formattedDeposit = remember(bike.depositInCents) {
        bike.depositInCents?.let { cents ->
            val depositInEuros = cents / 100.0
            NumberFormat
                .getCurrencyInstance(Locale.FRANCE)
                .format(depositInEuros)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(
            start = spacing.medium,
            end = spacing.medium
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.large)
    ) {
        item { Spacer(modifier = Modifier.height(spacing.extraSmall)) }

        // Pictures
        item {
            DetailCard(title = stringResource(R.string.pictures)) {
                PhotosContent(
                    uris = bike.photoUrls.map { it.toUri() },
                    isEditable = false
                )
            }
        }

        // Title & Description
        item {
            DetailCard(title = stringResource(R.string.title_description)) {
                Text(bike.title)
                Text(bike.description)
            }
        }

        // Characteristics
        item {
            DetailCard(title = stringResource(R.string.characteristics)) {

                // Category
                DetailRow(
                    stringResource(R.string.category),
                    bike.category?.let {
                        stringResource(it.labelRes())
                    } ?: ""
                )
                // Brand
                DetailRow(stringResource(R.string.brand), bike.brand)

                // Condition
                DetailRow(
                    stringResource(R.string.condition),
                    bike.condition?.let {
                        stringResource(it.labelRes())
                    } ?: ""
                )

                // Accessories
                AccessoriesRow(
                    label = stringResource(R.string.accessories),
                    accessories = bike.accessories
                )
            }
        }

        // Pricing
        item {
            DetailCard(title = stringResource(R.string.pricing)) {
                DetailRow(stringResource(R.string.pricing_day), formattedPrice)
                DetailRow(
                    stringResource(R.string.deposit),
                    formattedDeposit ?: stringResource(R.string.no_deposit)
                )
            }
        }

        // Address
        item {
            DetailCard(title = stringResource(R.string.location)) {
                DetailRow(stringResource(R.string.address_line), bike.location.street)
                if (bike.location.addressLine2.isNotBlank()) {
                    DetailRow(
                        stringResource(R.string.address_line_2_optional),
                        bike.location.addressLine2
                    )
                }
                DetailRow(stringResource(R.string.zip_code), bike.location.postalCode)
                DetailRow(stringResource(R.string.city), bike.location.city)
            }
        }

        item { Spacer(modifier = Modifier.height(spacing.extraSmall)) }
    }
}

@PreviewLightDark
@Composable
private fun DetailBikeScreenPreview() {
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
                depositInCents = 50000,
                location = BikeLocation(
                    street = "4 bd Longchamp",
                    postalCode = "13001",
                    city = "Marseille"
                )
            )

        val previewState = DetailScreenState(
            uiState = DetailBikeUiState.Success(fakeBike)
        )

        DetailBikeContent(state = previewState)
    }
}