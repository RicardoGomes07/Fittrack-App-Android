package com.example.fittrack.model

import java.util.UUID

/** Aggregates of a finished session, counting completed sets only. Times are epoch millis. */
data class SessionSummary(
    val id: UUID,
    val startedAt: Long,
    val endedAt: Long?,
    val exerciseCount: Int,
    val setCount: Int,
    val volume: Double
)
