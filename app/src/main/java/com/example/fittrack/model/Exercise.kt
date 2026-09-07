package com.example.fittrack.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "exercises",
    indices = [Index(value = ["name"], unique = true)]
)
data class Exercise(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val muscleGroup: MuscleGroup,
    val description: String = "",
    val exerciseType: ExerciseType,
    val imageRes: Int? = null
)