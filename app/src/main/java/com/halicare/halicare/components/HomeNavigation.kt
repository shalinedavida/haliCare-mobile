package com.halicare.halicare.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.android.gms.maps.model.LatLng
import com.halicare.halicare.screen.FindCounselingCenterScreen
import com.halicare.halicare.screens.AppointmentScreen
import com.halicare.halicare.screens.NewPatientScreen
import com.halicare.halicare.screens.ClinicRequirementsScreen
import com.halicare.halicare.screens.FindClinicsScreen
import com.halicare.halicare.viewModel.ClinicViewModel
import com.halicare.halicare.viewModel.CounselingCenterViewModel
import org.koin.androidx.compose.koinViewModel

sealed class BottomNavItem(val route: String, val icon: Int, val label: String) {
    object Home : BottomNavItem("home", com.halicare.halicare.R.drawable.ic_home_filled, "Home")
    object Clinics : BottomNavItem("clinics", com.halicare.halicare.R.drawable.ic_clinics_filled, "Clinics")
    object Appointments : BottomNavItem("appointments", com.halicare.halicare.R.drawable.ic_appointments_filled, "Appointments")
    object Counselling : BottomNavItem("counselling", com.halicare.halicare.R.drawable.ic_counselling_filled, "Counselling")
    object ClinicRequirements : BottomNavItem("clinic_requirements", 0, "Clinic Requirements")
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Clinics,
    BottomNavItem.Appointments,
    BottomNavItem.Counselling
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeNavigation(
    rootNavController: NavHostController,
    navController: NavHostController,
    clinicViewModel: ClinicViewModel,
    arvAvailabilities: List<com.halicare.halicare.model.ArvAvailability>,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.Home.route) {
            NewPatientScreen(
                onNavigateToClinicRequirements = { navController.navigate(BottomNavItem.ClinicRequirements.route) }
            )
        }
        composable(BottomNavItem.Clinics.route) {
            FindClinicsScreen(
                clinicViewModel = clinicViewModel,
                arvAvailabilities = arvAvailabilities,
                onBookAppointment = { centerId ->
                    rootNavController.navigate(com.halicare.halicare.screens.Screen.LetterUpload.createRoute(centerId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(BottomNavItem.Appointments.route) { AppointmentScreen(onBack = { navController.popBackStack() }) }
        composable(BottomNavItem.Counselling.route) {
            val counselingViewModel: CounselingCenterViewModel = koinViewModel()
            val userLocation = LatLng(-1.2921, 36.8219)
            FindCounselingCenterScreen(
                viewModel = counselingViewModel,
                userLocation = userLocation,
                onBack = { navController.popBackStack() }
            )
        }

        composable(BottomNavItem.ClinicRequirements.route) { ClinicRequirementsScreen(navController) }
    }
}