package com.halicare.halicare.viewModel

import androidx.lifecycle.ViewModel
import com.halicare.halicare.data.SettingsRepository

class NavigationViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    fun determineStartDestination(): Boolean {

        return settingsRepository.hasCompletedLocationSetup()
    }
}

