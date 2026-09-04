package com.example.fittrack.data.model

import com.example.fittrack.data.local.SetDao

import com.example.fittrack.model.Set
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class SetRepository (
    private val setDao: SetDao
){
    fun getSetsForSession(sessionId: UUID): Flow<List<Set>> = setDao.getSetsForSession(sessionId)

    suspend fun insertSet(set: Set) = setDao.insertSet(set)

    suspend fun updateSet(set: Set) = setDao.updateSet(set)

    suspend fun deleteSet(set: Set) = setDao.deleteSet(set)
}
