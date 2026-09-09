package com.example.fittrack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fittrack.model.*

@Database(entities = [Exercise::class, WorkoutSession::class, WorkoutSet::class, Workout::class, User::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FitTrackDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun setDao(): SetDao
    abstract fun userDao(): UserDao
}
