package com.arnoagape.lokavelo.ui.screen.main.map

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.ui.common.EventsEffect
import com.arnoagape.lokavelo.ui.common.components.DateRangePickerDialog
import com.arnoagape.lokavelo.ui.common.components.ErrorOverlay
import com.arnoagape.lokavelo.ui.common.components.ErrorType
import com.arnoagape.lokavelo.ui.screen.main.map.components.OSMMap
import com.arnoagape.lokavelo.ui.screen.main.map.components.MapSearchBar
import com.arnoagape.lokavelo.ui.screen.bikes.owner.addBike.sections.AddressLineField
import com.arnoagape.lokavelo.ui.screen.main.map.components.BikeHorizontalList
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseAuth
import org.osmdroid.util.GeoPoint
import java.time.LocalDate
import com.arnoagape.lokavelo.ui.screen.main.map.components.BikeListScreen


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onBikeClick: (String, LocalDate?, LocalDate?) -> Unit
) {

    val state by viewModel.state.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val context = LocalContext.current

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var showAddressSheet by rememberSaveable { mutableStateOf(false) }
    var pendingBikeId by rememberSaveable { mutableStateOf<String?>(null) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var recenterTrigger by rememberSaveable { mutableStateOf(false) }
    var selectedBikeId by rememberSaveable { mutableStateOf<String?>(null) }
    var showListView by rememberSaveable { mutableStateOf(false) }

    // Filters bikes
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid

    val visibleBikes = state.filteredBikes.filter {
        it.ownerId != currentUserId
    }

    val selectedBike = state.filteredBikes.find { it.id == selectedBikeId }
    val geoPoint = userLocation?.let {
        GeoPoint(it.latitude, it.longitude)
    }

    val locationPermissionState =
        rememberPermissionState(
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            viewModel.refreshLocation()
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }

    EventsEffect(viewModel.eventsFlow) { event ->
        when (event) {
            is MapEvent.ShowMessage -> {
                snackbarHostState.showSnackbar(
                    message = resources.getString(event.message),
                    duration = SnackbarDuration.Short
                )
            }

            is MapEvent.ShowSuccessMessage -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showAddressSheet) {

        ModalBottomSheet(
            onDismissRequest = { showAddressSheet = false }
        ) {

            var text by remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }

            Column(Modifier.padding(16.dp)) {

                AddressLineField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    value = text,
                    suggestions = state.suggestions,
                    addressError = false,
                    onValueChange = {
                        text = it
                        viewModel.onAddressQueryChange(it)
                    },
                    onSuggestionSelected = { suggestion ->
                        viewModel.updateAddressFromSuggestion(suggestion)
                        keyboardController?.hide()
                        showAddressSheet = false
                    }
                )

                Spacer(Modifier.height(16.dp))

                // 📍 Current position
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.clearLocationFilter()
                            keyboardController?.hide()
                            showAddressSheet = false
                            recenterTrigger = true
                        }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = stringResource(R.string.current_position),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        DateRangePickerDialog(
            onDismiss = { showDatePicker = false },
            onDatesSelected = { start, end ->
                viewModel.updateDates(start, end)
                showDatePicker = false

                pendingBikeId?.let {
                    onBikeClick(it, start, end)
                    pendingBikeId = null
                }
            }
        )
    }

    Box(Modifier.fillMaxSize()) {

        // Carte
        OSMMap(
            userLocation = geoPoint,
            bikes = visibleBikes,
            filters = state.filters,
            recenterTrigger = recenterTrigger,
            onRecenterHandled = { recenterTrigger = false },
            onBikeClicked = { bike ->
                selectedBikeId = bike.id
            },
            onMapTapped = {
                selectedBikeId = null
            }
        )

        // Barre + Dates
        Column {
            MapSearchBar(
                filters = state.filters,
                maxBikePrice = state.maxBikePrice,
                onAddressClick = { showAddressSheet = true },
                onDatesSelected = { start, end ->
                    viewModel.updateDates(start, end)
                },
                onCategorySelected = { category ->
                    viewModel.updateBikeCategory(category)
                },
                onMotorTypeChanged = { motorTypes ->
                    viewModel.updateMotorTypeFilter(motorTypes)
                },
                onFiltersSelected = { size, accessories, minPrice, maxPrice ->
                    viewModel.updateFilters(size, accessories, minPrice, maxPrice)
                }
            )

            Spacer(Modifier.weight(1f))

            // Boutons flottants
            SmallFloatingActionButton(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = {
                    viewModel.clearLocationFilter()
                    recenterTrigger = true
                },
                modifier = Modifier
                    .padding(end = 16.dp, bottom = 150.dp)
                    .align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = stringResource(R.string.recenter)
                )
            }
        }

        // Vue liste ou cadre vélo
        if (showListView) {
            BikeListScreen(
                bikes = visibleBikes,
                filters = state.filters,
                onBikeClick = { bike ->
                    if (viewModel.onBikeCardClicked()) {
                        if (state.filters.startDate == null || state.filters.endDate == null) {
                            pendingBikeId = bike.id
                            showDatePicker = true
                        } else {
                            onBikeClick(bike.id, state.filters.startDate, state.filters.endDate)
                        }
                    }
                },
                onBack = { showListView = false }
            )
        } else {
            // Bouton Liste — visible seulement si aucune card sélectionnée
            if (selectedBikeId == null) {
                Button(
                    onClick = { showListView = true },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.list))
                }
            }

            // Card vélo
            selectedBike?.let { bike ->
                BikeHorizontalList(
                    bikes = visibleBikes,
                    filters = state.filters,
                    onBikeClick = {
                        if (viewModel.onBikeCardClicked()) {
                            if (state.filters.startDate == null || state.filters.endDate == null) {
                                pendingBikeId = bike.id
                                showDatePicker = true
                            } else {
                                onBikeClick(bike.id, state.filters.startDate, state.filters.endDate)
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                )
            }
        }


        // Erreur réseau
        if (state.networkError) {
            ErrorOverlay(
                type = ErrorType.NETWORK,
                onRetry = { viewModel.clearNetworkError() }
            )
        }
    }
}