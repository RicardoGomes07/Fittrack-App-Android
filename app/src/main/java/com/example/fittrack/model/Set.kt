package com.example.fittrack.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import java.util.UUID

@Entity(tableName = "sets")
data class Set(
    @PrimaryKey(autoGenerate = true)
    val id: UUID = UUID.randomUUID(),
    val exerciseId: UUID,
    val sessionId: UUID,
    val reps: Int,
    val weight: Double,
)