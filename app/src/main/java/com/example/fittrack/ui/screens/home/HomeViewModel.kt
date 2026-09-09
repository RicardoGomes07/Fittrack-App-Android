package com.example.fittrack.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

import com.example.fittrack.data.AuthManager
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class HomeViewModel(private val authManager: AuthManager) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val motivationMessages = listOf(
        "Consistency is the key to success.",
        "Your only limit is you.",
        "Sweat today, smile tomorrow.",
        "Don't stop until you're proud.",
        "Make yourself proud.",
        "Everything you've ever wanted is on the other side of fear.",
        "The only bad workout is the one that didn't happen."
    )

    init {
        observeAuthState()
        loadHomeData()
    }

    private fun observeAuthState() {
        authManager.currentUser
            .onEach { user ->
                _uiState.update { 
                    it.copy(
                        loggedIn = user != null,
                        userName = user?.name ?: ""
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val dateLabel = getFormattedDate()
            val motivation = motivationMessages.random()
            
            _uiState.update {
                it.copy(
                    dateLabel = dateLabel,
                    streakDays = if (it.loggedIn) 5 else 0,
                    motivationMessage = motivation,
                    isLoading = false
                )
            }
        }
    }

    private fun getFormattedDate(): String {
        val now = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())
        val dateString = now.format(formatter)
        return "Today, $dateString"
    }
}
