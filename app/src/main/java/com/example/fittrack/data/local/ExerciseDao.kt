package com.example.fittrack.data.local

import androidx.room3.Dao
import androidx.room3.Query
import com.example.fittrack.model.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises")
    fun getAllExercises(): Flow<List<Exercise>>
}