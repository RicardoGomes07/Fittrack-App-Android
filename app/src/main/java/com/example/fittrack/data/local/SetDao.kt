package com.example.fittrack.data.local

import androidx.room.*
import com.example.fittrack.model.Set
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface SetDao {
    @Query("SELECT * FROM sets WHERE sessionId = :sessionId")
    fun getSetsForSession(sessionId: UUID): Flow<List<Set>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: Set)

    @Update
    suspend fun updateSet(set: Set)

    @Delete
    suspend fun deleteSet(set: Set)
}
