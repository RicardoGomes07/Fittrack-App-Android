package com.example.fittrack.model

data class ExerciseInput(
    val name: String,
    val muscleGroup: MuscleGroup,
    val exerciseType: ExerciseType,
    val equipment: Equipment,
    val description: String = "",
    val imageRes: Int? = null
)