package com.halicare.halicare
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
private const val PREFS_NAME = "location_prefs"
private const val KEY_SETUP_COMPLETE = "location_setup_complete"
private const val KEY_LATITUDE = "last_known_latitude"
private const val KEY_LONGITUDE = "last_known_longitude"
fun setLocationSetupComplete(context: Context, isComplete: Boolean) {
   val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
   with(sharedPreferences.edit()) {
       putBoolean(KEY_SETUP_COMPLETE, isComplete)
       apply()
   }
}
fun hasCompletedLocationSetup(context: Context): Boolean {
   val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
   val hasPermission = ContextCompat.checkSelfPermission(
       context,
       Manifest.permission.ACCESS_FINE_LOCATION
   ) == PackageManager.PERMISSION_GRANTED ||
   ContextCompat.checkSelfPermission(
       context,
       Manifest.permission.ACCESS_COARSE_LOCATION
   ) == PackageManager.PERMISSION_GRANTED
   val hasCompletedSetup = sharedPreferences.getBoolean(KEY_SETUP_COMPLETE, false)
   return hasPermission && hasCompletedSetup
}
fun saveLastKnownLocation(context: Context, location: Location) {
   val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
   with(sharedPreferences.edit()) {
       putFloat(KEY_LATITUDE, location.latitude.toFloat())
       putFloat(KEY_LONGITUDE, location.longitude.toFloat())
       apply()
   }
}
fun getLastKnownLocation(context: Context): Pair<Double, Double>? {
   val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
   val latitude = sharedPreferences.getFloat(KEY_LATITUDE, 0f)
   val longitude = sharedPreferences.getFloat(KEY_LONGITUDE, 0f)
   return if (latitude != 0f || longitude != 0f) {
       Pair(latitude.toDouble(), longitude.toDouble())
   } else {
       null
   }
}






