package com.arnoagape.lokavelo.ui.screen.main.map

import android.location.Location
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.ui.common.EventsEffect
import com.arnoagape.lokavelo.ui.screen.main.map.components.OSMMap
import com.arnoagape.lokavelo.ui.screen.main.map.components.SearchBar
import com.arnoagape.lokavelo.ui.screen.owner.addBike.sections.AddressLineField
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel
) {

    val state by viewModel.state.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val context = LocalContext.current

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var showAddressSheet by remember { mutableStateOf(false) }
    var recenterTrigger by remember { mutableStateOf(false) }

    val locationPermissionState =
        rememberPermissionState(
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )

    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            viewModel.userLocation
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

                // 📍 Position actuelle
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

    Box(Modifier.fillMaxSize()) {

        // Carte
        MapContent(
            userLocation = userLocation,
            bikes = state.filteredBikes,
            state = state,
            recenterTrigger = recenterTrigger,
            onRecenterHandled = { recenterTrigger = false }
        )

        // Barre + Dates
        Column {

            SearchBar(
                filters = state.filters,
                onAddressClick = { showAddressSheet = true },
                onDatesSelected = { start, end ->
                    viewModel.updateDates(
                        start.atStartOfDay(),
                        end.atStartOfDay()
                    )
                }
            )

            Spacer(Modifier.weight(1f))

            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = { viewModel.clearLocationFilter()
                    recenterTrigger = true },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = stringResource(R.string.recenter)
                )
            }
        }
    }
}

@Composable
fun MapContent(
    userLocation: Location?,
    bikes: List<Bike>,
    state: HomeScreenState,
    recenterTrigger: Boolean,
    onRecenterHandled: () -> Unit
) {

    val geoPoint = userLocation?.let {
        org.osmdroid.util.GeoPoint(it.latitude, it.longitude)
    }

    OSMMap(
        userLocation = geoPoint,
        bikes = bikes,
        filters = state.filters,
        recenterTrigger = recenterTrigger,
        onRecenterHandled = onRecenterHandled
    )

}