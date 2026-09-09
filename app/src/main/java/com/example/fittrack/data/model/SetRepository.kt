package com.example.fittrack.data.model

import com.example.fittrack.data.local.SetDao

import com.example.fittrack.model.WorkoutSet
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class SetRepository (
    private val setDao: SetDao
){
    fun getSetsForSession(sessionId: UUID): Flow<List<WorkoutSet>> = setDao.getSetsForSession(sessionId)

    suspend fun insertSet(set: WorkoutSet) = setDao.insertSet(set)

    suspend fun updateSet(set: WorkoutSet) = setDao.updateSet(set)

    suspend fun deleteSet(set: WorkoutSet) = setDao.deleteSet(set)
}

