package com.arnoagape.lokavelo.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.arnoagape.lokavelo.data.service.geolocation.LocationApi
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) : LocationApi {

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    override suspend fun getLastLocation(): Location? =
        suspendCancellableCoroutine { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(location)
                    } else {
                        // lastLocation est null → on demande une position fraîche
                        requestFreshLocation(continuation)
                    }
                }
                .addOnFailureListener { continuation.resume(null) }
        }

    @SuppressLint("MissingPermission")
    private fun requestFreshLocation(continuation: CancellableContinuation<Location?>) {
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .setDurationMillis(5_000L)  // timeout 5 secondes
            .build()

        fusedLocationClient.getCurrentLocation(request, null)
            .addOnSuccessListener { location -> continuation.resume(location) }
            .addOnFailureListener { continuation.resume(null) }
    }
}