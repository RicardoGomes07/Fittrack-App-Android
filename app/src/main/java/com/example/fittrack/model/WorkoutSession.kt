package com.example.fittrack.model

import androidx.room3.PrimaryKey
import java.util.UUID
import androidx.room3.Entity
import java.time.LocalDate

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true)
    val id: UUID = UUID.randomUUID(),
    val workoutId: UUID,
    val date: LocalDate,
)