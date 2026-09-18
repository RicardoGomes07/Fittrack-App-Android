package com.example.fittrack.ui

import com.example.fittrack.model.Equipment
import com.example.fittrack.model.Exercise
import com.example.fittrack.model.ExerciseType
import com.example.fittrack.model.MuscleGroup
import com.example.fittrack.model.WorkoutSet
import com.example.fittrack.ui.screens.workout.buildExerciseLogs
import com.example.fittrack.ui.screens.workout.formatDuration
import com.example.fittrack.ui.screens.workout.formatWeight
import com.example.fittrack.ui.screens.workout.restRemainingSeconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

class ActiveWorkoutLogicTest {

    private val session = UUID.randomUUID()

    private fun exercise(name: String) = Exercise(
        name = name,
        muscleGroup = MuscleGroup.CHEST,
        exerciseType = ExerciseType.WEIGHT_REPS,
        equipment = Equipment.BARBELL
    )

    private fun set(exercise: Exercise, position: Int) =
        WorkoutSet(exerciseId = exercise.id, sessionId = session, reps = 5, weight = 50.0, position = position)

    @Test
    fun logs_groupSetsByExerciseInOrderOfFirstAppearance() {
        val bench = exercise("Bench")
        val squat = exercise("Squat")
        // Interleaved: bench, squat, bench
        val sets = listOf(set(bench, 0), set(squat, 1), set(bench, 2))

        val logs = buildExerciseLogs(sets, listOf(squat, bench))

        assertEquals(listOf("Bench", "Squat"), logs.map { it.exercise.name })
        assertEquals(listOf(0, 2), logs[0].sets.map { it.position })
        assertEquals(listOf(1), logs[1].sets.map { it.position })
    }

    @Test
    fun logs_sortByPositionEvenIfSetsArriveUnordered() {
        val bench = exercise("Bench")
        val squat = exercise("Squat")
        val logs = buildExerciseLogs(listOf(set(squat, 3), set(bench, 1)), listOf(bench, squat))
        assertEquals(listOf("Bench", "Squat"), logs.map { it.exercise.name })
    }

    @Test
    fun logs_skipSetsWhoseExerciseIsUnknown() {
        val bench = exercise("Bench")
        val ghost = exercise("Ghost")
        val logs = buildExerciseLogs(listOf(set(bench, 0), set(ghost, 1)), listOf(bench))
        assertEquals(listOf("Bench"), logs.map { it.exercise.name })
    }

    @Test
    fun rest_isNullWhenNotRunningOrAlreadyOver() {
        assertNull(restRemainingSeconds(null, 1_000))
        assertNull(restRemainingSeconds(1_000, 1_000))
        assertNull(restRemainingSeconds(1_000, 5_000))
    }

    @Test
    fun rest_roundsPartialSecondsUp() {
        assertEquals(1, restRemainingSeconds(1_000, 0))
        assertEquals(2, restRemainingSeconds(1_001, 0))
        assertEquals(90, restRemainingSeconds(90_000, 0))
        assertEquals(90, restRemainingSeconds(90_000, 1)) // 89.999s left still shows 90
    }

    @Test
    fun duration_formatsMinutesAndHours() {
        assertEquals("00:00", formatDuration(0))
        assertEquals("01:05", formatDuration(65))
        assertEquals("59:59", formatDuration(3599))
        assertEquals("1:00:00", formatDuration(3600))
        assertEquals("2:03:04", formatDuration(7384))
    }

    @Test
    fun duration_neverGoesNegative() {
        assertEquals("00:00", formatDuration(-5))
    }

    @Test
    fun weight_dropsTrailingZeroOnWholeNumbers() {
        assertEquals("5", formatWeight(5.0))
        assertEquals("62.5", formatWeight(62.5))
        assertEquals("0", formatWeight(0.0))
    }
}
