package com.example.fittrack.model

import androidx.room3.PrimaryKey
import java.util.UUID
import androidx.room3.Entity

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val exercises: List<Exercise>
)