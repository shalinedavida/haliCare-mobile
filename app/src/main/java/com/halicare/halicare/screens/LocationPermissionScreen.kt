package com.halicare.halicare.screens


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import com.halicare.halicare.R
import com.halicare.halicare.saveLastKnownLocation
import com.halicare.halicare.setLocationSetupComplete
import com.halicare.halicare.ui.theme.HalicareTheme
import com.halicare.halicare.viewModel.LocationUIState
import com.halicare.halicare.viewModel.LocationViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPermissionScreen(
    onLocationReceived: () -> Unit
) {
    val context = LocalContext.current


    val viewModel: LocationViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val resolutionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.requestLocation()
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.requestLocation()
        }
    }
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF001F58)
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "HaliCare",
                            color = Color.White,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValues)
                    .padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(80.dp))
                if (uiState.isLoading) {
                    Spacer(modifier = Modifier.weight(1f))
                    CircularProgressIndicator(color = Color(0xFF001F58))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Checking location settings…",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.weight(1f))
                } else if (uiState.isResolvable) {
                    LaunchedEffect(uiState.resolvableException) {
                        val exception = uiState.resolvableException
                        if (exception != null) {
                            val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                            resolutionLauncher.launch(intentSenderRequest)
                        }
                    }
                } else if (uiState.error != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        "Unable to fetch your location. Please enable it in settings.",
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            settingsLauncher.launch(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(vertical = 16.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001F58))
                    ) {
                        Text(
                            "Open Settings",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                } else if (uiState.location != null) {
                    LaunchedEffect(Unit) {
                        val location = uiState.location
                        location?.let {
                            saveLastKnownLocation(context, it)
                            setLocationSetupComplete(context, true)
                            onLocationReceived()
                        }
                    }
                } else {
                    Text(
                        text = "What is your location?",
                        fontSize = 24.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_location_pin),
                        contentDescription = "Location Pin",
                        modifier = Modifier.size(200.dp)
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    Button(
                        onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(74.dp)
                            .padding(vertical = 10.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001F58))
                    ) {
                        Text(
                            text = "Allow Location",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    )
}



