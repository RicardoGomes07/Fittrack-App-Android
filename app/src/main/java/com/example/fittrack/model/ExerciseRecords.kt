package com.example.fittrack.model

/** Personal records for one exercise, derived from completed sets. */
data class ExerciseRecords(
    val bestWeight: Double,
    val bestWeightReps: Int,
    val bestWeightDate: Long,
    val estimatedOneRepMax: Double
)

/** Epley formula. A single rep is the weight itself. */
fun estimateOneRepMax(weight: Double, reps: Int): Double =
    if (reps <= 1) weight else weight * (1 + reps / 30.0)

/**
 * Heaviest set wins (ties go to more reps, then the most recent). Sets without reps are ignored.
 * Returns null when there is nothing to report.
 */
fun List<SetHistory>.toExerciseRecords(): ExerciseRecords? {
    val valid = filter { it.reps > 0 }
    if (valid.isEmpty()) return null
    val best = valid.maxWith(compareBy<SetHistory>({ it.weight }, { it.reps }, { it.startedAt }))
    return ExerciseRecords(
        bestWeight = best.weight,
        bestWeightReps = best.reps,
        bestWeightDate = best.startedAt,
        estimatedOneRepMax = valid.maxOf { estimateOneRepMax(it.weight, it.reps) }
    )
}
