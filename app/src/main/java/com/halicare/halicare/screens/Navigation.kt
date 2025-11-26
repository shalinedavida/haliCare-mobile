package com.halicare.halicare.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.halicare.halicare.model.ArvAvailability
import com.halicare.halicare.viewModel.ClinicDetailViewModel
import com.halicare.halicare.viewModel.ClinicViewModel
import com.halicare.halicare.viewModel.NavigationViewModel
import org.koin.androidx.compose.koinViewModel

sealed class Screen(val route: String) {
    object Launch : Screen("launch")
    object Splash : Screen("splashScreen")
    object TeaserOne : Screen("teaser1")
    object TeaserTwo : Screen("teaser2")
    object TeaserThree : Screen("teaser3")
    object TeaserFour : Screen("teaser4")
    object SignUp : Screen("signup")
    object SignIn : Screen("signin")
    object LocationPermission : Screen("locationPermission")
    object NewPatient : Screen("newPatient")
    object FindClinics : Screen("find_clinics")
    object Clinic : Screen("clinic/{center_id}") {
        fun createRoute(centerId: String) = "clinic/$centerId"
    }
    object LetterUpload : Screen("letter_upload/{center_id}") {
        fun createRoute(centerId: String) = "letter_upload/$centerId"
    }
    object BookAppointment : Screen("book_appointment/{center_id}") {
        fun createRoute(centerId: String) = "book_appointment/$centerId"
    }
    object AppointmentConfirmed : Screen("appointment_confirmed")
    object Appointment : Screen("appointment")
}

@Composable
fun SplashScreen(
    onNavigate: (Boolean) -> Unit,
    viewModel: NavigationViewModel = koinViewModel()
) {
    val completed = viewModel.determineStartDestination()
    onNavigate(completed)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(startDestination: String) {
    val navController = rememberNavController()

    val clinicViewModel: ClinicViewModel = koinViewModel()
    val arvAvailabilities by clinicViewModel.arvAvailabilities.collectAsState()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Launch.route) {
            LaunchScreen(navController)
        }

        composable(Screen.TeaserOne.route) {
            TeaserScreenOne(
                onBack = { },
                onNext = { navController.navigate(Screen.TeaserTwo.route) },
                onSkip = { navController.navigate(Screen.SignUp.route) }
            )
        }

        composable(Screen.TeaserTwo.route) {
            TeaserScreenTwo(
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(Screen.TeaserThree.route) },
                onSkip = { navController.navigate(Screen.SignUp.route) }
            )
        }

        composable(Screen.TeaserThree.route) {
            TeaserScreenThree(
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(Screen.TeaserFour.route) },
                onSkip = { navController.navigate(Screen.SignUp.route) }
            )
        }

        composable(Screen.TeaserFour.route) {
            TeaserScreenFour(
                onBack = { navController.popBackStack() },
                onGetStarted = { navController.navigate(Screen.SignUp.route) },
                onSkip = { navController.navigate(Screen.SignUp.route) }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpClick = { navController.navigate(Screen.SignIn.route) },
                onSignInClick = { navController.navigate(Screen.SignIn.route) }
            )
        }

        composable(Screen.SignIn.route) {
            SignInScreen(
                onSignUpClick = { navController.navigate(Screen.SignUp.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigate = { completedLocationSetup ->
                    if (completedLocationSetup) {
                        navController.navigate(Screen.NewPatient.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.LocationPermission.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.LocationPermission.route) {
            LocationPermissionScreen(
                onLocationReceived = {
                    navController.navigate(Screen.NewPatient.route) {
                        popUpTo(Screen.LocationPermission.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.NewPatient.route) {
            MainScreen(
                rootNavController = navController,
                clinicViewModel = clinicViewModel,
                arvAvailabilities = arvAvailabilities
            )
        }

        composable(Screen.FindClinics.route) {
            FindClinicsScreen(
                clinicViewModel = clinicViewModel,
                arvAvailabilities = arvAvailabilities,
                onBack = { navController.popBackStack() },
                onBookAppointment = { centerId ->
                    navController.navigate(Screen.LetterUpload.createRoute(centerId))
                }
            )
        }

        navigation(
            startDestination = Screen.Clinic.route,
            route = "clinic_flow"
        ) {
            composable(
                route = Screen.Clinic.route,
                arguments = listOf(navArgument("center_id") { type = NavType.StringType })
            ) { backStackEntry ->
                val centerId = backStackEntry.arguments?.getString("center_id") ?: return@composable

                val viewModel: ClinicDetailViewModel = koinViewModel(key = "clinic_vm_$centerId")

                LaunchedEffect(centerId) {
                    viewModel.checkIfUserIsNew(centerId)
                    viewModel.loadClinicDetails(centerId)
                }

                val isUserNewToClinic by viewModel.isUserNewToClinic.collectAsState(initial = true)

                ClinicScreen(
                    centerId = centerId,
                    arvAvailabilities = arvAvailabilities,
                    onNext = {
                        if (isUserNewToClinic == true) {
                            navController.navigate(Screen.LetterUpload.createRoute(centerId))
                        } else {
                            navController.navigate(Screen.BookAppointment.createRoute(centerId))
                        }
                    },
                    onBack = { navController.popBackStack() },
                    isNewUser = isUserNewToClinic == true
                )
            }

            composable(
                route = Screen.LetterUpload.route,
                arguments = listOf(navArgument("center_id") { type = NavType.StringType })
            ) { backStackEntry ->
                val centerId = backStackEntry.arguments?.getString("center_id") ?: return@composable

                val viewModel: ClinicDetailViewModel = koinViewModel(key = "clinic_vm_$centerId")

                LaunchedEffect(centerId) {
                    viewModel.checkIfUserIsNew(centerId)
                    if (viewModel.clinic.value == null) viewModel.loadClinicDetails(centerId)
                }

                val isUserNewToClinic by viewModel.isUserNewToClinic.collectAsState(initial = true)

                LetterUploadScreen(
                    centerId = centerId,
                    isUserNewToClinic = isUserNewToClinic == true,
                    onNext = { navController.navigate(Screen.BookAppointment.createRoute(centerId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.BookAppointment.route,
                arguments = listOf(navArgument("center_id") { type = NavType.StringType })
            ) { backStackEntry ->
                val centerId = backStackEntry.arguments?.getString("center_id") ?: return@composable

                BookAppointmentScreen(
                    centerId = centerId,
                    arvAvailabilities = arvAvailabilities,
                    onNext = { navController.navigate(Screen.Appointment.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Appointment.route) {
                AppointmentScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}