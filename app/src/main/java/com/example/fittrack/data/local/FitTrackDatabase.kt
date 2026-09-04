package com.example.fittrack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fittrack.model.Exercise
import com.example.fittrack.model.WorkoutSession
import com.example.fittrack.model.Set
import com.example.fittrack.model.Workout

@Database(entities = [Exercise::class, WorkoutSession::class, Set::class, Workout::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FitTrackDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun setDao(): SetDao
}
