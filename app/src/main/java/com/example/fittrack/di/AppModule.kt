package com.example.fittrack.di

import org.koin.dsl.module
import androidx.room3.Room
import com.example.fittrack.data.local.FitTrackDatabase
import com.example.fittrack.data.model.ExerciseRepository
import com.example.fittrack.data.model.SetRepository
import com.example.fittrack.data.model.WorkoutRepository
import org.koin.android.ext.koin.androidContext

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            FitTrackDatabase::class.java,
            "fittrack_database"
        ).build()
    }
    single { get<FitTrackDatabase>().exerciseDao() }
    single { ExerciseRepository(get()) }

    single { get<FitTrackDatabase>().setDao() }
    single { SetRepository(get()) }

    single { get<FitTrackDatabase>().workoutDao() }
    single { WorkoutRepository(get()) }


    //viewModelOf(::MainViewModel)
}
