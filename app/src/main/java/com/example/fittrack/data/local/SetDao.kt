package com.example.fittrack.data.local

import androidx.room.*
import com.example.fittrack.model.SetHistory
import com.example.fittrack.model.WorkoutSet
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface SetDao {
    @Query("SELECT * FROM sets WHERE sessionId = :sessionId ORDER BY position")
    fun getSetsForSession(sessionId: UUID): Flow<List<WorkoutSet>>

    @Query(
        """
        SELECT s.weight AS weight, s.reps AS reps, ws.startedAt AS startedAt
        FROM sets s
        INNER JOIN workout_sessions ws ON ws.id = s.sessionId
        WHERE ws.userId = :userId AND s.exerciseId = :exerciseId AND s.isCompleted = 1
        """
    )
    fun getSetHistory(userId: UUID, exerciseId: UUID): Flow<List<SetHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: WorkoutSet)

    @Update
    suspend fun updateSet(set: WorkoutSet)

    @Query("UPDATE sets SET weight = :weight, reps = :reps WHERE id = :id")
    suspend fun updateValues(id: UUID, weight: Double, reps: Int)

    @Query("UPDATE sets SET isCompleted = :completed WHERE id = :id")
    suspend fun updateCompleted(id: UUID, completed: Boolean)

    @Delete
    suspend fun deleteSet(set: WorkoutSet)

    @Query("DELETE FROM sets WHERE sessionId = :sessionId AND exerciseId = :exerciseId")
    suspend fun deleteSetsForExercise(sessionId: UUID, exerciseId: UUID)

    @Query("DELETE FROM sets WHERE sessionId = :sessionId AND isCompleted = 0")
    suspend fun deleteIncompleteSets(sessionId: UUID)
}
