package com.example.fittrack.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * One set of an exercise inside a session. [position] orders sets across the whole session, so an
 * exercise's place in the workout is the position of its first set.
 */
@Entity(
    tableName = "sets",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = WorkoutSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseId"), Index("sessionId")]
)
data class WorkoutSet(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val exerciseId: UUID,
    val sessionId: UUID,
    val reps: Int,
    val weight: Double,
    val position: Int,
    val isCompleted: Boolean = false,
)
