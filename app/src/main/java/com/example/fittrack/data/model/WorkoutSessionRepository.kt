package com.example.fittrack.data.model

import com.example.fittrack.data.local.WorkoutSessionDao
import com.example.fittrack.model.SessionSummary
import com.example.fittrack.model.WorkoutSession
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class WorkoutSessionRepository (
    private val workoutSessionDao: WorkoutSessionDao
){
    fun getAllWorkoutSessions(): Flow<List<WorkoutSession>> = workoutSessionDao.getAllWorkoutSessions()

    fun getActiveSession(userId: UUID): Flow<WorkoutSession?> = workoutSessionDao.getActiveSession(userId)

    fun getRecentSessionSummaries(userId: UUID, limit: Int): Flow<List<SessionSummary>> =
        workoutSessionDao.getRecentSessionSummaries(userId, limit)

    suspend fun insertWorkoutSession(session: WorkoutSession) = workoutSessionDao.insertWorkoutSession(session)

    suspend fun updateWorkoutSession(session: WorkoutSession) = workoutSessionDao.updateWorkoutSession(session)

    suspend fun deleteWorkoutSession(session: WorkoutSession) = workoutSessionDao.deleteWorkoutSession(session)
}
