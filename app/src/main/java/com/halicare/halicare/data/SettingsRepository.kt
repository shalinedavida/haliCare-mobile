package com.halicare.halicare.data
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat


interface SettingsRepository {
    fun hasCompletedLocationSetup(): Boolean
}


class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {
    private val PREFS_NAME = "location_prefs"
    private val KEY_SETUP_COMPLETE = "location_setup_complete"
    override fun hasCompletedLocationSetup(): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCompletedSetup = sharedPreferences.getBoolean(KEY_SETUP_COMPLETE, false)
        return hasPermission && hasCompletedSetup
    }
}

