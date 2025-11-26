package com.halicare.halicare.screens
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.android.datatransport.BuildConfig
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.halicare.halicare.R
import com.halicare.halicare.model.ArvAvailability
import com.halicare.halicare.model.ClinicDetails
import com.halicare.halicare.model.ClinicService
import com.halicare.halicare.model.ContactInfo
import com.halicare.halicare.viewModel.ClinicDetailViewModel
import com.halicare.halicare.viewModel.ClinicViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnrememberedMutableState")
@Composable
fun FindClinicsScreen(
    clinicViewModel: ClinicViewModel,
    arvAvailabilities: List<ArvAvailability>,
    detailViewModel: ClinicDetailViewModel = koinViewModel(),
    onBack: () -> Unit = {},
    onBookAppointment: (centerId: String) -> Unit = {}
) {
    val context = LocalContext.current
    val clinics by clinicViewModel.clinics.collectAsState()
    val isLoading by clinicViewModel.isLoading.collectAsState()
    val errorMessage by clinicViewModel.errorMessage.collectAsState()
    val nairobiCenter = LatLng(-1.286389, 36.817223)
    val kenyaBounds = LatLngBounds(
        LatLng(-4.678, 33.909),
        LatLng(5.0199, 41.899)
    )

    var selectedClinic by remember { mutableStateOf<ClinicDetails?>(null) }
    var query by remember { mutableStateOf("") }
    var isMapView by remember { mutableStateOf(true) }
    var showClinicDetails by remember { mutableStateOf(false) }
    var selectedClinicId by remember { mutableStateOf<String?>(null) }
    var mapInitialized by remember { mutableStateOf(false) }

    val markerIcon = remember { getMarkerIconFromDrawable(context, R.drawable.ic_clinics_filled) }

    val filteredClinics = remember(clinics, query) {
        Log.d("FindClinicsScreen", "Filtering clinics: ${clinics.size} total")

        if (query.isEmpty()) {
            clinics
        } else {
            clinics.filter {
                it.center_name.contains(query, ignoreCase = true) ||
                        it.address.contains(query, ignoreCase = true)
            }
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(nairobiCenter, 13f)
    }

    LaunchedEffect(clinics) {
        if (clinics.isNotEmpty() && !mapInitialized) {
            mapInitialized = true
            Log.d("FindClinicsScreen", "Map initialized with ${clinics.size} clinics")

            if (clinics.isNotEmpty()) {
                val boundsBuilder = LatLngBounds.Builder()
                clinics.forEach { clinic ->
                    if (clinic.latitude != 0.0 && clinic.longitude != 0.0) {
                        boundsBuilder.include(LatLng(clinic.latitude, clinic.longitude))
                        boundsBuilder.include(LatLng(clinic.latitude, clinic.longitude))
                    }
                }
                val bounds = boundsBuilder.build()
                cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            }
        }
    }

    LaunchedEffect(Unit) {
        Log.d("FindClinicsScreen", "Starting to load data")
        clinicViewModel.fetchNearbyClinics(nairobiCenter.latitude, nairobiCenter.longitude)
    }

    LaunchedEffect(clinics) {
        Log.d("FindClinicsScreen", "Total clinics loaded: ${clinics.size}")
        clinics.forEach { clinic ->
            Log.d("FindClinicsScreen", "Clinic: ${clinic.center_name} | Lat: ${clinic.latitude}, Lng: ${clinic.longitude}")
        }
    }

    LaunchedEffect(arvAvailabilities) {
        Log.d("FindClinicsScreen", "ARV availabilities loaded: ${arvAvailabilities.size}")
        arvAvailabilities.forEach { availability ->
            Log.d("FindClinicsScreen", "ARV at ${availability.centerId}: ${availability.arvAvailability}")
        }
    }

    LaunchedEffect(filteredClinics, query) {
        if (query.isNotEmpty() && filteredClinics.size == 1) {
            selectedClinic = filteredClinics.first()
            if (filteredClinics.first().latitude != 0.0 && filteredClinics.first().longitude != 0.0) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(filteredClinics.first().latitude, filteredClinics.first().longitude),
                        15f
                    )
                )
            }
        } else if (query.isEmpty()) {
            selectedClinic = null
            if (clinics.isNotEmpty()) {
                val boundsBuilder = LatLngBounds.Builder()
                clinics.forEach { clinic ->
                    if (clinic.latitude != 0.0 && clinic.longitude != 0.0) {
                        boundsBuilder.include(LatLng(clinic.latitude, clinic.longitude))
                    }
                }
                val bounds = boundsBuilder.build()
                cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            }
        }
    }

    LaunchedEffect(selectedClinic) {
        selectedClinic?.let { clinic ->
            if (filteredClinics.contains(clinic) && clinic.latitude != 0.0 && clinic.longitude != 0.0) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(clinic.latitude, clinic.longitude),
                        15f
                    )
                )
            }
        }
    }

    LaunchedEffect(showClinicDetails, selectedClinicId) {
        if (showClinicDetails && selectedClinicId != null) {
            detailViewModel.loadClinicDetails(selectedClinicId!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Find Clinics",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF001B67),
                        fontSize = 20.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 60.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF001B67))
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            SearchAndToggleUIClinics(
                isMapView = isMapView,
                onToggleMap = { isMapView = true },
                onToggleList = { isMapView = false },
                darkBlue = Color(0xFF001B67),
                lightBlue = Color(0xFFD1E7F9),
                searchQuery = query,
                onSearchQueryChange = { query = it }
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF001B67))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Loading ARV clinic data...",
                            color = Color(0xFF001B67)
                        )
                    }
                }
            } else if (errorMessage?.isNotBlank() == true) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = errorMessage ?: "Unknown error", color = Color.Red)
                }
            } else {
                if (isMapView) {
                    Box(Modifier.weight(1f)) {
                        if (mapInitialized) {
                            GoogleMap(
                                modifier = Modifier.fillMaxSize().testTag("clinicMap"),
                                cameraPositionState = cameraPositionState,
                                uiSettings = MapUiSettings(
                                    zoomControlsEnabled = true,
                                    myLocationButtonEnabled = false
                                ),
                                properties = MapProperties(
                                    isMyLocationEnabled = false,
                                    latLngBoundsForCameraTarget = kenyaBounds
                                ),
                                onMapLoaded = {
                                    Log.d("FindClinicsScreen", "Map fully loaded")
                                }
                            ) {
                                filteredClinics.forEach { clinic ->
                                    if (clinic.latitude != 0.0 && clinic.longitude != 0.0) {
                                        Marker(
                                            state = MarkerState(position = LatLng(clinic.latitude, clinic.longitude)),
                                            title = clinic.center_name,
                                            snippet = "ARV Available",
                                            onClick = {
                                                selectedClinic = clinic
                                                true
                                            },
                                            icon = markerIcon
                                        )
                                    }
                                }
                            }
                        }

                        if (!mapInitialized) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = Color(0xFF001B67))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = " ",
                                        color = Color(0xFF001B67)
                                    )
                                }
                            }
                        }

                        if (BuildConfig.DEBUG) {
                            Card(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(16.dp)
                                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = "Debug Info:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Clinics: ${filteredClinics.size}",
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "ARV Data: ${arvAvailabilities.size}",
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Map Initialized: $mapInitialized",
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        if (query.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(16.dp, top = if (BuildConfig.DEBUG) 120.dp else 16.dp)
                                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF001B67)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${filteredClinics.size} clinic${if (filteredClinics.size != 1) "s" else ""} found",
                                    modifier = Modifier.padding(16.dp, vertical = 12.dp),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                        selectedClinic?.let { clinic ->
                            ClinicBottomSheet(
                                clinic = clinic,
                                onDismiss = { selectedClinic = null },
                                onViewDetails = {
                                    selectedClinicId = clinic.center_id
                                    showClinicDetails = true
                                }
                            )
                        }
                    }
                } else {
                    if (filteredClinics.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (query.isEmpty()) "No ARV clinics available" else "No ARV clinics found matching \"$query\"",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            items(filteredClinics, key = { it.center_id }) { clinic ->
                                ClinicListItem(
                                    clinic = clinic,
                                    onViewDetails = {
                                        selectedClinicId = clinic.center_id
                                        showClinicDetails = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        if (showClinicDetails && selectedClinicId != null) {
            ClinicDetailsModalBottomSheet(
                centerId = selectedClinicId!!,
                detailViewModel = detailViewModel,
                arvAvailabilities = arvAvailabilities,
                onDismiss = {
                    showClinicDetails = false
                    selectedClinicId = null
                },
                onBookAppointment = { centerId ->
                    showClinicDetails = false
                    selectedClinicId = null
                    onBookAppointment(centerId)
                }
            )
        }
    }
}
fun getMarkerIconFromDrawable(context: Context, @DrawableRes drawableId: Int): BitmapDescriptor {
    val drawable = ContextCompat.getDrawable(context, drawableId)
        ?: return BitmapDescriptorFactory.defaultMarker()
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@Composable
fun SearchAndToggleUIClinics(
    isMapView: Boolean,
    onToggleMap: () -> Unit,
    onToggleList: () -> Unit,
    darkBlue: Color,
    lightBlue: Color,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.Transparent)
            .padding(horizontal = 12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search for clinics...", color = Color.Gray) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
            },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = darkBlue,
                unfocusedBorderColor = darkBlue,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = Color.Black
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .background(color = lightBlue, shape = RoundedCornerShape(24.dp))
                    .padding(vertical = 4.dp, horizontal = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onToggleMap,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMapView) darkBlue else Color.Transparent,
                            contentColor = if (isMapView) Color.White else darkBlue
                        ),
                        border = if (!isMapView) BorderStroke(1.dp, darkBlue) else null,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Map view")
                    }
                    Button(
                        onClick = onToggleList,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isMapView) darkBlue else Color.Transparent,
                            contentColor = if (!isMapView) Color.White else darkBlue
                        ),
                        border = if (isMapView) BorderStroke(1.dp, darkBlue) else null,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("List view")
                    }
                }
            }
        }
    }
}

@Composable
private fun ClinicBottomSheet(
    clinic: ClinicDetails,
    onDismiss: () -> Unit,
    onViewDetails: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(390.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(Color(0xFFD1E7F9))
                .shadow(8.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFD1E7F9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    AsyncImage(
                        model = clinic.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            clinic.center_name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF001B67),
                            modifier = Modifier.weight(1f)
                        )
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "ARV Available",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Color(0xFF001B67),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = clinic.address,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF001B67)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    val timeParts = clinic.hours.split(" - ")
                    Text(
                        text = if (timeParts.size == 2) "Opens: ${timeParts[0]} | Closes: ${timeParts[1]}" else "Hours: ${clinic.hours.ifBlank { "N/A" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onViewDetails,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001B67))
                    ) {
                        Text("View Details", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF001B67),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ClinicListItem(clinic: ClinicDetails, onViewDetails: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = clinic.center_name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF001B67),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "ARV",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = Color(0xFF001B67), modifier = Modifier.size(16.dp))
                Text(text = clinic.address, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 6.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            val timeParts = clinic.hours.split(" - ")
            Text(
                text = if (timeParts.size == 2) "Opens: ${timeParts[0]} | Closes: ${timeParts[1]}" else "Hours: ${clinic.hours.ifBlank { "N/A" }}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onViewDetails,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001B67)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("View Details", color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicDetailsModalBottomSheet(
    centerId: String,
    detailViewModel: ClinicDetailViewModel,
    arvAvailabilities: List<ArvAvailability>,
    onDismiss: () -> Unit,
    onBookAppointment: (String) -> Unit
) {
    val clinic by detailViewModel.clinic.collectAsState()
    val arvService by detailViewModel.arvService.collectAsState()
    val isLoading by detailViewModel.isLoading.collectAsState()
    val listState = rememberLazyListState()
    val bottomPadding = 80.dp

    val arvAvailability = remember(centerId, arvAvailabilities) {
        arvAvailabilities.find { it.centerId == centerId }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp, 4.dp)
                        .background(Color.Gray, RoundedCornerShape(2.dp))
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        },
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        ),
        containerColor = Color.White
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (clinic == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Clinic details not available")
                }
            } else {
                val safeClinic = clinic!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = bottomPadding),
                    state = listState,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = safeClinic.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF001B67),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = onDismiss
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color(0xFF001B67)
                                )
                            }
                        }

                        if (arvAvailability != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ARV Service Status:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF001B67),
                                    fontWeight = FontWeight.Bold
                                )
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (arvAvailability!!.isAvailable) Color(0xFF4CAF50) else Color.Red
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = if (arvAvailability!!.isAvailable) "Available" else "Not Available",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = Color(0xFF001B67),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = safeClinic.address ?: "No address available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Hours: ${safeClinic.hours ?: "Not specified"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Services Available",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF001B67),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    arvService?.let { arv ->
                        item {
                            ServiceCard(service = arv, isArvService = true)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    safeClinic.services?.let { services ->
                        val otherServices = if (arvService != null) {
                            services.filter { it.serviceId != arvService!!.serviceId }
                        } else {
                            services
                        }

                        items(otherServices.size) { index ->
                            ServiceCard(service = otherServices[index], isArvService = false)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    } ?: item {
                        Text(
                            text = "No services information available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Contact Information",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF001B67),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    item {
                        safeClinic.contact?.let { contact ->
                            ContactCard(contact)
                        } ?: run {
                            Text(
                                text = "No contact information available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = { onBookAppointment(centerId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (arvAvailability?.isAvailable == true) Color(0xFF001B67) else Color.Gray
                        ),
                        enabled = arvAvailability?.isAvailable == true
                    ) {
                        Text(
                            text = if (arvAvailability?.isAvailable == true) "Book ARV Appointment" else "ARV Not Available",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceCard(service: ClinicService, isArvService: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isArvService) 4.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isArvService) Color(0xFFE8F5E9) else Color.White
        ),
        border = if (isArvService) BorderStroke(1.dp, Color(0xFF4CAF50)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isArvService) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "ARV",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = service.title,
                        color = Color(0xFF001B67),
                        fontWeight = if (isArvService) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
                service.hours?.let {
                    Text(
                        text = it,
                        color = Color(0xFF001B67),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
            service.description?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = it,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ContactCard(contact: ContactInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Phone",
            color = Color(0xFF001B67),
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )
        Text(
            text = contact.phone,
            color = Color(0xFF001B67),
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )
    }
}

private fun String?.ifBlank(default: String): String {
    return this?.takeIf { it.isNotBlank() } ?: default
}