package com.example.fittrack.di

import org.koin.dsl.module
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.fittrack.data.local.FitTrackDatabase
import com.example.fittrack.data.model.ExerciseRepository
import com.example.fittrack.data.model.SetRepository
import com.example.fittrack.data.model.WorkoutRepository
import com.example.fittrack.data.model.WorkoutSessionRepository
import com.example.fittrack.ui.screens.exercises.ExerciseDetailViewModel
import com.example.fittrack.ui.screens.exercises.ExercisesViewModel
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
                    db.execSQL(
                        """
                        INSERT OR IGNORE INTO exercises (id, name, muscleGroup) VALUES
                        ('${UUID.randomUUID()}', 'Chest Press', 'CHEST'),
                        ('${UUID.randomUUID()}', 'Incline Chest Press', 'CHEST'),
                        ('${UUID.randomUUID()}', 'Lat Pulldown', 'BACK'),
                        ('${UUID.randomUUID()}', 'Bent-Over Row', 'BACK'),
                        ('${UUID.randomUUID()}', 'Pull-ups', 'BACK'),
                        ('${UUID.randomUUID()}', 'Barbell Squat', 'LEGS'),
                        ('${UUID.randomUUID()}', 'Leg Press', 'LEGS'),
                        ('${UUID.randomUUID()}', 'Deadlift', 'LEGS'),
                        ('${UUID.randomUUID()}', 'Overhead Shoulder Press', 'SHOULDERS'),
                        ('${UUID.randomUUID()}', 'Lateral Raises', 'SHOULDERS'),
                        ('${UUID.randomUUID()}', 'Barbell Bicep Curl', 'ARMS'),
                        ('${UUID.randomUUID()}', 'Tricep Pushdown', 'ARMS'),
                        ('${UUID.randomUUID()}', 'Plank', 'ABS')
                        """.trimIndent()
                    )
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
}
