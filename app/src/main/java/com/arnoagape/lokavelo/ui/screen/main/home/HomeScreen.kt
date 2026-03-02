package com.arnoagape.lokavelo.ui.screen.main.home

import android.location.Location
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import com.arnoagape.lokavelo.domain.model.Bike
import com.arnoagape.lokavelo.ui.common.EventsEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel
) {

    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val context = LocalContext.current
    val userLocation by viewModel.userLocation.collectAsState()

    val locationPermissionState =
        rememberPermissionState(
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )

    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            viewModel.fetchUserLocation()
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }

    /*BackHandler(
        enabled = state.selection.isSelectionMode || state.isSearchActive
    ) {
        when {
            state.selection.isSelectionMode ->
                viewModel.exitSelectionMode()

            state.isSearchActive ->
                viewModel.toggleSearch()
        }
    }*/

    EventsEffect(viewModel.eventsFlow) { event ->
        when (event) {
            is HomeEvent.ShowMessage -> {
                snackbarHostState.showSnackbar(
                    message = resources.getString(event.message),
                    duration = SnackbarDuration.Short
                )
            }

            is HomeEvent.ShowSuccessMessage -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(Modifier.fillMaxSize()) {

        MapContent(
            userLocation = userLocation,
            bikes = emptyList() // on branchera plus tard
        )

        // Ici tu peux ajouter :
        // - barre de recherche
        // - bouton liste
        // - bottom nav
    }
}

@Composable
fun MapContent(
    userLocation: Location?,
    bikes: List<Bike>
) {

    val geoPoint = userLocation?.let {
        org.osmdroid.util.GeoPoint(it.latitude, it.longitude)
    }

    OSMMap(
        userLocation = geoPoint,
        bikes = bikes
    )
}