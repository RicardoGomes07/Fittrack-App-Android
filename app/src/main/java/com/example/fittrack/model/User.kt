package com.example.fittrack.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val nickname: String,
    val password: String,
    val weight: Double = 0.0,
    val height: Int = 0,
    val birthDate: LocalDate? = null,
    val memberSince: LocalDate = LocalDate.now(),
    val goalWeight: Double? = null,
    val gender: Gender = Gender.OTHER,
    val level: Int = 1,
    val xp: Int = 0
)
