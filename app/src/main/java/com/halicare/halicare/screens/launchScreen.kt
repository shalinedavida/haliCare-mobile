package com.halicare.halicare.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.halicare.halicare.R
import kotlinx.coroutines.delay
import android.content.Context

@Composable
fun LaunchScreen(navController: NavHostController) {
    val backgroundColor = Color(0xFF001E6B)
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        delay(4000)
        val prefs = context.getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE)
        val token = prefs.getString("ACCESS_TOKEN", "") ?: ""

        if (token.isNotBlank()) {
            navController.navigate(Screen.NewPatient.route) {
                popUpTo(Screen.Launch.route) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.TeaserOne.route) {
                popUpTo(Screen.Launch.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.halicarelogo),
            contentDescription = "HaliCare Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(150.dp)
        )
    }
}
