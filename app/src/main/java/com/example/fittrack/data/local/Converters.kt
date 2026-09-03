package com.example.fittrack.data.local

import androidx.room.TypeConverter
import com.example.fittrack.model.MuscleGroup
import java.time.LocalDate
import java.util.UUID

class Converters {
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun toUUID(uuid: String?): UUID? = uuid?.let { UUID.fromString(it) }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? = epochDay?.let { LocalDate.ofEpochDay(it) }

    @TypeConverter
    fun fromMuscleGroup(muscleGroup: MuscleGroup?): String? = muscleGroup?.name

    @TypeConverter
    fun toMuscleGroup(name: String?): MuscleGroup? = name?.let { MuscleGroup.valueOf(it) }

    @TypeConverter
    fun fromUUIDList(list: List<UUID>?): String? = list?.joinToString(",") { it.toString() }

    @TypeConverter
    fun toUUIDList(data: String?): List<UUID>? = data?.split(",")?.filter { it.isNotEmpty() }?.map { UUID.fromString(it) }
}
