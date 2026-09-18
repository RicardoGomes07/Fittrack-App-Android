package com.example.fittrack.ui.screens.home

import com.example.fittrack.model.SessionSummary

data class HomeUiState(
    val userName: String = "",
    val dateLabel: String = "",
    val streakDays: Int = 0,
    val motivationMessage: String = "",
    val isLoading: Boolean = true,
    val loggedIn: Boolean = false,
    val hasActiveWorkout: Boolean = false,
    val recentSessions: List<SessionSummary> = emptyList()
)
