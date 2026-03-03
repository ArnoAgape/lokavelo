package com.arnoagape.lokavelo.ui.screen.main.map.components

import android.graphics.Paint
import androidx.preference.PreferenceManager
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
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

@Composable
fun OSMMap(
    userLocation: GeoPoint?,
    bikes: List<Bike>,
    filters: SearchFilters,
    recenterTrigger: Boolean,
    onRecenterHandled: () -> Unit
) {
    var lastCentered by remember { mutableStateOf<GeoPoint?>(null) }
    val primaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f).toArgb()

    val tileSource = XYTileSource(
        "CartoVoyager",
        0,
        19,
        256,
        ".png",
        arrayOf(
            "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
            "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
            "https://c.basemaps.cartocdn.com/rastertiles/voyager/"
        )
    )

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->

            Configuration.getInstance().userAgentValue = context.packageName
            Configuration.getInstance().load(
                context,
                PreferenceManager.getDefaultSharedPreferences(context)
            )

            MapView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                setTileSource(tileSource)
                setMultiTouchControls(true)
                controller.setZoom(14.0)
                controller.setCenter(
                    userLocation ?: GeoPoint(43.2965, 5.3698)
                )

                val mapEventsReceiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        return true
                    }

                    override fun longPressHelper(p: GeoPoint?): Boolean = false
                }

                val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
                overlays.add(mapEventsOverlay)
            }
        },
        update = { mapView ->

            if (mapView.tileProvider.tileSource != tileSource) {
                mapView.setTileSource(tileSource)
            }

            // 🔁 Bouton recentrage
            if (recenterTrigger && userLocation != null) {

                mapView.controller.apply {
                    setZoom(16.0)
                    animateTo(userLocation)
                }

                lastCentered = userLocation
                onRecenterHandled()
            }

            // 🔥 Recentrage si nouvelle ville
            filters.center?.let { center ->
                if (lastCentered != center) {
                    mapView.controller.animateTo(center)
                    mapView.controller.setZoom(13.0)
                    lastCentered = center
                }
            }

            // Supprime uniquement markers + cercles
            mapView.overlays.removeAll {
                it is Marker || it is Polygon
            }

            // 📍 Cercle position utilisateur
            userLocation?.let { location ->
                val circle = Polygon().apply {
                    points = Polygon.pointsAsCircle(location, 200.0)

                    fillPaint.color = "#403B82F6".toColorInt()
                    fillPaint.style = Paint.Style.FILL

                    outlinePaint.color = "#803B82F6".toColorInt()
                    outlinePaint.style = Paint.Style.STROKE
                    outlinePaint.strokeWidth = 2f
                }
                mapView.overlays.add(circle)
            }

            // 🏙 Cercle ville recherchée
            filters.center?.let { center ->
                val cityCircle = Polygon().apply {
                    points = Polygon.pointsAsCircle(center, 3000.0)
                    fillPaint.color = primaryColor
                    outlinePaint.strokeWidth = 0f
                }
                mapView.overlays.add(cityCircle)
            }

            // 📍 Marker adresse recherchée
            filters.center?.let { center ->

                val searchMarker = Marker(mapView).apply {
                    position = center
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                    icon = mapView.context.getDrawable(R.drawable.ic_search_location_marker)
                }

                mapView.overlays.add(searchMarker)
            }

            // 🚲 Markers vélos
            bikes.forEach { bike ->
                val marker = Marker(mapView).apply {
                    position = GeoPoint(
                        bike.location.latitude,
                        bike.location.longitude
                    )

                    icon = mapView.context.getDrawable(R.drawable.ic_bike_marker_light)

                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }

                mapView.overlays.add(marker)
            }

            mapView.invalidate()
        }
    )
}