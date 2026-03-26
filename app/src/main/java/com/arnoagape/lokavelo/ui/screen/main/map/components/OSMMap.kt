package com.arnoagape.lokavelo.ui.screen.main.map.components

import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.preference.PreferenceManager
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.arnoagape.lokavelo.domain.model.Bike
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import androidx.core.graphics.toColorInt
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.ui.screen.main.map.SearchFilters
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.MapEventsOverlay
import java.time.temporal.ChronoUnit

@Composable
fun OSMMap(
    userLocation: GeoPoint?,
    bikes: List<Bike>,
    filters: SearchFilters,
    recenterTrigger: Boolean,
    onRecenterHandled: () -> Unit,
    onBikeClicked: (Bike) -> Unit,
    onMapTapped: () -> Unit
) {

    var lastCentered by remember { mutableStateOf<GeoPoint?>(null) }
    var lastBikes by remember { mutableStateOf<List<Bike>>(emptyList()) }
    var lastSelectedDays by remember { mutableLongStateOf(0L) }

    var bikeMarkers by remember { mutableStateOf<List<Marker>>(emptyList()) }
    var userCircle by remember { mutableStateOf<Polygon?>(null) }
    var cityCircle by remember { mutableStateOf<Polygon?>(null) }
    var searchMarker by remember { mutableStateOf<Marker?>(null) }

    val primaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f).toArgb()

    // val isDarkTheme = isSystemInDarkTheme()
    // val tileUrl = if (isDarkTheme) "voyager_dark_matter " else "voyager"
    // val name = if (isDarkTheme) "CartoDarkMatter" else "CartoVoyager"

    val tileSource = XYTileSource(
        "CartoVoyager",
        0, 19, 256, ".png",
        arrayOf(
            "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
            "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
            "https://c.basemaps.cartocdn.com/rastertiles/voyager/"
        )
    )

    Box(Modifier.fillMaxSize()) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->

                MapView(context).apply {

                    Configuration.getInstance().userAgentValue = context.packageName
                    Configuration.getInstance().load(
                        context,
                        PreferenceManager.getDefaultSharedPreferences(context)
                    )

                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    zoomController.setVisibility(
                        CustomZoomButtonsController.Visibility.NEVER
                    )

                    setTileSource(tileSource)
                    setUseDataConnection(true)
                    setMultiTouchControls(true)

                    controller.setZoom(14.0)
                    controller.setCenter(GeoPoint(43.2965, 5.3698))

                    val mapEventsReceiver = object : MapEventsReceiver {

                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            onMapTapped()
                            return true
                        }

                        override fun longPressHelper(p: GeoPoint?): Boolean = false
                    }

                    overlays.add(MapEventsOverlay(mapEventsReceiver))
                }
            },
            update = { mapView ->

                // 🔁 Recentrage bouton
                if (recenterTrigger && userLocation != null) {

                    mapView.controller.apply {
                        setZoom(16.0)
                        animateTo(userLocation)
                    }

                    lastCentered = userLocation
                    onRecenterHandled()
                }

                // 📍 Premier centrage automatique sur la position
                if (userLocation != null && lastCentered == null) {
                    mapView.controller.setZoom(16.0)
                    mapView.controller.setCenter(userLocation)
                    lastCentered = userLocation
                }

                // 🔥 Recentrage nouvelle ville
                filters.center?.let { center ->
                    if (lastCentered != center) {
                        mapView.controller.animateTo(center)
                        mapView.controller.setZoom(13.0)
                        lastCentered = center
                    }
                }

                // ===============================
                // 📍 Cercle position utilisateur
                // ===============================

                userCircle?.let { mapView.overlays.remove(it) }

                userLocation?.let { location ->
                    val circle = Polygon().apply {
                        points = Polygon.pointsAsCircle(location, 200.0)
                        fillPaint.color = "#403B82F6".toColorInt()
                        fillPaint.style = Paint.Style.FILL
                        outlinePaint.color = "#803B82F6".toColorInt()
                        outlinePaint.strokeWidth = 2f
                    }
                    mapView.overlays.add(circle)
                    userCircle = circle
                }

                // ===============================
                // 🏙 Cercle ville recherchée
                // ===============================

                cityCircle?.let { mapView.overlays.remove(it) }

                filters.center?.let { center ->
                    val circle = Polygon().apply {
                        points = Polygon.pointsAsCircle(center, 3000.0)
                        fillPaint.color = primaryColor
                        outlinePaint.strokeWidth = 0f
                    }
                    mapView.overlays.add(circle)
                    cityCircle = circle
                }

                // ===============================
                // 📍 Marker recherche
                // ===============================

                searchMarker?.let { mapView.overlays.remove(it) }

                filters.center?.let { center ->
                    val marker = Marker(mapView).apply {
                        position = center
                        setAnchor(
                            Marker.ANCHOR_CENTER,
                            Marker.ANCHOR_BOTTOM
                        )
                        icon = mapView.context.getDrawable(
                            R.drawable.ic_search_location_marker
                        )
                    }

                    mapView.overlays.add(marker)
                    searchMarker = marker
                }

                // ===============================
                // 🚲 Markers vélos
                // ===============================

                val selectedDays = if (filters.startDate != null && filters.endDate != null) {
                    ChronoUnit.DAYS.between(filters.startDate, filters.endDate).coerceAtLeast(1)
                } else {
                    0L
                }

                if (lastBikes != bikes || lastSelectedDays != selectedDays) {

                    // Supprimer anciens markers
                    bikeMarkers.forEach {
                        mapView.overlays.remove(it)
                    }

                    val newMarkers = bikes.map { bike ->

                        Marker(mapView).apply {

                            if (bike.location.latitude != null && bike.location.longitude != null) {
                                position = GeoPoint(bike.location.latitude, bike.location.longitude)
                            }

                            val isBelowMinDays =
                                selectedDays > 0 && selectedDays < bike.minDaysRental
                            val iconDrawable = mapView.context.getDrawable(
                                R.drawable.ic_bike_marker_light
                            )?.mutate()
                            if (isBelowMinDays) {
                                iconDrawable?.colorFilter = PorterDuffColorFilter(
                                    "#808080".toColorInt(),
                                    PorterDuff.Mode.MULTIPLY
                                )
                            }

                            icon = iconDrawable


                            setAnchor(
                                Marker.ANCHOR_CENTER,
                                Marker.ANCHOR_BOTTOM
                            )

                            setOnMarkerClickListener { _, _ ->
                                onBikeClicked(bike)
                                true
                            }
                        }
                    }

                    newMarkers.forEach {
                        mapView.overlays.add(it)
                    }

                    bikeMarkers = newMarkers
                    lastBikes = bikes
                    lastSelectedDays = selectedDays

                    mapView.invalidate()
                }
            }
        )

        Text(
            text = "© OpenStreetMap contributors",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 16.dp, start = 8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.3f))
        )
    }
}