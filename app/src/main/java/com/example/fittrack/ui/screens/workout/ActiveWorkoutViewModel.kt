package com.example.fittrack.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrack.data.AuthManager
import com.example.fittrack.data.model.ExerciseRepository
import com.example.fittrack.data.model.SetRepository
import com.example.fittrack.data.model.WorkoutSessionRepository
import com.example.fittrack.model.User
import com.example.fittrack.model.WorkoutSession
import com.example.fittrack.model.WorkoutSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Drives the live workout. The session and its sets are written to Room as they change, so an
 * in-progress workout survives process death; the elapsed time is derived from the stored start time.
 * The rest timer is in-memory only.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ActiveWorkoutViewModel(
    private val sessionRepository: WorkoutSessionRepository,
    private val setRepository: SetRepository,
    private val exerciseRepository: ExerciseRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val restEndsAt = MutableStateFlow<Long?>(null)
    private val restDurationSeconds = MutableStateFlow(DEFAULT_REST_SECONDS)
    private var handledRouteExerciseId: String? = null

    private val activeSession: Flow<WorkoutSession?> = authManager.currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(null) else sessionRepository.getActiveSession(user.id)
        }

    private val activeSets: Flow<List<WorkoutSet>> = activeSession
        .flatMapLatest { session ->
            if (session == null) flowOf(emptyList()) else setRepository.getSetsForSession(session.id)
        }

    private val ticker: Flow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000)
        }
    }

    val uiState: StateFlow<ActiveWorkoutUiState> = combine(
        authManager.isInitialized,
        activeSession,
        activeSets,
        exerciseRepository.getExercises(),
        combine(ticker, restEndsAt) { now, endsAt -> now to endsAt }
    ) { initialized, session, sets, exercises, (now, endsAt) ->
        val logs = buildExerciseLogs(sets, exercises)
        val inWorkout = logs.map { it.exercise.id }.toSet()
        ActiveWorkoutUiState(
            isLoading = !initialized,
            hasSession = session != null,
            elapsedSeconds = session?.let { ((now - it.startedAt) / 1000).coerceAtLeast(0) } ?: 0,
            exercises = logs,
            availableExercises = exercises.filter { it.id !in inWorkout }.sortedBy { it.name },
            restRemainingSeconds = restRemainingSeconds(endsAt, now)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ActiveWorkoutUiState())

    fun startWorkout() {
        viewModelScope.launch { ensureSession() }
    }

    /** Entry from "Add to Today's Workout". Handled once per route argument so recomposition can't repeat it. */
    fun addExerciseFromRoute(exerciseId: String) {
        if (exerciseId == handledRouteExerciseId) return
        handledRouteExerciseId = exerciseId
        runCatching { UUID.fromString(exerciseId) }.getOrNull()?.let(::addExercise)
    }

    fun addExercise(exerciseId: UUID) {
        viewModelScope.launch {
            val session = ensureSession() ?: return@launch
            appendSet(session, exerciseId, onlyIfAbsent = true)
        }
    }

    fun addSet(exerciseId: UUID) {
        viewModelScope.launch {
            val session = currentSession() ?: return@launch
            appendSet(session, exerciseId)
        }
    }

    fun updateSetValues(setId: UUID, weight: Double, reps: Int) {
        viewModelScope.launch { setRepository.updateValues(setId, weight, reps) }
    }

    fun toggleSetCompleted(set: WorkoutSet) {
        viewModelScope.launch {
            val completed = !set.isCompleted
            setRepository.updateCompleted(set.id, completed)
            if (completed) restEndsAt.value = System.currentTimeMillis() + restDurationSeconds.value * 1000L
        }
    }

    fun deleteSet(set: WorkoutSet) {
        viewModelScope.launch { setRepository.deleteSet(set) }
    }

    fun removeExercise(exerciseId: UUID) {
        viewModelScope.launch {
            val session = currentSession() ?: return@launch
            setRepository.deleteSetsForExercise(session.id, exerciseId)
        }
    }

    /** Shifts the running rest timer, and the length used for the next rest, by [deltaSeconds]. */
    fun adjustRest(deltaSeconds: Int) {
        restDurationSeconds.update { (it + deltaSeconds).coerceAtLeast(REST_STEP_SECONDS) }
        restEndsAt.update { it?.plus(deltaSeconds * 1000L) }
    }

    fun skipRest() {
        restEndsAt.value = null
    }

    /** Keeps completed sets only. A workout with none is discarded rather than saved empty. */
    fun finishWorkout(onDone: () -> Unit) {
        viewModelScope.launch {
            val session = currentSession()
            if (session != null) {
                setRepository.deleteIncompleteSets(session.id)
                if (setRepository.getSetsForSession(session.id).first().isEmpty()) {
                    sessionRepository.deleteWorkoutSession(session)
                } else {
                    sessionRepository.updateWorkoutSession(session.copy(endedAt = System.currentTimeMillis()))
                }
            }
            restEndsAt.value = null
            onDone()
        }
    }

    fun discardWorkout(onDone: () -> Unit) {
        viewModelScope.launch {
            currentSession()?.let { sessionRepository.deleteWorkoutSession(it) }
            restEndsAt.value = null
            onDone()
        }
    }

    private suspend fun awaitUser(): User? {
        authManager.isInitialized.first { it }
        return authManager.currentUser.value
    }

    private suspend fun currentSession(): WorkoutSession? {
        val user = awaitUser() ?: return null
        return sessionRepository.getActiveSession(user.id).first()
    }

    private suspend fun ensureSession(): WorkoutSession? {
        val user = awaitUser() ?: return null
        return sessionRepository.getActiveSession(user.id).first()
            ?: WorkoutSession(userId = user.id, startedAt = System.currentTimeMillis())
                .also { sessionRepository.insertWorkoutSession(it) }
    }

    /** Adds a set after the session's last one, prefilled from the exercise's previous set. */
    private suspend fun appendSet(session: WorkoutSession, exerciseId: UUID, onlyIfAbsent: Boolean = false) {
        val sets = setRepository.getSetsForSession(session.id).first()
        val previous = sets.lastOrNull { it.exerciseId == exerciseId }
        if (onlyIfAbsent && previous != null) return
        setRepository.insertSet(
            WorkoutSet(
                exerciseId = exerciseId,
                sessionId = session.id,
                reps = previous?.reps ?: 0,
                weight = previous?.weight ?: 0.0,
                position = (sets.maxOfOrNull { it.position } ?: -1) + 1
            )
        )
    }
}
