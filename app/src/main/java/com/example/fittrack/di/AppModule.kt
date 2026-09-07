package com.example.fittrack.di

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import org.koin.dsl.module
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.fittrack.R
import com.example.fittrack.data.insertInitialExercises
import com.example.fittrack.data.local.FitTrackDatabase
import com.example.fittrack.data.model.ExerciseRepository
import com.example.fittrack.data.model.SetRepository
import com.example.fittrack.data.model.WorkoutRepository
import com.example.fittrack.data.model.WorkoutSessionRepository
import com.example.fittrack.model.Exercise
import com.example.fittrack.model.MuscleGroup
import com.example.fittrack.ui.screens.exercises.ExerciseDetailViewModel
import com.example.fittrack.ui.screens.exercises.ExercisesViewModel
import com.example.fittrack.ui.screens.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import java.util.UUID

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            FitTrackDatabase::class.java,
            "fittrack_database"
        )
            .fallbackToDestructiveMigration(true)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    insertInitialExercises(db)
                }
            })
            .build()
    }

    single { get<FitTrackDatabase>().exerciseDao() }
    single { ExerciseRepository(get()) }

    single { get<FitTrackDatabase>().setDao() }
    single { SetRepository(get()) }

    single { get<FitTrackDatabase>().workoutDao() }
    single { WorkoutRepository(get()) }

    single { get<FitTrackDatabase>().workoutSessionDao() }
    single { WorkoutSessionRepository(get()) }

    viewModelOf(::ExercisesViewModel)
    viewModelOf(::ExerciseDetailViewModel)
    viewModelOf(::HomeViewModel)
}
