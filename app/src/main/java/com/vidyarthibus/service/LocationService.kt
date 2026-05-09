package com.vidyarthibus.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.vidyarthibus.data.model.BusRoute
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first

class LocationService(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /** Emits a single location fix */
    private fun getCurrentLocation(): Flow<Location?> = callbackFlow {
        if (!hasLocationPermission()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000L
        ).setMaxUpdates(1).build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                trySend(result.lastLocation)
                close()
            }
        }

        fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        awaitClose { fusedClient.removeLocationUpdates(callback) }
    }

    /**
     * FR-05: Returns true if user is within [route.proximityRadius] metres
     * of the route's centre point.
     *
     * Production note: For higher accuracy, compute distance to the nearest
     * point on the route polyline instead of a single centre point.
     */
    suspend fun isNearRoute(route: BusRoute): Boolean {
        val location = getCurrentLocation().first() ?: return false
        val routeLocation = Location("route").apply {
            latitude = route.centerLat
            longitude = route.centerLng
        }
        val distanceMetres = location.distanceTo(routeLocation)
        return distanceMetres <= route.proximityRadius
    }

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}