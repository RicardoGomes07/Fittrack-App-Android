package com.example.fittrack.ui.screens.profile

import com.example.fittrack.model.User
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ProfileUiState(
    val name: String = "",
    val username: String = "",
    val level: Int = 1,
    val xp: Int = 0,
    val maxXp: Int = 100,
    val memberSince: String = "",
    val weight: Double = 0.0,
    val height: Int = 0,
    val gender: String = "Other",
    val streakDays: Int = 0,
    val isPro: Boolean = true,
    val consistency: Double = 0.0,
    val workouts: Int = 0,
    val workoutsChange: Int = 0,
    val tonnage: Double = 0.0,
    val hoursTrained: Int = 0,
    val avgSessionMinutes: Int = 0,
    val monthlyPRs: Int = 0,
    val prs: List<PersonalRecord> = emptyList(),
    val unitSystem: String = "kg"
)

data class PersonalRecord(
    val name: String,
    val value: Int,
    val unit: String,
    val date: String,
    val isNew: Boolean = false
)

fun User.toProfileUiState(currentSystem: String): ProfileUiState {
    val formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault())
    val nextLevelXp = level * 500 // Example logic: 500, 1000, 1500...
    return ProfileUiState(
        name = name,
        username = "@$nickname",
        level = level,
        xp = xp,
        maxXp = nextLevelXp,
        memberSince = memberSince.format(formatter),
        weight = weight,
        height = height,
        gender = gender.name.lowercase().replaceFirstChar { it.uppercase() },
        unitSystem = currentSystem,
        streakDays = 0,
        consistency = 0.0,
        workouts = 0,
        workoutsChange = 0,
        tonnage = 0.0,
        hoursTrained = 0,
        avgSessionMinutes = 0,
        monthlyPRs = 0,
        prs = emptyList()
    )
}
