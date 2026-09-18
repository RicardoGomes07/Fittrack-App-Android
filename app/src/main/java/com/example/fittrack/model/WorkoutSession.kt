package com.example.fittrack.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/** A workout being (or already) performed. It is active while [endedAt] is null. Times are epoch millis. */
@Entity(
    tableName = "workout_sessions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class WorkoutSession(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val startedAt: Long,
    val endedAt: Long? = null,
)
