package com.example.fittrack.data.local

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.example.fittrack.model.Exercise
import com.example.fittrack.model.WorkoutSession
import com.example.fittrack.model.Set

@Database(entities = [Exercise::class, WorkoutSession::class, Set::class], version = 1, exportSchema = false)
//@TypeConverters(Converters::class)
abstract class FitTrackDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun setDao(): SetDao
}
