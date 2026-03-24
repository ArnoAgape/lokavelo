package com.arnoagape.lokavelo.ui.screen.bikes.owner.editBike

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.domain.model.AddressSuggestion
import com.arnoagape.lokavelo.ui.common.Event
import com.arnoagape.lokavelo.ui.common.EventsEffect
import com.arnoagape.lokavelo.ui.common.components.AlertDialogNonSaved
import com.arnoagape.lokavelo.ui.common.components.LoadingOverlay
import com.arnoagape.lokavelo.ui.common.components.photo.PhotosSection
import com.arnoagape.lokavelo.ui.screen.bikes.owner.addBike.sections.AdditionalSection
import com.arnoagape.lokavelo.ui.screen.bikes.owner.addBike.sections.CharacteristicsSection
import com.arnoagape.lokavelo.ui.screen.bikes.owner.addBike.sections.DepositSection
import com.arnoagape.lokavelo.ui.screen.bikes.owner.addBike.sections.LocationSection
import com.arnoagape.lokavelo.ui.screen.bikes.owner.addBike.sections.PricingSection
import com.arnoagape.lokavelo.ui.screen.bikes.owner.addBike.sections.SubmitButton
import com.arnoagape.lokavelo.ui.screen.bikes.owner.addBike.sections.TitleDescriptionSection
import com.arnoagape.lokavelo.ui.theme.LocalSpacing
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import com.arnoagape.lokavelo.ui.utils.createImageUri
import com.arnoagape.lokavelo.ui.utils.vibrateError
import com.arnoagape.lokavelo.ui.utils.vibrateSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBikeScreen(
    bikeId: String,
    viewModel: EditBikeViewModel,
    onSaveClick: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val hasUnsavedChanges by viewModel.hasUnsavedChanges.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resources = LocalResources.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (hasUnsavedChanges) {
            showExitDialog = true
        } else {
            onBack()
        }
    }

    LaunchedEffect(bikeId) {
        viewModel.setBikeId(bikeId)
    }

    EventsEffect(viewModel.eventsFlow) { event ->
        when (event) {
            is Event.ShowMessage -> {
                context.vibrateError()
                val result = snackbarHostState.showSnackbar(
                    message = resources.getString(event.message),
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.onAction(EditBikeEvent.Submit)
                }
            }

            is Event.ShowSuccessMessage -> {
                context.vibrateSuccess()
                Toast.makeText(
                    context,
                    R.string.success_bike_edited,
                    Toast.LENGTH_SHORT
                ).show()
                onSaveClick()
            }
        }
    }

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
                title = { Text(stringResource(R.string.edit)) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (hasUnsavedChanges) {
                                showExitDialog = true
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            stringResource(R.string.cd_go_back)
                        )
                    }
                }
            )
        },

        bottomBar = {
            if (state.uiState !is EditBikeUiState.Error.Initial &&
                state.uiState !is EditBikeUiState.Loading
            ) {
                SubmitButton(
                    modifier = Modifier.imePadding(),
                    enabled = !state.isSaving,
                    onClick = {
                        viewModel.onAction(EditBikeEvent.Submit)
                    },
                    isLoading = state.isSaving,
                    submitText = stringResource(R.string.button_edit_bike)
                )
            }
        }

    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
                .fillMaxSize()
        ) {

            when (state.uiState) {

                is EditBikeUiState.Loaded -> {
                    EditBikeContent(
                        modifier = Modifier.fillMaxSize(),
                        state = state,
                        onAction = viewModel::onAction,
                        onMovePhoto = viewModel::movePhoto,
                        suggestions = state.suggestions,
                        onSuggestionSelected = viewModel::onSuggestionSelected
                    )
                }

                is EditBikeUiState.Loading -> {
                    LoadingOverlay()
                }

                else -> Unit
            }
        }

        if (showExitDialog) {
            AlertDialogNonSaved(
                onConfirm = {
                    showExitDialog = false
                    onBack()
                },
                onDismiss = { showExitDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditBikeContent(
    modifier: Modifier = Modifier,
    state: EditBikeScreenState,
    onMovePhoto: (Int, Int) -> Unit,
    onAction: (EditBikeEvent) -> Unit,
    suggestions: List<AddressSuggestion>,
    onSuggestionSelected: (AddressSuggestion) -> Unit
) {
    val spacing = LocalSpacing.current
    val context = LocalContext.current

    var showSheet by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // ---------------- CAMERA ----------------

    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success && photoUri != null) {
                onAction(EditBikeEvent.AddPhoto(photoUri!!))
            }
        }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                val uri = createImageUri(context)
                photoUri = uri
                cameraLauncher.launch(uri)
            }
        }

    fun launchCamera() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val uri = createImageUri(context)
            photoUri = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ---------------- GALLERY (modern picker) ----------------

    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia()
        ) { uris ->

            if (uris.isNotEmpty()) {
                uris.forEach { uri ->
                    onAction(EditBikeEvent.AddPhoto(uri))
                }
            }
        }

    fun launchGallery() {
        galleryLauncher.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }

    // ---------------- BOTTOM SHEET ----------------

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.primary
                )
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) {

            ListItem(
                headlineContent = { Text(stringResource(R.string.take_picture)) },
                leadingContent = { Icon(Icons.Default.CameraAlt, null) },
                modifier = Modifier.clickable {
                    launchCamera()
                    showSheet = false
                }
            )

            ListItem(
                headlineContent = { Text(stringResource(R.string.browse_gallery)) },
                leadingContent = { Icon(Icons.Default.Photo, null) },
                modifier = Modifier.clickable {
                    launchGallery()
                    showSheet = false
                }
            )
        }
    }

    // ---------------- SCREEN CONTENT ----------------

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

        item { Spacer(modifier = modifier.height(spacing.extraSmall)) }

        item {
            PhotosSection(
                photos = state.photos,
                photosError = state.form.photosError,
                onAddPhotoClick = { showSheet = true },
                onRemovePhoto = { id ->
                    onAction(EditBikeEvent.RemovePhoto(id))
                },
                onPhotoEdited = { id, newUri ->
                    onAction(EditBikeEvent.ReplacePhoto(id, newUri))
                },
                onMovePhoto = onMovePhoto,
                isEditable = true
            )
        }

        item {
            CharacteristicsSection(
                category = state.form.category,
                brand = state.form.brand,
                condition = state.form.condition,
                electric = state.form.electric,
                size = state.form.size,
                accessories = state.form.accessories,
                categoryError = state.form.categoryError,
                conditionError = state.form.conditionError,
                sizeError = state.form.sizeError,
                onCategoryChange = { onAction(EditBikeEvent.CategoryChanged(it)) },
                onBrandChange = { onAction(EditBikeEvent.BrandChanged(it)) },
                onStateChange = { onAction(EditBikeEvent.StateChanged(it)) },
                onElectricChange = { onAction(EditBikeEvent.ElectricChanged(it)) },
                onAccessoriesChange = { onAction(EditBikeEvent.AccessoriesChanged(it)) },
                onSizeChange = { onAction(EditBikeEvent.SizeChanged(it)) }
            )
        }

        item {
            TitleDescriptionSection(
                title = state.form.title,
                description = state.form.description,
                titleError = state.form.titleError,
                descriptionError = state.form.descriptionError,
                onTitleChange = {
                    onAction(EditBikeEvent.TitleChanged(it))
                },
                onDescriptionChange = {
                    onAction(EditBikeEvent.DescriptionChanged(it))
                }
            )
        }

        item {
            LocationSection(
                addressLine = state.form.location.street,
                zipCode = state.form.location.postalCode,
                city = state.form.location.city,
                addressError = state.form.streetError,
                zipCodeError = state.form.postalCodeError,
                cityError = state.form.cityError,
                suggestions = suggestions,
                onAddressLineChange = {
                    onAction(EditBikeEvent.AddressChanged(it))
                },
                onZipCodeChange = {
                    onAction(EditBikeEvent.ZipChanged(it))
                },
                onCityChange = {
                    onAction(EditBikeEvent.CityChanged(it))
                },
                onSuggestionSelected = onSuggestionSelected
            )
        }

        item {
            PricingSection(
                price = state.form.priceText,
                priceError = state.form.priceError,
                onPriceChange = {
                    onAction(EditBikeEvent.PriceChanged(it))
                },
                monthPrice = state.form.monthPriceText,
                twoDaysPrice = state.form.twoDaysPriceText,
                weekPrice = state.form.weekPriceText,
                onTwoDaysChange = { onAction(EditBikeEvent.TwoDaysPriceChanged(it)) },
                onWeekChange = { onAction(EditBikeEvent.WeekPriceChanged(it)) },
                onMonthChange = { onAction(EditBikeEvent.MonthPriceChanged(it)) }
            )
        }

        item {
            DepositSection(
                deposit = state.form.depositText,
                onDepositChange = {
                    onAction(EditBikeEvent.DepositChanged(it))
                }
            )
        }

        item {
            AdditionalSection(
                availability = state.form.available,
                minDaysRental = state.form.minDaysRentalText,
                onAvailabilityChange = {
                    onAction(EditBikeEvent.AvailableChanged(it))
                },
                minDaysRentalChange = {
                    onAction(EditBikeEvent.MinDaysRentalChanged(it))
                },
                minDaysRentalError = state.form.minDaysRentalError,
            )
        }

        item { Spacer(modifier = Modifier.height(spacing.extraSmall)) }
    }
}

@PreviewLightDark
@Composable
private fun EditBikeContentPreview() {
    LokaveloTheme {
        EditBikeContent(
            state = EditBikeScreenState(),
            onAction = {},
            onMovePhoto = { _, _ -> },
            suggestions = emptyList(),
            onSuggestionSelected = {}
        )
    }
}