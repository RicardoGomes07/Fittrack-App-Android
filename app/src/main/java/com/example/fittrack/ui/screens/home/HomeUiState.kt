package com.example.fittrack.ui.screens.home

data class HomeUiState(
    val userName: String = "",
    val dateLabel: String = "",
    val streakDays: Int = 0,
    val motivationMessage: String = "",
    val isLoading: Boolean = false
)
