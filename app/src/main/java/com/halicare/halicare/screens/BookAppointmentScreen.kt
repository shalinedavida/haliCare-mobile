package com.halicare.halicare.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halicare.halicare.model.ArvAvailability
import com.halicare.halicare.viewModel.BookingStatus
import com.halicare.halicare.viewModel.ClinicDetailViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookAppointmentScreen(
    centerId: String,
    arvAvailabilities: List<ArvAvailability>,
    onNext: () -> Unit,
    onBack: () -> Unit = {}
) {
    val viewModel: ClinicDetailViewModel = koinViewModel(key = "clinic_vm_$centerId")
    val clinic by viewModel.clinic.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val bookingStatus by viewModel.bookingStatus.collectAsState()
    val arvService by viewModel.arvService.collectAsState()

    val arvAvailability = remember(centerId, arvAvailabilities) {
        arvAvailabilities.find { it.centerId == centerId }
    }

    LaunchedEffect(centerId) {
        viewModel.loadClinicDetails(centerId)
    }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(8, 0)) }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    var showBookingSuccessDialog by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    if (selectedDate < today) {
        selectedDate = today
    }

    if (selectedDate == today && selectedTime <= LocalTime.now()) {
        val nextHour = LocalTime.now().plusHours(1).withMinute(0)
        selectedTime = nextHour
    }

    Scaffold(
        topBar = {
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
                    Text(
                        text = "Book Appointment",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    )
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (clinic == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Clinic information not available",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001B67))
                    ) {
                        Text("Go Back", color = Color.White)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                if (arvAvailability == null) {
                    Text(
                        text = "ARV service information not available",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Red,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001B67))
                    ) {
                        Text("Go Back", color = Color.White)
                    }
                } else if (!arvAvailability!!.isAvailable) {
                    Text(
                        text = "ARV service is not available at this clinic",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Red,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001B67))
                    ) {
                        Text("Go Back", color = Color.White)
                    }
                } else {
                    Text(
                        text = "Select Appointment Date",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF001B67),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showDatePickerDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFF001B67))
                    ) {
                        Icon(Icons.Filled.DateRange, contentDescription = "Select Date", tint = Color(0xFF001B67))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = selectedDate.format(dateFormatter), color = Color(0xFF001B67), fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Select Appointment Time",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF001B67),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showTimePickerDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFF001B67))
                    ) {
                        Icon(Icons.Filled.Schedule, contentDescription = "Select Time", tint = Color(0xFF001B67))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = selectedTime.format(timeFormatter), color = Color(0xFF001B67), fontSize = 16.sp)
                    }

                    if (showDatePickerDialog) {
                        val datePickerState = rememberDatePickerState(
                            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                            selectableDates = object : SelectableDates {
                                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                    val date = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                                    return date >= today
                                }
                            }
                        )
                        DatePickerDialog(
                            onDismissRequest = { showDatePickerDialog = false },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            val newDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                                            if (newDate >= today) selectedDate = newDate
                                        }
                                        showDatePickerDialog = false
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF001B67))
                                ) { Text("OK") }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showDatePickerDialog = false },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF001B67))
                                ) { Text("Cancel") }
                            },
                            colors = DatePickerDefaults.colors(
                                containerColor = Color.White,
                                titleContentColor = Color(0xFF001B67),
                                headlineContentColor = Color(0xFF001B67),
                                weekdayContentColor = Color(0xFF001B67),
                                dayContentColor = Color.Black,
                                disabledDayContentColor = Color.Gray,
                                selectedDayContentColor = Color.White,
                                disabledSelectedDayContentColor = Color.LightGray,
                                selectedDayContainerColor = Color(0xFF001B67),
                                todayContentColor = Color(0xFF001B67),
                                todayDateBorderColor = Color(0xFF001B67),
                                dayInSelectionRangeContentColor = Color.White,
                                dayInSelectionRangeContainerColor = Color(0xFF001B67).copy(alpha = 0.7f)
                            )
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }

                    if (showTimePickerDialog) {
                        val timePickerState = rememberTimePickerState(
                            initialHour = selectedTime.hour,
                            initialMinute = selectedTime.minute,
                            is24Hour = false
                        )
                        AlertDialog(
                            onDismissRequest = { showTimePickerDialog = false },
                            containerColor = Color.White,
                            title = {
                                Text(
                                    "Select Time",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color(0xFF001B67)
                                )
                            },
                            text = {
                                TimePicker(
                                    state = timePickerState,
                                    modifier = Modifier.padding(16.dp),
                                    colors = TimePickerDefaults.colors(
                                        clockDialColor = Color(0xFFECECEC),
                                        clockDialSelectedContentColor = Color.White,
                                        clockDialUnselectedContentColor = Color.Black,
                                        selectorColor = Color(0xFF001B67),
                                        containerColor = Color.White,
                                        periodSelectorBorderColor = Color(0xFF001B67),
                                        periodSelectorSelectedContainerColor = Color(0xFF001B67),
                                        periodSelectorUnselectedContainerColor = Color.Transparent,
                                        periodSelectorSelectedContentColor = Color.White,
                                        periodSelectorUnselectedContentColor = Color(0xFF001B67),
                                        timeSelectorSelectedContainerColor = Color(0xFF001B67),
                                        timeSelectorUnselectedContainerColor = Color(0xFFD6E4FF),
                                        timeSelectorSelectedContentColor = Color.White,
                                        timeSelectorUnselectedContentColor = Color(0xFF001B67)
                                    )
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                                        showTimePickerDialog = false
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF001B67))
                                ) { Text("OK") }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showTimePickerDialog = false },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF001B67))
                                ) { Text("Cancel") }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Service",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF001B67),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (arvService != null) {
                        OutlinedButton(
                            onClick = { /* Optional: Add click handler if needed */ },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFF001B67)),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = arvService!!.serviceName,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = Color(0xFF001B67)
                                )

                                arvService!!.description?.takeIf { it.isNotBlank() }?.let { desc ->
                                    Text(
                                        text = desc,
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "ARV service not found at this clinic",
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val isDateValid = selectedDate >= today
                    val isTimeValid = if (selectedDate == today) selectedTime > LocalTime.now() else true
                    val canBook = arvService != null && isDateValid && isTimeValid && bookingStatus !is BookingStatus.Loading

                    Button(
                        onClick = {
                            if (arvService != null) {
                                viewModel.bookAppointmentWithDetails(
                                    centerId = centerId,
                                    serviceId = arvService!!.serviceId,
                                    date = selectedDate,
                                    time = selectedTime
                                )
                            }
                        },
                        enabled = canBook,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("confirmBookingButton"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001B67))
                    ) {
                        if (bookingStatus is BookingStatus.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp).testTag("bookingProgressBar"),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Confirm", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                        }
                    }

                    if (!canBook && arvService != null &&
                        (selectedDate < today || (selectedDate == today && selectedTime <= LocalTime.now()))
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Please select a future date and time",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    LaunchedEffect(bookingStatus) {
                        if (bookingStatus is BookingStatus.Success) {
                            showBookingSuccessDialog = true
                            viewModel.resetBookingStatus()
                        }
                    }

                    if (bookingStatus is BookingStatus.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Failed: ${(bookingStatus as BookingStatus.Error).message ?: "Unknown error"}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        if (showBookingSuccessDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFCCCCCC))
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Appointment Confirmed",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0A2150),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your appointment has been\nsuccessfully scheduled",
                            fontSize = 20.sp,
                            color = Color(0xFF0A2150),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(40.dp))
                        Button(
                            onClick = {
                                showBookingSuccessDialog = false
                                onNext()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF03266B),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                text = "View",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}