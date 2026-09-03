package com.example.fittrack.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val muscleGroup: MuscleGroup,
    //val equipment: equipment
    val image: String? = null,
)