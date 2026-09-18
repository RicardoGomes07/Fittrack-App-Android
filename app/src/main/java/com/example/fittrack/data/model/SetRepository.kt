package com.example.fittrack.data.model

import com.example.fittrack.data.local.SetDao

import com.example.fittrack.model.SetHistory
import com.example.fittrack.model.WorkoutSet
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class SetRepository (
    private val setDao: SetDao
){
    fun getSetsForSession(sessionId: UUID): Flow<List<WorkoutSet>> = setDao.getSetsForSession(sessionId)

    fun getSetHistory(userId: UUID, exerciseId: UUID): Flow<List<SetHistory>> = setDao.getSetHistory(userId, exerciseId)

    suspend fun insertSet(set: WorkoutSet) = setDao.insertSet(set)

    suspend fun updateSet(set: WorkoutSet) = setDao.updateSet(set)

    suspend fun updateValues(id: UUID, weight: Double, reps: Int) = setDao.updateValues(id, weight, reps)

    suspend fun updateCompleted(id: UUID, completed: Boolean) = setDao.updateCompleted(id, completed)

    suspend fun deleteSet(set: WorkoutSet) = setDao.deleteSet(set)

    suspend fun deleteSetsForExercise(sessionId: UUID, exerciseId: UUID) = setDao.deleteSetsForExercise(sessionId, exerciseId)

    suspend fun deleteIncompleteSets(sessionId: UUID) = setDao.deleteIncompleteSets(sessionId)
}
