package com.halicare.halicare.viewModel

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halicare.halicare.api.AuthTokenProvider
import com.halicare.halicare.model.AppointmentRequest
import com.halicare.halicare.model.ArvAvailability
import com.halicare.halicare.model.Clinic
import com.halicare.halicare.model.ClinicService
import com.halicare.halicare.repository.ClinicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ClinicDetailViewModel(
    private val repository: ClinicRepository,
    private val tokenProvider: AuthTokenProvider,
    private val context: Context
) : ViewModel() {
    private val _clinic = MutableStateFlow<Clinic?>(null)
    val clinic: StateFlow<Clinic?> = _clinic.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _bookingStatus = MutableStateFlow<BookingStatus>(BookingStatus.Idle)
    val bookingStatus: StateFlow<BookingStatus> = _bookingStatus.asStateFlow()

    private val _isUserNewToClinic = MutableStateFlow<Boolean?>(null)
    val isUserNewToClinic: StateFlow<Boolean?> = _isUserNewToClinic.asStateFlow()

    private val _arvAvailability = MutableStateFlow<ArvAvailability?>(null)
    val arvAvailability: StateFlow<ArvAvailability?> = _arvAvailability.asStateFlow()

    private val _arvService = MutableStateFlow<ClinicService?>(null)
    val arvService: StateFlow<ClinicService?> = _arvService.asStateFlow()

    private val prefs = context.getSharedPreferences("HALICARE_PREFS", Context.MODE_PRIVATE)

    init {
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkIfUserIsNew(clinicId: String) {
        if (_isUserNewToClinic.value != null) return
        _isUserNewToClinic.value = null
        viewModelScope.launch {
            try {
                val userId = prefs.getString("USER_ID", null)
                if (userId == null) {
                    _isUserNewToClinic.value = true
                    return@launch
                }
                val hasBooked = repository.hasUserBookedAtClinic(userId, clinicId)
                _isUserNewToClinic.value = !hasBooked
            } catch (e: Exception) {
                Log.e("ClinicDetailVM", "Error checking booking history", e)
                _isUserNewToClinic.value = true
            }
        }
    }

    fun loadClinicDetails(centerId: String) {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val clinicDetails = repository.getClinicWithServices(centerId)
                _clinic.value = clinicDetails

                clinicDetails?.services?.let { services ->
                    val arvService = services.find { service ->
                        service.serviceName.contains("ARV", ignoreCase = true)
                    }
                    _arvService.value = arvService
                } ?: run {
                    _arvService.value = null
                }

                if (clinicDetails == null) {
                    _errorMessage.value = "Clinic not found."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load clinic details: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun bookAppointmentWithDetails(
        centerId: String,
        serviceId: String,
        date: LocalDate,
        time: LocalTime
    ) {
        viewModelScope.launch {
            _bookingStatus.value = BookingStatus.Loading
            try {
                val userId = prefs.getString("USER_ID", null)
                    ?: throw IllegalStateException("User not logged in")
                val localDateTime = LocalDateTime.of(date, time)
                val utcDateTime = localDateTime
                    .atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("UTC"))
                val isoDateTime = utcDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

                val transferUriString = prefs.getString("TRANSFER_LETTER_URI", null)
                val transferUri: Uri? = transferUriString?.let { Uri.parse(it) }

                val request = AppointmentRequest(
                    centerId = centerId,
                    serviceId = serviceId,
                    appointmentDate = isoDateTime,
                    userId = userId,
                    transferLetter = null,
                    bookingStatus = "Upcoming"
                )

                val result = repository.bookAppointment(context, request, transferUri)
                if (result.isSuccess) {
                    prefs.edit().remove("TRANSFER_LETTER_URI").apply()
                    _isUserNewToClinic.value = false
                    _bookingStatus.value = BookingStatus.Success
                } else {
                    _bookingStatus.value = BookingStatus.Error(result.exceptionOrNull()?.message ?: "Booking failed")
                }
            } catch (e: Exception) {
                _bookingStatus.value = BookingStatus.Error("Error: ${e.message}")
            }
        }
    }

    fun resetBookingStatus() {
        _bookingStatus.value = BookingStatus.Idle
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

sealed class BookingStatus {
    object Idle : BookingStatus()
    object Loading : BookingStatus()
    object Success : BookingStatus()
    data class Error(val message: String) : BookingStatus()
}