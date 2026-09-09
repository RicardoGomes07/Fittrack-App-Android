package com.example.fittrack.data.local

import androidx.room.*
import com.example.fittrack.model.WorkoutSet
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface SetDao {
    @Query("SELECT * FROM sets WHERE sessionId = :sessionId")
    fun getSetsForSession(sessionId: UUID): Flow<List<WorkoutSet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: WorkoutSet)

    @Update
    suspend fun updateSet(set: WorkoutSet)

    @Delete
    suspend fun deleteSet(set: WorkoutSet)
}
