package com.halicare.halicare.viewModel

import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halicare.halicare.model.Appointment
import com.halicare.halicare.model.ClinicDetails
import com.halicare.halicare.model.ArvAvailability
import com.halicare.halicare.repository.ClinicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
class ClinicViewModel(
    private val repository: ClinicRepository,
    private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("HALICARE_PREFS", Context.MODE_PRIVATE)

    private val _clinics = MutableStateFlow<List<ClinicDetails>>(emptyList())
    val clinics: StateFlow<List<ClinicDetails>> = _clinics

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments

    private val _isCancelling = MutableStateFlow(false)
    val isCancelling: StateFlow<Boolean> = _isCancelling

    private val _cancelResult = MutableStateFlow<Result<Unit>?>(null)
    val cancelResult: StateFlow<Result<Unit>?> = _cancelResult

    private val _arvAvailabilities = MutableStateFlow<List<ArvAvailability>>(emptyList())
    val arvAvailabilities: StateFlow<List<ArvAvailability>> = _arvAvailabilities

    private val geocodedCache = mutableMapOf<String, Pair<Double, Double>>()

    init {
        loadAppointments()
        loadArvAvailabilities()
    }

    private fun loadArvAvailabilities() {
        viewModelScope.launch {
            try {
                Log.d("ClinicViewModel", "Loading ARV availabilities")
                val availabilities = repository.getArvAvailability()
                _arvAvailabilities.value = availabilities
                Log.d("ClinicViewModel", "ARV availabilities loaded: ${availabilities.size}")
            } catch (e: Exception) {
                Log.e("ClinicViewModel", "Failed to load ARV availabilities", e)
            }
        }
    }

    fun fetchNearbyClinics(latitude: Double, longitude: Double) {
        if (_isLoading.value) return

        viewModelScope.launch {
            _errorMessage.value = null
            _isLoading.value = true
            try {
                Log.d("ClinicViewModel", "Fetching clinics")
                val rawClinics = repository.getNearbyClinics(latitude, longitude)
                val enhancedClinics = enhanceClinicsWithGeocoding(rawClinics)

                val filteredClinics = enhancedClinics.filter { clinic ->
                    val availability = _arvAvailabilities.value.find { it.centerId == clinic.center_id }
                    availability?.isAvailable == true
                }

                _clinics.value = filteredClinics
                Log.d("ClinicViewModel", "Clinics filtered: ${filteredClinics.size} out of ${enhancedClinics.size}")
            } catch (e: Exception) {
                Log.e("ClinicViewModel", "Failed to fetch clinics", e)
                _errorMessage.value = "Failed to load clinics: ${e.message ?: "Unknown error"}"
                _clinics.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun enhanceClinicsWithGeocoding(clinics: List<ClinicDetails>): List<ClinicDetails> {
        return clinics.map { clinic ->
            if (clinic.latitude != 0.0 && clinic.longitude != 0.0) {
                return@map clinic
            }

            if (clinic.address.isNullOrBlank()) {
                return@map clinic
            }

            geocodedCache[clinic.address]?.let { (lat, lng) ->
                return@map clinic.copy(latitude = lat, longitude = lng)
            }

            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = withContext(Dispatchers.IO) {
                    geocoder.getFromLocationName(clinic.address, 1)
                }
                if (!addresses.isNullOrEmpty()) {
                    val lat = addresses[0].latitude
                    val lng = addresses[0].longitude
                    geocodedCache[clinic.address] = lat to lng
                    clinic.copy(latitude = lat, longitude = lng)
                } else {
                    Log.w("ClinicViewModel", "Geocoding returned no results for: ${clinic.address}")
                    clinic
                }
            } catch (e: Exception) {
                Log.e("ClinicViewModel", "Geocoding failed for address: ${clinic.address}", e)
                clinic
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadAppointments() {
        viewModelScope.launch {
            _isLoading.value = true
            var isSuccessful = false
            try {
                val userId = prefs.getString("USER_ID", null)
                if (userId == null) {
                    _errorMessage.value = "User not logged in. Please log in again."
                    return@launch
                }
                _appointments.value = repository.getAppointmentsByUserId(userId)
                isSuccessful = true
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load appointments: ${e.message}"
            } finally {
                if (isSuccessful) {
                    _isLoading.value = false
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun cancelAppointment(appointmentId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                _isCancelling.value = true
                Log.d("ClinicViewModel", "Attempting to cancel appointment: $appointmentId")
                val userId = prefs.getString("USER_ID", null)
                val appointmentToCancel = _appointments.value.find { it.appointmentId == appointmentId }
                if (userId == null || appointmentToCancel == null) {
                    _errorMessage.value = "Cannot cancel appointment: User not logged in or appointment not found."
                    onComplete(false)
                    return@launch
                }
                val appointmentDate = appointmentToCancel.appointmentDate
                val centerId = appointmentToCancel.centerId
                val serviceId = appointmentToCancel.serviceId
                val result = repository.cancelAppointment(
                    appointmentId,
                    userId,
                    appointmentDate,
                    centerId,
                    serviceId
                )
                if (result.isSuccess) {
                    loadAppointments()
                    onComplete(true)
                } else {
                    Log.e("ClinicViewModel", "Cancellation failed on server: ${result.exceptionOrNull()?.message}")
                    loadAppointments()
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to cancel appointment. Please check your network."
                    onComplete(false)
                }
            } catch (e: Exception) {
                Log.e("ClinicViewModel", "Exception during cancellation", e)
                loadAppointments()
                _errorMessage.value = "Network error: Failed to reach server to cancel appointment."
                onComplete(false)
            } finally {
                _isCancelling.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun completeAppointment(appointmentId: String) {
        viewModelScope.launch {
            try {
                _isCancelling.value = true
                Log.d("ClinicViewModel", "Attempting to complete appointment: $appointmentId")
                val userId = prefs.getString("USER_ID", null)
                val appointmentToComplete = _appointments.value.find { it.appointmentId == appointmentId }
                if (userId == null || appointmentToComplete == null) {
                    _errorMessage.value = "Cannot complete appointment: User not logged in or appointment not found."
                    return@launch
                }
                val appointmentDate = appointmentToComplete.appointmentDate
                val centerId = appointmentToComplete.centerId
                val serviceId = appointmentToComplete.serviceId
                val result = repository.updateAppointmentStatus(
                    appointmentId = appointmentId,
                    status = "Completed",
                    userId = userId,
                    appointmentDate = appointmentDate,
                    centerId = centerId,
                    serviceId = serviceId
                )
                if (result.isSuccess) {
                    loadAppointments()
                    Log.d("ClinicViewModel", "Appointment completed successfully. Reloading data.")
                } else {
                    loadAppointments()
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to complete appointment."
                }
            } catch (e: Exception) {
                Log.e("ClinicViewModel", "Exception during completion", e)
                loadAppointments()
                _errorMessage.value = "Network error: Failed to reach server to complete appointment."
            } finally {
                _isCancelling.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearCancelResult() {
        _cancelResult.value = null
    }
}