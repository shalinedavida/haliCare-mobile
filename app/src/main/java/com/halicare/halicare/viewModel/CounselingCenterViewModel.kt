package com.halicare.halicare.viewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halicare.halicare.model.CounselingCenterDetails
import com.halicare.halicare.repository.CounselingCenterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class CounselingCenterViewModel(
    private val repository: CounselingCenterRepository
) : ViewModel() {


    private val _centers = MutableStateFlow<List<CounselingCenterDetails>>(emptyList())
    val centers: StateFlow<List<CounselingCenterDetails>> = _centers.asStateFlow()


    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()




    fun loadCenters() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _centers.value = repository.getCenters()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }
}

