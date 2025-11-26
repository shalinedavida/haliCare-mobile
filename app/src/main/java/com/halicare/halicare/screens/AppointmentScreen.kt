package com.halicare.halicare.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.halicare.halicare.model.Appointment
import com.halicare.halicare.model.AppointmentStatus
import com.halicare.halicare.viewModel.ClinicViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import android.util.Log 
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
fun Appointment.getAppointmentStatus(): AppointmentStatus {
   return when (this.bookingStatus) {
       "Cancelled" -> AppointmentStatus.Cancelled
       "Completed" -> AppointmentStatus.Completed
       else -> {
           val today = LocalDate.now()
           
           try {
               val datePart = if (this.appointmentDate.length >= 10) {
                   this.appointmentDate.substring(0, 10)
               } else {
                   this.appointmentDate
               }
               
               val appointmentDate = LocalDate.parse(datePart, DateTimeFormatter.ISO_LOCAL_DATE)
              
               if (appointmentDate.isBefore(today)) {
                   return AppointmentStatus.Cancelled
               } else {
                   return AppointmentStatus.Upcoming
               }
           } catch (e: DateTimeParseException) {
               Log.e("AppointmentStatus", "FINAL DATE PARSE FAIL for ID: ${this.appointmentId}. String: '${this.appointmentDate}'", e)
               return AppointmentStatus.Upcoming 
           } catch (e: Exception) {
               Log.e("AppointmentStatus", "Generic error during date check for ID: ${this.appointmentId}. String: '${this.appointmentDate}'", e)
               return AppointmentStatus.Upcoming
           }
       }
   }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppointmentScreen(
   onBack: () -> Unit = {},
   selectedTab: AppointmentStatus = AppointmentStatus.Upcoming,
   onTabSelected: (AppointmentStatus) -> Unit = {}
) {
   val viewModel: ClinicViewModel = koinViewModel()
   var currentTab by remember { mutableStateOf(selectedTab) }
   val allAppointments by viewModel.appointments.collectAsState()
   val isLoading by viewModel.isLoading.collectAsState()
   val isCancelling by viewModel.isCancelling.collectAsState()
   val errorMsg by viewModel.errorMessage.collectAsState()


   val appointmentCounts = remember(allAppointments) {
       AppointmentStatus.values().associateWith { status ->
           allAppointments.count { it.getAppointmentStatus() == status }
       }
   }


   val filteredAppointments = remember(allAppointments, currentTab) {
       allAppointments.filter {
           it.getAppointmentStatus() == currentTab
       }
   }


   val snackbarHostState = remember { SnackbarHostState() }


   LaunchedEffect(errorMsg) {
       errorMsg?.let {
           snackbarHostState.showSnackbar(it)
           viewModel.clearError()
       }
   }


   LaunchedEffect(Unit) {
       viewModel.loadAppointments()
   }


   Scaffold(
       snackbarHost = { SnackbarHost(snackbarHostState) },
       topBar = {
           Box(
               modifier = Modifier
                   .fillMaxWidth()
                   .background(Color(0xFF03266B))
                   .height(52.dp),
               contentAlignment = Alignment.CenterStart
           ) {
               Row(
                   Modifier.fillMaxSize(),
                   verticalAlignment = Alignment.CenterVertically
               ) {
                   IconButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) {
                       Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                   }
                   Spacer(Modifier.width(12.dp))
                   Text(
                       "My Appointments",
                       color = Color.White,
                       fontWeight = FontWeight.Bold,
                       fontSize = 22.sp,
                       modifier = Modifier.padding(start = 8.dp)
                   )
               }
           }
       },
       floatingActionButton = {
           FloatingActionButton(
               onClick = { viewModel.loadAppointments() },
               containerColor = Color(0xFF03266B),
               contentColor = Color.White
           ) {
               Icon(Icons.Default.Refresh, contentDescription = "Refresh")
           }
       }
   ) { paddingValues ->
       Column(
           modifier = Modifier
               .fillMaxSize()
               .background(Color(0xFFF5F7FA))
               .padding(paddingValues)
       ) {
           Row(
               Modifier
                   .fillMaxWidth()
                   .background(Color.White)
                   .padding(vertical = 12.dp, horizontal = 12.dp),
               horizontalArrangement = Arrangement.spacedBy(12.dp)
           ) {
               AppointmentStatus.values().forEach { tab ->
                   val count = appointmentCounts[tab] ?: 0


                   Button(
                       onClick = {
                           currentTab = tab
                           onTabSelected(tab)
                       },
                       colors = ButtonDefaults.buttonColors(
                           containerColor = if (currentTab == tab) Color(0xFF03266B) else Color(0xFFF2F6FF),
                           contentColor = if (currentTab == tab) Color.White else Color(0xFF03266B)
                       ),
                       shape = RoundedCornerShape(12.dp),
                       elevation = ButtonDefaults.buttonElevation(
                           defaultElevation = if (currentTab == tab) 4.dp else 0.dp
                       ),
                       modifier = Modifier
                           .weight(1f)
                           .height(36.dp)
                   ) {
                       Text("${tab.name} ($count)", fontWeight = FontWeight.Bold,
                           fontSize = 13.sp)
                   }
               }
           }


           if (isLoading) {
               Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                   Column(horizontalAlignment = Alignment.CenterHorizontally) {
                       CircularProgressIndicator(color = Color(0xFF03266B))
                       Spacer(Modifier.height(16.dp))
                       Text(
                           "Loading your appointments...",
                           color = Color(0xFF03266B),
                           fontSize = 16.sp
                       )
                   }
               }
           } else if (filteredAppointments.isEmpty()) {
               Box(
                   Modifier
                       .fillMaxSize()
                       .padding(16.dp),
                   contentAlignment = Alignment.Center
               ) {
                   Column(horizontalAlignment = Alignment.CenterHorizontally) {
                       Text(
                           "No ${currentTab.name.lowercase()} appointments",
                           color = Color.Gray,
                           fontSize = 18.sp
                       )
                       Spacer(Modifier.height(8.dp))
                       Text(
                           "Your ${currentTab.name.lowercase()} appointments will appear here",
                           color = Color.Gray,
                           fontSize = 14.sp
                       )
                   }
               }
           } else {
               LazyColumn(
                   modifier = Modifier
                       .fillMaxSize()
                       .padding(horizontal = 16.dp),
                   verticalArrangement = Arrangement.spacedBy(16.dp),
                   contentPadding = PaddingValues(vertical = 16.dp)
               ) {
                   items(filteredAppointments) { appointment ->
                       AppointmentCard(
                           appointment = appointment,
                           onCancel = {
                               viewModel.cancelAppointment(appointment.appointmentId) { }
                           },
                           onComplete = {
                               viewModel.completeAppointment(appointment.appointmentId)
                           },
                           isCancelling = isCancelling
                       )
                   }
               }
           }
       }
   }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppointmentCard(
   appointment: Appointment,
   onCancel: () -> Unit,
   onComplete: () -> Unit,
   isCancelling: Boolean
) {
   val status = appointment.getAppointmentStatus()
   val isUpcoming = status == AppointmentStatus.Upcoming 
   val isCompleted = status == AppointmentStatus.Completed
   val isCancelled = status == AppointmentStatus.Cancelled
   
   val formattedDate = remember(appointment.appointmentDate) {
       val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE
       
       val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("en", "KE"))

       try {
           val datePart = if (appointment.appointmentDate.length >= 10) {
               appointment.appointmentDate.substring(0, 10)
           } else {
               appointment.appointmentDate
           }
           LocalDate.parse(datePart, inputFormatter).format(outputFormatter)
       } catch (e: Exception) {
           Log.e("AppointmentCard", "Failed to format date: ${appointment.appointmentDate}", e)
           appointment.appointmentDate
       }
   }

    val showActionButtons = status == AppointmentStatus.Upcoming


   val cardColor = when {
       isCancelled -> Color(0xFFF8F8F8)
       isCompleted -> Color(0xFFE8F5E9)
       else -> Color.White
   }


   val statusColor = when {
       isCancelled -> Color(0xFF757575)
       isCompleted -> Color(0xFF2E7D32)
       else -> Color(0xFF03266B)
   }


   val statusText = when (status) {
       AppointmentStatus.Cancelled -> "Cancelled"
       AppointmentStatus.Completed -> "Completed"
       AppointmentStatus.Upcoming -> "Upcoming"
   }


   Card(
       modifier = Modifier
           .fillMaxWidth()
           .shadow(
               elevation = 4.dp,
               shape = RoundedCornerShape(16.dp)
           ),
       shape = RoundedCornerShape(16.dp),
       colors = CardDefaults.cardColors(containerColor = cardColor)
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
               Text(
                   text = statusText,
                   color = Color.White,
                   fontWeight = FontWeight.Bold,
                   modifier = Modifier
                       .background(
                           color = statusColor,
                           shape = RoundedCornerShape(12.dp)
                       )
                       .padding(horizontal = 12.dp, vertical = 4.dp)
               )

               if (showActionButtons) {
                   Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                       Button(
                           onClick = onComplete,
                           enabled = !isCancelling,
                           shape = RoundedCornerShape(8.dp),
                           colors = ButtonDefaults.buttonColors(
                               containerColor = Color(0xFF2E7D32),
                               contentColor = Color.White
                           ),
                           modifier = Modifier.height(36.dp)
                       ) {
                           Text("Complete", fontWeight = FontWeight.Bold)
                       }

                       OutlinedButton(
                           onClick = onCancel,
                           enabled = !isCancelling,
                           shape = RoundedCornerShape(8.dp),
                           border = ButtonDefaults.outlinedButtonBorder.copy(
                               width = 1.dp,
                           ),
                           colors = ButtonDefaults.outlinedButtonColors(
                               contentColor = Color(0xFF03266B)
                           ),
                           modifier = Modifier.height(36.dp)
                       ) {
                           if (isCancelling) {
                               CircularProgressIndicator(
                                   modifier = Modifier.size(16.dp),
                                   strokeWidth = 2.dp,
                                   color = Color(0xFF03266B)
                               )
                           } else {
                               Text("Cancel", fontWeight = FontWeight.Bold)
                           }
                       }
                   }
               }
           }

           Spacer(Modifier.height(12.dp))

           Row(
               modifier = Modifier.fillMaxWidth(),
               verticalAlignment = Alignment.Top
           ) {
               AsyncImage(
                   model = appointment.imageUrl,
                   contentDescription = appointment.clinicName,
                   modifier = Modifier
                       .size(90.dp)
                       .clip(RoundedCornerShape(12.dp))
               )


               Spacer(Modifier.width(16.dp))


               Column(
                   modifier = Modifier.weight(1f)
               ) {
                   Text(
                       text = appointment.clinicName ?: "Unknown Clinic",
                       fontWeight = FontWeight.Bold,
                       fontSize = 18.sp,
                       color = Color(0xFF0A2150),
                       maxLines = 1
                   )


                   Spacer(Modifier.height(4.dp))


                   Text(
                       text = "Service: ${appointment.serviceName}",
                       color = Color(0xFF546E7A),
                       fontSize = 15.sp,
                       maxLines = 1
                   )


                   Spacer(Modifier.height(8.dp))


                   Row {
                       Icon(
                           Icons.Default.ArrowBack,
                           contentDescription = null,
                           tint = Color(0xFF03266B),
                           modifier = Modifier.size(16.dp).rotate(-90f)
                       )
                       Spacer(Modifier.width(4.dp))
                       Text(
                           text = formattedDate,
                           color = Color(0xFF546E7A),
                           fontSize = 14.sp
                       )
                   }

                   Spacer(Modifier.height(4.dp))


                   Row {
   
                    Icon(
                           Icons.Default.ArrowBack,
                           contentDescription = null,
                           tint = Color(0xFF03266B),
                           modifier = Modifier.size(16.dp).rotate(-90f)
                       )
                       Spacer(Modifier.width(4.dp))
                       Text(
                           text = appointment.time,
                           color = Color(0xFF546E7A),
                           fontSize = 14.sp
                       )
                   }
               }
           }
       }
   }
}
