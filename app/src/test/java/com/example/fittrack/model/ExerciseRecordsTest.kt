package com.example.fittrack.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ExerciseRecordsTest {

    private fun set(weight: Double, reps: Int, startedAt: Long = 0) = SetHistory(weight, reps, startedAt)

    @Test
    fun oneRepMax_singleRepIsTheWeightItself() {
        assertEquals(100.0, estimateOneRepMax(100.0, 1), 0.0001)
    }

    @Test
    fun oneRepMax_usesEpley() {
        // 100 * (1 + 5/30)
        assertEquals(116.6667, estimateOneRepMax(100.0, 5), 0.0001)
    }

    @Test
    fun records_areNullWithoutUsableSets() {
        assertNull(emptyList<SetHistory>().toExerciseRecords())
        assertNull(listOf(set(100.0, 0)).toExerciseRecords())
    }

    @Test
    fun records_bestSetIsTheHeaviest() {
        val records = listOf(set(80.0, 10), set(100.0, 3, startedAt = 5), set(90.0, 6)).toExerciseRecords()
        assertNotNull(records)
        assertEquals(100.0, records!!.bestWeight, 0.0)
        assertEquals(3, records.bestWeightReps)
        assertEquals(5L, records.bestWeightDate)
    }

    @Test
    fun records_tieOnWeightGoesToMoreReps_thenToTheMostRecent() {
        val moreReps = listOf(set(100.0, 3, 1), set(100.0, 5, 2)).toExerciseRecords()!!
        assertEquals(5, moreReps.bestWeightReps)

        val newer = listOf(set(100.0, 5, 1), set(100.0, 5, 9)).toExerciseRecords()!!
        assertEquals(9L, newer.bestWeightDate)
    }

    @Test
    fun records_oneRepMaxCanComeFromALighterHighRepSet() {
        // 100x1 -> 100, but 90x8 -> 114
        val records = listOf(set(100.0, 1), set(90.0, 8)).toExerciseRecords()!!
        assertEquals(100.0, records.bestWeight, 0.0)
        assertEquals(114.0, records.estimatedOneRepMax, 0.0001)
    }

    @Test
    fun records_ignoreSetsWithoutReps() {
        val records = listOf(set(200.0, 0), set(60.0, 5)).toExerciseRecords()!!
        assertEquals(60.0, records.bestWeight, 0.0)
    }

    @Test
    fun records_bodyweightSetsKeepTheHighestReps() {
        val records = listOf(set(0.0, 8), set(0.0, 12), set(0.0, 10)).toExerciseRecords()!!
        assertEquals(0.0, records.bestWeight, 0.0)
        assertEquals(12, records.bestWeightReps)
    }
}
