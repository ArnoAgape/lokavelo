package com.arnoagape.lokavelo.ui.screen.owner.editBike

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.ui.common.Event
import com.arnoagape.lokavelo.ui.common.EventsEffect
import com.arnoagape.lokavelo.ui.common.components.ErrorOverlay
import com.arnoagape.lokavelo.ui.common.components.LoadingOverlay
import com.arnoagape.lokavelo.ui.screen.owner.addBike.sections.CharacteristicsSection
import com.arnoagape.lokavelo.ui.screen.owner.addBike.sections.DepositSection
import com.arnoagape.lokavelo.ui.screen.owner.addBike.sections.LocationSection
import com.arnoagape.lokavelo.ui.screen.owner.addBike.sections.PhotosSection
import com.arnoagape.lokavelo.ui.screen.owner.addBike.sections.PricingSection
import com.arnoagape.lokavelo.ui.screen.owner.addBike.sections.PublishButton
import com.arnoagape.lokavelo.ui.screen.owner.addBike.sections.TitleDescriptionSection
import com.arnoagape.lokavelo.ui.theme.LocalSpacing
import com.arnoagape.lokavelo.ui.theme.LokaveloTheme
import com.arnoagape.lokavelo.ui.utils.createImageUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBikeScreen(
    bikeId: String,
    viewModel: EditBikeViewModel,
    onSaveClick: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resources = LocalResources.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(bikeId) {
        viewModel.setBikeId(bikeId)
        viewModel.resetSubmitting()
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
                    R.string.success_bike_edited,
                    Toast.LENGTH_SHORT
                ).show()
                onSaveClick()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },

        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit)) },
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
            PublishButton(
                enabled = state.isValid,
                onClick = {
                    viewModel.onAction(EditBikeEvent.Submit)
                },
                isSubmitting = state.uiState is EditBikeUiState.Submitting
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
            EditBikeContent(
                modifier = Modifier.fillMaxSize(),
                state = state,
                onAction = viewModel::onAction,
                removeRemotePhoto = viewModel::removeRemotePhoto
            )

            // 🎯 OVERLAY LOADING / SUBMIT
            if (
                state.uiState is EditBikeUiState.Loading ||
                state.uiState is EditBikeUiState.Submitting
            ) {
                LoadingOverlay(
                    text = if (state.uiState is EditBikeUiState.Submitting)
                        stringResource(R.string.publishing)
                    else
                        stringResource(R.string.loading)
                )
            }

            // 🎯 ERREUR PLEIN ÉCRAN
            if (state.uiState is EditBikeUiState.Error) {
                ErrorOverlay(
                    message = stringResource(R.string.error_generic)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditBikeContent(
    modifier: Modifier = Modifier,
    state: EditBikeScreenState,
    onAction: (EditBikeEvent) -> Unit,
    removeRemotePhoto: (String) -> Unit
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
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            uri?.let { onAction(EditBikeEvent.AddPhoto(it)) }
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
            val allUris = state.remotePhotoUrls.map { it.toUri() } + state.localUris

            PhotosSection(
                uris = allUris,
                onAddPhotoClick = { showSheet = true },
                onRemovePhoto = { uri ->

                    val remoteMatch = state.remotePhotoUrls
                        .firstOrNull { it == uri.toString() }

                    if (remoteMatch != null) {
                        removeRemotePhoto(remoteMatch)
                    } else {
                        onAction(EditBikeEvent.RemovePhoto(uri))
                    }
                },
                isEditable = true
            )
        }

        item {
            CharacteristicsSection(
                category = state.form.category,
                brand = state.form.brand,
                state = state.form.condition,
                isElectric = state.form.isElectric,
                accessories = state.form.accessories,
                onCategoryChange = { onAction(EditBikeEvent.CategoryChanged(it)) },
                onBrandChange = { onAction(EditBikeEvent.BrandChanged(it)) },
                onStateChange = { onAction(EditBikeEvent.StateChanged(it)) },
                onElectricChange = { onAction(EditBikeEvent.ElectricChanged(it)) },
                onAccessoriesChange = { onAction(EditBikeEvent.AccessoriesChanged(it)) }
            )
        }

        item {
            TitleDescriptionSection(
                title = state.form.title,
                description = state.form.description,
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
                addressLine2 = state.form.location.addressLine2,
                zipCode = state.form.location.postalCode,
                city = state.form.location.city,
                onAddressLineChange = {
                    onAction(EditBikeEvent.AddressChanged(it))
                },
                onAddressLine2Change = {
                    onAction(EditBikeEvent.Address2Changed(it))
                },
                onZipCodeChange = {
                    onAction(EditBikeEvent.ZipChanged(it))
                },
                onCityChange = {
                    onAction(EditBikeEvent.CityChanged(it))
                }
            )
        }

        item {
            PricingSection(
                price = state.form.priceText,
                onPriceChange = {
                    onAction(EditBikeEvent.PriceChanged(it))
                }
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
            removeRemotePhoto = {}
        )
    }
}