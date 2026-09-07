package com.example.fittrack.model

import androidx.room.PrimaryKey
import java.util.UUID
import androidx.room.Entity
import java.time.LocalDate

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val workoutId: UUID,
    val date: LocalDate,
)