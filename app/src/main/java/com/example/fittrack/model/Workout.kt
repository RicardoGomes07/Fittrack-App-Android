package com.example.fittrack.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val exerciseIds: List<UUID> = emptyList()
)
