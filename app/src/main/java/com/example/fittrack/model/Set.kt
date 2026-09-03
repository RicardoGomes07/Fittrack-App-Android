package com.example.fittrack.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "sets")
data class Set(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val exerciseId: UUID,
    val sessionId: UUID,
    val reps: Int,
    val weight: Double,
)