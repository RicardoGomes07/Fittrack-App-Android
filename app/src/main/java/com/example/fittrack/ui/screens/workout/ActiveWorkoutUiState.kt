package com.example.fittrack.ui.screens.workout

import com.example.fittrack.model.Exercise
import com.example.fittrack.model.WorkoutSet

const val DEFAULT_REST_SECONDS = 90
const val REST_STEP_SECONDS = 15

data class ExerciseLog(
    val exercise: Exercise,
    val sets: List<WorkoutSet>
)

data class ActiveWorkoutUiState(
    val isLoading: Boolean = true,
    val hasSession: Boolean = false,
    val elapsedSeconds: Long = 0,
    val exercises: List<ExerciseLog> = emptyList(),
    /** Exercises not yet in the workout, for the picker. */
    val availableExercises: List<Exercise> = emptyList(),
    /** Seconds left on the rest timer, or null when no rest is running. */
    val restRemainingSeconds: Int? = null
)

/** Groups sets by exercise, keeping the order in which each exercise first appears. */
fun buildExerciseLogs(sets: List<WorkoutSet>, exercises: List<Exercise>): List<ExerciseLog> {
    val byId = exercises.associateBy { it.id }
    return sets.sortedBy { it.position }
        .groupBy { it.exerciseId }
        .mapNotNull { (exerciseId, group) -> byId[exerciseId]?.let { ExerciseLog(it, group) } }
}

/** Whole seconds left until [restEndsAt] (rounded up), or null when there is no running rest. */
fun restRemainingSeconds(restEndsAt: Long?, now: Long): Int? {
    if (restEndsAt == null) return null
    val remainingMs = restEndsAt - now
    return if (remainingMs <= 0) null else ((remainingMs + 999) / 1000).toInt()
}
