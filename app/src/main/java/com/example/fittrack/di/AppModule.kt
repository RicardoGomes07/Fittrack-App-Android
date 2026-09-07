package com.example.fittrack.di

import org.koin.dsl.module
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.fittrack.R
import com.example.fittrack.data.local.FitTrackDatabase
import com.example.fittrack.data.model.ExerciseRepository
import com.example.fittrack.data.model.SetRepository
import com.example.fittrack.data.model.WorkoutRepository
import com.example.fittrack.data.model.WorkoutSessionRepository
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
                    db.execSQL(
                        """
                        INSERT OR IGNORE INTO exercises (id, name, muscleGroup, imgRes) VALUES
                        ('${UUID.randomUUID()}', 'Chest Press', 'CHEST', ${R.drawable.ic_chest_press}),
                        ('${UUID.randomUUID()}', 'Incline Chest Press', 'CHEST', ${R.drawable.ic_incline_chest_press}),
                        ('${UUID.randomUUID()}', 'Lat Pulldown', 'BACK', ${R.drawable.ic_lat_pulldown}),
                        ('${UUID.randomUUID()}', 'Bent-Over Row', 'BACK', ${R.drawable.ic_bent_over_row}),
                        ('${UUID.randomUUID()}', 'Pull-ups', 'BACK', ${R.drawable.ic_pull_ups}),
                        ('${UUID.randomUUID()}', 'Barbell Squat', 'LEGS', ${R.drawable.ic_barbell_squat}),
                        ('${UUID.randomUUID()}', 'Leg Press', 'LEGS', ${R.drawable.ic_leg_press}),
                        ('${UUID.randomUUID()}', 'Deadlift', 'LEGS', ${R.drawable.ic_deadlift}),
                        ('${UUID.randomUUID()}', 'Overhead Shoulder Press', 'SHOULDERS', ${R.drawable.ic_overhead_shoulder_press}),
                        ('${UUID.randomUUID()}', 'Lateral Raises', 'SHOULDERS', ${R.drawable.ic_lateral_raises}),
                        ('${UUID.randomUUID()}', 'Barbell Bicep Curl', 'ARMS', ${R.drawable.ic_barbell_bicep_curl}),
                        ('${UUID.randomUUID()}', 'Tricep Pushdown', 'ARMS', ${R.drawable.ic_tricep_pushdown}),
                        ('${UUID.randomUUID()}', 'Plank', 'ABS', ${R.drawable.ic_plank})
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
    viewModelOf(::HomeViewModel)
}
