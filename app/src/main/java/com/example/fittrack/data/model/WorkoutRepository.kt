package com.example.fittrack.data.model

import com.example.fittrack.data.local.WorkoutDao
import com.example.fittrack.model.Workout
import kotlinx.coroutines.flow.Flow

class WorkoutRepository (
    private val workoutDao: WorkoutDao
){
    fun getAllWorkouts(): Flow<List<Workout>> = workoutDao.getAllWorkouts()

    suspend fun insertWorkout(workout: Workout) = workoutDao.insertWorkout(workout)

    suspend fun updateWorkout(workout: Workout) = workoutDao.updateWorkout(workout)

    suspend fun deleteWorkout(workout: Workout) = workoutDao.deleteWorkout(workout)
}
