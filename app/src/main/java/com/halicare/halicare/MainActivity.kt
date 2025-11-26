package com.halicare.halicare

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.halicare.halicare.screens.AppNavigation
import com.halicare.halicare.screens.Screen
import com.halicare.halicare.ui.theme.HalicareTheme
import com.google.android.gms.maps.MapsInitializer
import com.google.android.libraries.places.api.Places

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiKey = getMapsApiKey(applicationContext)
        MapsInitializer.initialize(applicationContext)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }
        setContent {
            HalicareTheme {
                val prefs = getSharedPreferences("HALICARE_PREFS", Context.MODE_PRIVATE)
                val token = prefs.getString("ACCESS_TOKEN", "")
                val userId = prefs.getString("USER_ID", "")
                val hasLaunchedBefore = prefs.getBoolean("HAS_LAUNCHED_BEFORE", false)

                val startDestination = if (!hasLaunchedBefore) {
                    Screen.TeaserOne.route
                } else {
                    if (token.isNullOrBlank() || userId.isNullOrBlank()) {
                        Screen.SignIn.route
                    } else {
                        Screen.Splash.route
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(startDestination = startDestination)
                }
            }
        }
    }

    private fun getMapsApiKey(context: Context): String {
        return try {
            val appInfo = context.packageManager
                .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            appInfo.metaData.getString("com.google.android.geo.API_KEY") ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}