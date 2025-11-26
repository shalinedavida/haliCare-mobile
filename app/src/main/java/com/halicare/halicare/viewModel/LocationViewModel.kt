package com.halicare.halicare.viewModel
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ResolvableApiException
import com.halicare.halicare.data.LocationRepository
import com.halicare.halicare.data.LocationSettingsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class LocationUIState(
    val isLoading: Boolean = false,
    val location: Location? = null,
    val isResolvable: Boolean = false,
    val resolvableException: ResolvableApiException? = null,
    val error: String? = null
)


class LocationViewModel(private val repository: LocationRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LocationUIState())
    val uiState: StateFlow<LocationUIState> = _uiState.asStateFlow()


    fun requestLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val settingsResult = repository.checkLocationSettings()
            when (settingsResult) {
                is LocationSettingsResult.Success -> fetchLocation()
                is LocationSettingsResult.Resolvable -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isResolvable = true,
                    resolvableException = settingsResult.exception
                )
                is LocationSettingsResult.Failure -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Location settings are not satisfied."
                )
            }
        }
    }


    private fun fetchLocation() {
        viewModelScope.launch {
            val location = repository.fetchLocation()
            if (location != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    location = location,
                    isResolvable = false,
                    resolvableException = null,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    location = null,
                    isResolvable = false,
                    resolvableException = null,
                    error = "Unable to fetch location."
                )
            }
        }
    }
}

