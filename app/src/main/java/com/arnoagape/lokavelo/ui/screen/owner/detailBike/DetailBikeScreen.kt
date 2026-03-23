package com.arnoagape.lokavelo.ui.screen.owner.detailBike

import android.net.Uri
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.domain.model.BikeCategory
import com.arnoagape.lokavelo.domain.model.BikeCondition
import com.arnoagape.lokavelo.domain.model.BikeEquipment
import com.arnoagape.lokavelo.domain.model.BikeLocation
import com.arnoagape.lokavelo.domain.model.BikeSize
import com.arnoagape.lokavelo.domain.model.labelRes
import com.arnoagape.lokavelo.ui.common.EventsEffect
import com.arnoagape.lokavelo.ui.common.components.ConfirmDeleteDialog
import com.arnoagape.lokavelo.ui.common.components.DeletingOverlay
import com.arnoagape.lokavelo.ui.common.components.ErrorOverlay
import com.arnoagape.lokavelo.ui.common.components.ErrorType
import com.arnoagape.lokavelo.ui.common.components.LoadingOverlay
import com.arnoagape.lokavelo.ui.common.components.photo.PhotoItem
import com.arnoagape.lokavelo.ui.common.components.photo.PhotosContent
import com.arnoagape.lokavelo.ui.common.components.photo.ZoomableImageViewer
import com.arnoagape.lokavelo.ui.screen.owner.detailBike.sections.AccessoriesRow
import com.arnoagape.lokavelo.ui.screen.owner.detailBike.sections.DetailCard
import com.arnoagape.lokavelo.ui.screen.owner.detailBike.sections.DetailRow
import com.arnoagape.lokavelo.ui.theme.LocalSpacing
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import com.arnoagape.lokavelo.ui.utils.toDaysString
import com.arnoagape.lokavelo.ui.utils.toEuroString

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

            is DetailBikeEvent.ShowMessage -> {
                val result = snackbarHostState.showSnackbar(
                    message = resources.getString(event.message),
                    actionLabel = resources.getString(R.string.try_again),
                    withDismissAction = true,
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.onEditClicked(bikeId)
                }
            }

            is DetailBikeEvent.ShowSuccessMessage -> {
                Toast.makeText(
                    context,
                    event.message,
                    Toast.LENGTH_SHORT
                ).show()
                onBikeDeleted()
            }

            is DetailBikeEvent.NavigateToEdit -> {
                onEditClick(event.bikeId)
            }
        }
    }

    val bike = (state.bikeState as? DetailBikeUiState.Success)?.bike

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    actionColor = MaterialTheme.colorScheme.error,
                    dismissActionContentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = bike?.title ?: stringResource(R.string.detail_bike),
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
                },
                actions = {
                    bike?.let {
                        IconButton(
                            onClick = { viewModel.onEditClicked(it.id) }
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                stringResource(R.string.cd_edit_bike)
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.requestDeleteConfirmation()
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                stringResource(
                                    R.string.cd_button_delete_the_bike
                                )
                            )
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

            when (state.bikeState) {

                is DetailBikeUiState.Success, DetailBikeUiState.Idle -> {
                    DetailBikeContent(
                        modifier = Modifier.fillMaxSize(),
                        state = state
                    )
                }

                is DetailBikeUiState.Loading -> {
                    LoadingOverlay()
                }

                is DetailBikeUiState.Deleting -> {
                    DeletingOverlay()
                }

                is DetailBikeUiState.Error.Network -> {
                    ErrorOverlay(
                        type = ErrorType.NETWORK,
                        onRetry = { viewModel.setBikeId(bikeId) }
                    )
                }

                is DetailBikeUiState.Error.Generic -> {
                    ErrorOverlay(
                        type = ErrorType.GENERIC,
                        onRetry = { viewModel.setBikeId(bikeId) }
                    )
                }
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
            confirmButtonMessage = stringResource(R.string.delete_irreversible)
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
    val context = LocalContext.current

    var viewerUris by remember { mutableStateOf<List<Uri>?>(null) }
    var viewerStartIndex by remember { mutableIntStateOf(0) }

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
                    photos = bike.photoUrls.map { url ->
                        PhotoItem.Remote(
                            id = url,
                            url = url
                        )
                    },
                    onAddPhotoClick = {},
                    onRemovePhoto = {},
                    onPhotoEdited = { _, _ -> },
                    onMovePhoto = { _, _ -> },
                    onViewerOpen = { uris, index ->
                        viewerUris = uris
                        viewerStartIndex = index
                    },
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

                // Electric?
                val isBikeElectric =
                    if (bike.electric) stringResource(R.string.yes) else stringResource(R.string.no)
                DetailRow(
                    stringResource(R.string.electric_bike),
                    isBikeElectric
                )
                // Brand
                DetailRow(stringResource(R.string.brand), bike.brand)

                // Size
                DetailRow(
                    stringResource(R.string.size),
                    bike.size?.name ?: ""
                )

                // Condition
                DetailRow(
                    stringResource(R.string.condition),
                    bike.condition?.let {
                        stringResource(it.labelRes())
                    } ?: ""
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
                DetailRow(
                    label = stringResource(R.string.address_line),
                    value = bike.location.street,
                    labelWeight = 1f,
                    valueWeight = 2f
                )
                DetailRow(stringResource(R.string.zip_code), bike.location.postalCode)
                DetailRow(stringResource(R.string.city), bike.location.city)
            }
        }

        // Pricing
        item {
            DetailCard(title = stringResource(R.string.pricing)) {

                bike.priceInCents
                    .toEuroString()
                    .let {
                        DetailRow(stringResource(R.string.pricing_day), it)
                    }

                bike.priceTwoDaysInCents
                    ?.toEuroString()
                    ?.let {
                        DetailRow(stringResource(R.string.pricing_two_days), it)
                    }

                bike.priceWeekInCents
                    ?.toEuroString()
                    ?.let {
                        DetailRow(stringResource(R.string.pricing_week), it)
                    }

                bike.priceMonthInCents
                    ?.toEuroString()
                    ?.let {
                        DetailRow(stringResource(R.string.pricing_month), it)
                    }

                DetailRow(
                    stringResource(R.string.deposit),
                    bike.depositInCents
                        ?.toEuroString()
                        ?: stringResource(R.string.no_deposit)
                )
            }
        }

        // Additional options
        item {
            DetailCard(title = stringResource(R.string.additional_section)) {
                DetailRow(
                    label = stringResource(R.string.min_duration_rental_detail),
                    value = bike.minDaysRental.toDaysString(context),
                    labelWeight = 2f,
                    valueWeight = 1f
                )
            }
        }

        item { Spacer(modifier = Modifier.height(spacing.extraSmall)) }
    }

    viewerUris?.let { uris ->
        ZoomableImageViewer(
            uris = uris,
            startIndex = viewerStartIndex,
            onDismiss = { viewerUris = null }
        )
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
                size = BikeSize.M,
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

        val previewState = DetailScreenState(
            bikeState = DetailBikeUiState.Success(fakeBike)
        )

        DetailBikeContent(
            state = previewState
        )
    }
}