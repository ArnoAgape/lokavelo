package com.arnoagape.lokavelo.ui.screen.owner.addBike

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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
fun AddBikeScreen(
    viewModel: AddBikeViewModel,
    onSaveClick: () -> Unit,
    onClose: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resources = LocalResources.current
    val snackbarHostState = remember { SnackbarHostState() }

    EventsEffect(viewModel.eventsFlow) { event ->
        when (event) {
            is Event.ShowMessage -> {
                snackbarHostState.showSnackbar(
                    message = resources.getString(event.message)
                )
            }

            is Event.ShowSuccessMessage -> {
                Toast.makeText(
                    context,
                    R.string.success_bike_added,
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
                title = { Text(stringResource(R.string.add_bike)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            )
        },

        bottomBar = {
            PublishButton(
                enabled = state.isValid,
                onClick = {
                    viewModel.onAction(AddBikeEvent.Submit)
                },
                isSubmitting = state.uiState is AddBikeUiState.Submitting
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
            AddBikeContent(
                modifier = Modifier.fillMaxSize(),
                state = state,
                onAction = viewModel::onAction
            )

            // 🎯 OVERLAY LOADING / SUBMIT
            if (
                state.uiState is AddBikeUiState.Loading ||
                state.uiState is AddBikeUiState.Submitting
            ) {
                LoadingOverlay(
                    text = if (state.uiState is AddBikeUiState.Submitting)
                        stringResource(R.string.publishing)
                    else
                        stringResource(R.string.loading)
                )
            }

            // 🎯 ERREUR PLEIN ÉCRAN
            if (state.uiState is AddBikeUiState.Error) {
                ErrorOverlay(
                    message = stringResource(R.string.error_generic)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBikeContent(
    modifier: Modifier = Modifier,
    state: AddBikeScreenState,
    onAction: (AddBikeEvent) -> Unit
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
                onAction(AddBikeEvent.AddPhoto(photoUri!!))
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
            uri?.let { onAction(AddBikeEvent.AddPhoto(it)) }
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
                uris = state.localUris,
                onAddPhotoClick = { showSheet = true },
                onRemovePhoto = { uri ->
                    onAction(AddBikeEvent.RemovePhoto(uri))
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
                onCategoryChange = { onAction(AddBikeEvent.CategoryChanged(it)) },
                onBrandChange = { onAction(AddBikeEvent.BrandChanged(it)) },
                onStateChange = { onAction(AddBikeEvent.StateChanged(it)) },
                onElectricChange = { onAction(AddBikeEvent.ElectricChanged(it)) },
                onAccessoriesChange = { onAction(AddBikeEvent.AccessoriesChanged(it)) }
            )
        }

        item {
            TitleDescriptionSection(
                title = state.form.title,
                description = state.form.description,
                onTitleChange = {
                    onAction(AddBikeEvent.TitleChanged(it))
                },
                onDescriptionChange = {
                    onAction(AddBikeEvent.DescriptionChanged(it))
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
                    onAction(AddBikeEvent.AddressChanged(it))
                },
                onAddressLine2Change = {
                    onAction(AddBikeEvent.Address2Changed(it))
                },
                onZipCodeChange = {
                    onAction(AddBikeEvent.ZipChanged(it))
                },
                onCityChange = {
                    onAction(AddBikeEvent.CityChanged(it))
                }
            )
        }

        item {
            PricingSection(
                price = state.form.priceText,
                onPriceChange = {
                    onAction(AddBikeEvent.PriceChanged(it))
                }
            )
        }

        item {
            DepositSection(
                deposit = state.form.depositText,
                onDepositChange = {
                    onAction(AddBikeEvent.DepositChanged(it))
                }
            )
        }

        item { Spacer(modifier = Modifier.height(spacing.extraSmall)) }
    }
}

@PreviewLightDark
@Composable
private fun AddBikeContentPreview() {
    LokaveloTheme {
        AddBikeContent(
            state = AddBikeScreenState(),
            onAction = {}
        )
    }
}