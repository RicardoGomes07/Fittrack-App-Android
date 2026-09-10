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
        // Mocking some stats for now as we don't have workout history fully implemented
        streakDays = 14,
        consistency = 92.4,
        workouts = 184,
        workoutsChange = 12,
        tonnage = 412.5,
        hoursTrained = 156,
        avgSessionMinutes = 54,
        monthlyPRs = 7,
        prs = listOf(
            PersonalRecord("Squat", 140, "kg", "Oct 24 • +5kg gain"),
            PersonalRecord("Bench Press", 105, "kg", "Nov 02 • +2.5kg gain"),
            PersonalRecord("Deadlift", 185, "kg", "3 days ago • Epley est.", isNew = true),
            PersonalRecord("OHP", 65, "kg", "Sep 18 • Verified")
        )
    )
}
