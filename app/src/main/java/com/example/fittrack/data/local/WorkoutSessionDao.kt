package com.example.fittrack.data.local

import androidx.room.*
import com.example.fittrack.model.SessionSummary
import com.example.fittrack.model.WorkoutSession
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface WorkoutSessionDao {
    @Query("SELECT * FROM workout_sessions")
    fun getAllWorkoutSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getWorkoutSessionById(id: UUID): WorkoutSession?

    @Query("SELECT * FROM workout_sessions WHERE userId = :userId AND endedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    fun getActiveSession(userId: UUID): Flow<WorkoutSession?>

    @Query(
        """
        SELECT ws.id AS id, ws.startedAt AS startedAt, ws.endedAt AS endedAt,
               COUNT(DISTINCT s.exerciseId) AS exerciseCount,
               COUNT(s.id) AS setCount,
               COALESCE(SUM(s.weight * s.reps), 0.0) AS volume
        FROM workout_sessions ws
        LEFT JOIN sets s ON s.sessionId = ws.id AND s.isCompleted = 1
        WHERE ws.userId = :userId AND ws.endedAt IS NOT NULL
        GROUP BY ws.id
        ORDER BY ws.startedAt DESC
        LIMIT :limit
        """
    )
    fun getRecentSessionSummaries(userId: UUID, limit: Int): Flow<List<SessionSummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSession(session: WorkoutSession)

    @Update
    suspend fun updateWorkoutSession(session: WorkoutSession)

    @Delete
    suspend fun deleteWorkoutSession(session: WorkoutSession)
}
