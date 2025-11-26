package com.halicare.halicare.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halicare.halicare.model.ArvAvailability
import com.halicare.halicare.model.ClinicService
import com.halicare.halicare.model.ContactInfo
import com.halicare.halicare.viewModel.ClinicDetailViewModel
import org.koin.androidx.compose.koinViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ClinicScreen(
    centerId: String,
    arvAvailabilities: List<ArvAvailability>,
    onNext: () -> Unit,
    onBack: () -> Unit,
    isNewUser: Boolean
) {
    val viewModel: ClinicDetailViewModel = koinViewModel(key = "clinic_vm_$centerId")
    val clinic by viewModel.clinic.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val arvAvailability = remember(centerId, arvAvailabilities) {
        arvAvailabilities.find { it.centerId == centerId }
    }

    LaunchedEffect(centerId) {
        viewModel.checkIfUserIsNew(centerId)
        viewModel.loadClinicDetails(centerId)
    }

    var hasNavigatedBack by remember { mutableStateOf(false) }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        !isLoading && clinic == null -> {
            LaunchedEffect(Unit) {
                if (!hasNavigatedBack) {
                    Log.w("ClinicScreen", "Clinic data is null after loading. Navigating back.")
                    onBack()
                    hasNavigatedBack = true
                }
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Clinic data not available. Returning...")
            }
        }
        else -> {
            val safeClinic = clinic!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF001B67))
                        .padding(vertical = 32.dp, horizontal = 24.dp)
                ) {
                    Column {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = safeClinic.name,
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        safeClinic.location?.let { Text(text = it, color = Color.White, fontSize = 18.sp) }
                        safeClinic.address?.let { Text(text = it, color = Color.White, fontSize = 18.sp) }
                        safeClinic.hours?.let {
                            Text(
                                text = it,
                                color = Color.White,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 18.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "Services Available",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF001B67),
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    safeClinic.services?.let { services ->
                        items(services) { service ->
                            ServiceCard(service)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Contact Information",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF001B67),
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    safeClinic.contact?.let { contact ->
                        item {
                            ContactCard(contact)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        Button(
                            onClick = {
                                onNext()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (arvAvailability?.isAvailable == true) Color(0xFF001B67) else Color.Gray
                            ),
                            enabled = arvAvailability?.isAvailable == true
                        ) {
                            Text("Book", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                        }
                        if (arvAvailability?.isAvailable != true) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (arvAvailability == null) "Checking availability..." else "ARV service is currently unavailable",
                                color = Color.Red,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceCard(service: ClinicService) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = service.title,
                color = Color(0xFF001B67),
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
            service.hours?.let {
                Text(
                    text = it,
                    color = Color(0xFF001B67),
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
        }
        service.description?.takeIf { it.isNotBlank() }?.let {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = it,
                fontSize = 14.sp,
                color = Color(0xFF001B67),
                lineHeight = 18.sp
            )
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