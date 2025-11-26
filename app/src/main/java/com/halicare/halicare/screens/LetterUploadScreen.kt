package com.halicare.halicare.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halicare.halicare.viewModel.ClinicDetailViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun LetterUploadScreen(
    centerId: String,
    isUserNewToClinic: Boolean,
    onNext: () -> Unit,
    onBack: () -> Unit = {}
) {
    val viewModel: ClinicDetailViewModel = koinViewModel(key = "clinic_vm_$centerId")
    val context = LocalContext.current

    LaunchedEffect(centerId) {
        if (viewModel.clinic.value == null) {
            viewModel.loadClinicDetails(centerId)
        }
    }

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
        uploadError = null
        uri?.let {
            val prefs = context.getSharedPreferences("HALICARE_PREFS", Context.MODE_PRIVATE)
            prefs.edit().putString("TRANSFER_LETTER_URI", it.toString()).apply()
        }
    }

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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Letter Upload",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isUserNewToClinic) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "To proceed please upload the\ntransfer letter",
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(Color(0xFFECECEC), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color(0xFF001B67), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("\u2191", color = Color(0xFF001B67), fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Select and upload the files of choice",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { filePickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder,
                    ) {
                        Text(
                            text = "Choose Files",
                            color = Color(0xFF001B67),
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                    if (selectedFileUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "File Selected",
                            color = Color(0xFF007700),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Welcome back! You can proceed to book your appointment.",
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 24.dp),
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    if (isUserNewToClinic) {
                        if (selectedFileUri == null) {
                            uploadError = "Please choose a file"
                            return@Button
                        }
                        onNext()
                    } else {
                        onNext()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001B67)),
                enabled = (isUserNewToClinic && selectedFileUri != null) || !isUserNewToClinic
            ) {
                Text("Book Appointment", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
            }
        }

        uploadError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
        }
    }
}