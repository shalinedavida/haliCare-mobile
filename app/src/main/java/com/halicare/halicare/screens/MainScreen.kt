package com.halicare.halicare.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.halicare.halicare.components.HomeBottomNavigationBar
import com.halicare.halicare.components.HomeNavigation
import com.halicare.halicare.model.ArvAvailability
import com.halicare.halicare.viewModel.ClinicViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    rootNavController: NavHostController,
    clinicViewModel: ClinicViewModel,
    arvAvailabilities: List<ArvAvailability>
) {
    val bottomNavController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            HomeBottomNavigationBar(navController = bottomNavController)
        }
    ) { innerPadding ->
        HomeNavigation(
            rootNavController = rootNavController,
            navController = bottomNavController,
            clinicViewModel = clinicViewModel,
            arvAvailabilities = arvAvailabilities,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        )
    }
}