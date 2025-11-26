package com.halicare.halicare.data
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.coroutines.tasks.await
class LocationRepository(private val context: Context) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    suspend fun checkLocationSettings(): LocationSettingsResult {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 2000L
        ).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(request)
        val client = LocationServices.getSettingsClient(context)
        return try {
            client.checkLocationSettings(builder.build()).await()
            LocationSettingsResult.Success
        } catch (e: Exception) {
            if (e is ResolvableApiException) {
                LocationSettingsResult.Resolvable(e)
            } else {
                LocationSettingsResult.Failure
            }
        }
    }
    @SuppressLint("MissingPermission")
    suspend fun fetchLocation(): Location? {
        return try {
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                ?: fusedClient.lastLocation.await()
        } catch (_: Exception) {
            null
        }
    }
}
sealed class LocationSettingsResult {
    object Success : LocationSettingsResult()
    data class Resolvable(val exception: ResolvableApiException) : LocationSettingsResult()
    object Failure : LocationSettingsResult()
}

