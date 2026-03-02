package com.arnoagape.lokavelo.ui.screen.main.home

import android.graphics.Paint
import androidx.preference.PreferenceManager
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.arnoagape.lokavelo.domain.model.Bike
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import androidx.core.graphics.toColorInt
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay

@Composable
fun OSMMap(
    userLocation: GeoPoint?,
    bikes: List<Bike>
) {
    var hasCentered by remember { mutableStateOf(false) }

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

                setTileSource(TileSourceFactory.MAPNIK)
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

            if (!hasCentered && userLocation != null) {
                mapView.controller.animateTo(userLocation)
                mapView.controller.zoomTo(15.0, 600L)
                hasCentered = true
            }

            mapView.overlays.removeAll {
                it is Marker || it is Polygon
            }

            // 📍 Position utilisateur
            userLocation?.let { location ->
                val circle = Polygon().apply {
                    points = Polygon.pointsAsCircle(location, 500.0)

                    fillPaint.color = "#403B82F6".toColorInt() // bleu transparent
                    fillPaint.style = Paint.Style.FILL

                    outlinePaint.color = "#803B82F6".toColorInt()
                    outlinePaint.style = Paint.Style.STROKE
                    outlinePaint.strokeWidth = 2f
                }

                mapView.overlays.add(circle)
            }

            // 🚲 Vélos
            bikes.forEach { bike ->
                val marker = Marker(mapView)
                marker.position = GeoPoint(
                    bike.location.latitude,
                    bike.location.longitude
                )
                marker.title = bike.title
                mapView.overlays.add(marker)
            }

            mapView.invalidate()
        }
    )
}