package com.example.fittrack.data.model

import com.example.fittrack.data.local.ExerciseDao
import com.example.fittrack.model.Exercise
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ExerciseRepository(
    private val exerciseDao: ExerciseDao
){
    fun getExercises() : Flow<List<Exercise>> = exerciseDao.getAllExercises()

    suspend fun getExerciseById(id: UUID): Exercise? = exerciseDao.getExerciseById(id)

    suspend fun insertExercise(exercise: Exercise) = exerciseDao.insertExercise(exercise)

    suspend fun updateExercise(exercise: Exercise) = exerciseDao.updateExercise(exercise)

    suspend fun deleteExercise(exercise: Exercise) = exerciseDao.deleteExercise(exercise)
}
