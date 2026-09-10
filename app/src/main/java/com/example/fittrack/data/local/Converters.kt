package com.example.fittrack.data.local

import androidx.room.TypeConverter
import com.example.fittrack.model.Equipment
import com.example.fittrack.model.ExerciseType
import com.example.fittrack.model.Gender
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

    @TypeConverter
    fun fromEquipment(value: Equipment): String = value.name

    @TypeConverter
    fun toEquipment(value: String): Equipment = Equipment.valueOf(value)

    @TypeConverter
    fun fromExerciseType(type: ExerciseType): String = type.name

    @TypeConverter
    fun toExerciseType(value: String): ExerciseType = ExerciseType.valueOf(value)

    @TypeConverter
    fun fromGender(gender: Gender): String = gender.name

    @TypeConverter
    fun toGender(value: String): Gender = Gender.valueOf(value)
}
