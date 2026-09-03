package com.example.fittrack.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import java.util.UUID

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val muscleGroup: muscleGroup,
    //val equipment: equipment
    val image: String? = null,
)