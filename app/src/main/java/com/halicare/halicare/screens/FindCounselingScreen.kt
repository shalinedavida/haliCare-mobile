package com.halicare.halicare.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.halicare.halicare.R
import com.halicare.halicare.model.CounselingCenterDetails
import com.halicare.halicare.viewModel.CounselingCenterViewModel
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindCounselingCenterScreen(
    viewModel: CounselingCenterViewModel,
    userLocation: LatLng,
    onBack: () -> Unit = {}
) {
    val darkBlue = Color(0xFF001B67)
    val lightBlue = Color(0xFFD1E7F9)

    val centers by viewModel.centers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isMapView by remember { mutableStateOf(true) }
    var selectedCenterForBottomSheet by remember { mutableStateOf<CounselingCenterDetails?>(null) }
    var selectedCenterForFullDetails by remember { mutableStateOf<CounselingCenterDetails?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadCenters()
    }

    val filteredCenters = remember(centers, searchQuery) {
        if (searchQuery.isBlank()) centers
        else centers.filter {
            it.center_name.contains(searchQuery, ignoreCase = true) ||
                    it.address.contains(searchQuery, ignoreCase = true)
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation, 12f)
    }

    val context = LocalContext.current
    val markerIcon = remember { getMarkerIconFromDrawable(context, R.drawable.ic_clinics_filled) }

    LaunchedEffect(selectedCenterForBottomSheet) {
        if (selectedCenterForBottomSheet != null) {
            scope.launch { sheetState.show() }
        }
    }

    fun dismissBottomSheet() {
        scope.launch {
            sheetState.hide()
            selectedCenterForBottomSheet = null
        }
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Find Counseling Centers",
                            fontWeight = FontWeight.Bold,
                            color = darkBlue,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 40.dp)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = darkBlue)
                        }
                    },
                    colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color.White)
                )
            },containerColor = Color.White
        )
        { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = darkBlue)
                        }
                    }

                    errorMessage != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Error: $errorMessage",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    else -> {
                        if (isMapView) {
                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState,
                                uiSettings = MapUiSettings(
                                    myLocationButtonEnabled = false
                                ),
                                properties = MapProperties(
                                    isMyLocationEnabled = false
                                )
                            ) {
                                centers.forEach { center ->
                                    Marker(
                                        state = MarkerState(center.location),
                                        title = center.center_name,
                                        snippet = "Status: ${center.operational_status}",
                                        icon = markerIcon,
                                        onClick = {
                                            selectedCenterForBottomSheet = center
                                            true
                                        }
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 150.dp)
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.Top,
                                contentPadding = PaddingValues(bottom = 40.dp)
                            ) {
                                items(filteredCenters) { center ->
                                    CounselingCenterCard(
                                        center = center,
                                        darkBlue = darkBlue,
                                        onDetailsClick = {
                                            selectedCenterForFullDetails = center
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                SearchAndToggleUI(
                    isMapView = isMapView,
                    onToggleMap = { isMapView = true },
                    onToggleList = { isMapView = false },
                    darkBlue = darkBlue,
                    lightBlue = lightBlue,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp)
                        .zIndex(1f)
                )
            }
        }


        if (selectedCenterForBottomSheet != null) {
            ModalBottomSheet(
                onDismissRequest = { dismissBottomSheet() },
                sheetState = sheetState,
                containerColor = Color.Transparent,
                dragHandle = null
            ) {
                CounselingCenterBottomCard(
                    center = selectedCenterForBottomSheet!!,
                    darkBlue = darkBlue,
                    onDismiss = { dismissBottomSheet() }
                )
            }
        }

        if (selectedCenterForFullDetails != null) {
            AlertDialog(
                onDismissRequest = { selectedCenterForFullDetails = null },
                containerColor = Color.White,
                title = {
                    Text(
                        selectedCenterForFullDetails!!.center_name,
                        fontWeight = FontWeight.Bold,
                        color = darkBlue
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AsyncImage(
                            model = selectedCenterForFullDetails!!.imageUrl,
                            contentDescription = selectedCenterForFullDetails!!.center_name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(" Address: ${selectedCenterForFullDetails!!.address}", color = darkBlue)
                        Text(" Status: ${selectedCenterForFullDetails!!.operational_status}", color = darkBlue)
                        Text(" Contact: ${selectedCenterForFullDetails!!.contact}", color = darkBlue)
                        Text(" Opening Hours: ${selectedCenterForFullDetails!!.opening_hours}", color = darkBlue)
                        Text(" Closing Hours: ${selectedCenterForFullDetails!!.closing_hours}", color = darkBlue)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { selectedCenterForFullDetails = null },
                        colors = ButtonDefaults.buttonColors(containerColor = darkBlue)
                    ) {
                        Text("Close", color = Color.White)
                    }
                }

            )
        }
    }
}

@Composable
fun CounselingCenterCard(
    center: CounselingCenterDetails,
    darkBlue: Color,
    onDetailsClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(75.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(68.dp)
                    .background(color = darkBlue, shape = RoundedCornerShape(12.dp))
                    .shadow(4.dp, RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.width(12.dp))
            Text(
                center.center_name,
                modifier = Modifier.weight(1f),
                color = darkBlue,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            Button(
                onClick = onDetailsClick,
                modifier = Modifier.height(37.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = darkBlue,
                    contentColor = Color.White
                )
            ) {
                Text("Details")
            }
        }
    }
}

@Composable
fun CounselingCenterBottomCard(
    center: CounselingCenterDetails,
    darkBlue: Color,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(Color(0xFFD1E7F9))
            .padding(16.dp)
    ) {
        AsyncImage(
            model = center.imageUrl,
            contentDescription = center.center_name,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = center.center_name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = darkBlue
        )

        Spacer(Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = darkBlue,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = center.address,
                color = darkBlue,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Status: ${center.operational_status}",
            color = darkBlue,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = darkBlue)
        ) {
            Text("Close", color = Color.White)
        }
    }
}
@Composable
fun SearchAndToggleUI(
    isMapView: Boolean,
    onToggleMap: () -> Unit,
    onToggleList: () -> Unit,
    darkBlue: Color,
    lightBlue: Color,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(Color.Transparent)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search counseling centers...", color = Color.Gray) },
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
            textStyle = TextStyle(color = Color.Black),
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





