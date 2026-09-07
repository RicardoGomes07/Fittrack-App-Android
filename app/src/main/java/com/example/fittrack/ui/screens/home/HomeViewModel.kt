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

class HomeViewModel : ViewModel() {

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
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Simulating data loading
            val dateLabel = getFormattedDate()
            val motivation = motivationMessages.random()
            
            _uiState.update {
                it.copy(
                    userName = "Ricardo", // This could come from a repository
                    dateLabel = dateLabel,
                    streakDays = 5, // This could come from a workout repository
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
