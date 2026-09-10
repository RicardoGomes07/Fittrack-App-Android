package com.example.fittrack.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrack.data.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine

class ProfileViewModel(private val authManager: AuthManager) : ViewModel() {
    val loggedInUser = authManager.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isInitialized = authManager.isInitialized
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _unitSystem = MutableStateFlow("kg")
    
    val uiState: StateFlow<ProfileUiState> = combine(
        authManager.currentUser,
        _unitSystem
    ) { user, system ->
        user?.toProfileUiState(system) ?: ProfileUiState(unitSystem = system)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    fun toggleUnitSystem() {
        _unitSystem.update { if (it == "kg") "lbs" else "kg" }
    }

    fun logout() {
        viewModelScope.launch {
            authManager.logout()
        }
    }
}
