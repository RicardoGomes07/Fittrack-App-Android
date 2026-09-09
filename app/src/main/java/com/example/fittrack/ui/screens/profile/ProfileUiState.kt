package com.example.fittrack.ui.screens.profile

data class ProfileUiState(
    val name: String = "Alex Morgan",
    val username: String = "@alex_lifts",
    val level: Int = 18,
    val memberSince: String = "Jan 2023",
    val streakDays: Int = 14,
    val isPro: Boolean = true,
    val consistency: Double = 92.4,
    val workouts: Int = 184,
    val workoutsChange: Int = 12,
    val tonnage: Double = 412.5,
    val hoursTrained: Int = 156,
    val avgSessionMinutes: Int = 54,
    val monthlyPRs: Int = 7,
    val prs: List<PersonalRecord> = listOf(
        PersonalRecord("Squat", 140, "kg", "Oct 24 • +5kg gain"),
        PersonalRecord("Bench Press", 105, "kg", "Nov 02 • +2.5kg gain"),
        PersonalRecord("Deadlift", 185, "kg", "3 days ago • Epley est.", isNew = true),
        PersonalRecord("OHP", 65, "kg", "Sep 18 • Verified")
    ),
    val badges: List<BadgeInfo> = listOf(
        BadgeInfo("Century Club", "100+ Workouts", "workspace_premium"),
        BadgeInfo("Iron Grip", "180kg Deadlift", "front_hand"),
        BadgeInfo("Streak King", "14d Active", "local_fire_department"),
        BadgeInfo("Dawn Patrol", "5AM Lifter", "wb_sunny")
    ),
    val unitSystem: String = "kg"
)

data class PersonalRecord(
    val name: String,
    val value: Int,
    val unit: String,
    val date: String,
    val isNew: Boolean = false
)

data class BadgeInfo(
    val name: String,
    val description: String,
    val icon: String
)
