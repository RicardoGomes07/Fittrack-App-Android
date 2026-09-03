package com.example.fittrack.data.model

import com.example.fittrack.data.local.ExerciseDao
import com.example.fittrack.model.Exercise
import kotlinx.coroutines.flow.Flow

class ExerciseRepository(
    private val exerciseDao: ExerciseDao
){
    fun getExercises() : Flow<List<Exercise>> = exerciseDao.getAllExercises()
}