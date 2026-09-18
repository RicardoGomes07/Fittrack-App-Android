package com.example.fittrack.model

/** A completed set of one exercise together with when its session started (epoch millis). */
data class SetHistory(
    val weight: Double,
    val reps: Int,
    val startedAt: Long
)
