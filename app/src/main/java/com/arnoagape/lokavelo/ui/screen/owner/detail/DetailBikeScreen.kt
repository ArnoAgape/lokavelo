package com.arnoagape.lokavelo.ui.screen.owner.detail

import android.widget.Toast
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
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.arnoagape.lokavelo.ui.common.components.ConfirmDeleteDialog
import com.arnoagape.lokavelo.ui.common.components.ErrorOverlay
import com.arnoagape.lokavelo.ui.common.components.LoadingOverlay
import com.arnoagape.lokavelo.ui.screen.owner.addBike.sections.PhotosContent
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailBikeScreen(
    bikeId: String,
    viewModel: DetailBikeViewModel,
    onEditClick: (String) -> Unit,
    onBikeDeleted: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsStateWithLifecycle()

    val resources = LocalResources.current
    val context = LocalContext.current
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

            is Event.ShowSuccessMessage -> {
                Toast.makeText(
                    context,
                    event.message,
                    Toast.LENGTH_SHORT
                ).show()
                onBikeDeleted()
            }
        }
    }

    val bike = (state.bikeState as? DetailBikeUiState.Success)?.bike

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },

        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detail_bike)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    bike?.let {
                        IconButton(
                            onClick = { onEditClick(it.id) }
                        ) {
                            Icon(Icons.Default.Edit, null)
                        }

                        IconButton(
                            onClick = {
                                viewModel.requestDeleteConfirmation()
                            }
                        ) {
                            Icon(Icons.Default.Delete, null)
                        }
                    }
                }
            )
        }

    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
                .fillMaxSize()
        ) {

            // 🎯 CONTENU PRINCIPAL
            DetailBikeContent(
                modifier = Modifier.fillMaxSize(),
                state = state
            )

            // 🎯 OVERLAY LOADING
            if (state.bikeState is DetailBikeUiState.Loading) {
                LoadingOverlay(
                    text = stringResource(R.string.loading)
                )
            }

            // 🎯 ERREUR OVERLAY
            if (state.bikeState is DetailBikeUiState.Error) {
                ErrorOverlay(
                    message = stringResource(R.string.error_generic)
                )
            }
        }
    }

    // 🎯 DIALOG SUPPRESSION
    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            show = true,
            onConfirm = {
                viewModel.dismissDeleteDialog()
                viewModel.deleteBike()
            },
            onDismiss = {
                viewModel.dismissDeleteDialog()
            },
            confirmButtonTitle = stringResource(R.string.confirm_delete_bike),
            confirmButtonMessage = stringResource(R.string.confirm_delete_message_current_bike)
        )
    }
}

@Composable
fun DetailBikeContent(
    modifier: Modifier = Modifier,
    state: DetailScreenState
) {
    val ui = state.bikeState

    if (ui is DetailBikeUiState.Success) {
        DetailItem(
            modifier = modifier,
            bike = ui.bike
        )
    }
}

@Composable
fun DetailItem(
    modifier: Modifier = Modifier,
    bike: Bike
) {

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

        // Pictures
        item {
            DetailCard(title = stringResource(R.string.pictures)) {
                PhotosContent(
                    uris = bike.photoUrls.map { it.toUri() },
                    isEditable = false
                )
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

        // Title & Description
        item {
            DetailCard(title = stringResource(R.string.title_description)) {
                Text(bike.title)
                Text(bike.description)
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
            bikeState = DetailBikeUiState.Success(fakeBike)
        )

        DetailBikeContent(
            state = previewState
        )
    }
}