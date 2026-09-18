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
import com.example.fittrack.data.model.WorkoutSessionRepository
import com.example.fittrack.model.SessionSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class HomeViewModel(
    private val authManager: AuthManager,
    private val sessionRepository: WorkoutSessionRepository
) : ViewModel() {

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
        observeWorkouts()
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeWorkouts() {
        authManager.currentUser
            .flatMapLatest { user ->
                if (user == null) {
                    flowOf(false to emptyList<SessionSummary>())
                } else {
                    combine(
                        sessionRepository.getActiveSession(user.id),
                        sessionRepository.getRecentSessionSummaries(user.id, RECENT_SESSIONS_LIMIT)
                    ) { active, recent -> (active != null) to recent }
                }
            }
            .onEach { (hasActive, recent) ->
                _uiState.update { it.copy(hasActiveWorkout = hasActive, recentSessions = recent) }
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
                    streakDays = 0,
                    motivationMessage = motivation,
                    isLoading = false
                )
            }
        }
    }

    private companion object {
        const val RECENT_SESSIONS_LIMIT = 5
    }

    private fun getFormattedDate(): String {
        val now = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())
        val dateString = now.format(formatter)
        return "Today, $dateString"
    }
}
