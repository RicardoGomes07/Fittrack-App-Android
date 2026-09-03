package com.example.fittrack.data.local

import androidx.room.*
import com.example.fittrack.model.WorkoutSession
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface WorkoutSessionDao {
    @Query("SELECT * FROM workout_sessions")
    fun getAllWorkoutSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getWorkoutSessionById(id: UUID): WorkoutSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSession(session: WorkoutSession)

    @Update
    suspend fun updateWorkoutSession(session: WorkoutSession)

    @Delete
    suspend fun deleteWorkoutSession(session: WorkoutSession)
}
