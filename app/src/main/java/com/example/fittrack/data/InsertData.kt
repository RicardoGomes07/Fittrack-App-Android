package com.example.fittrack.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.fittrack.R
import com.example.fittrack.model.Exercise
import com.example.fittrack.model.ExerciseType
import com.example.fittrack.model.MuscleGroup

fun insertInitialExercises(db: SupportSQLiteDatabase) {
    val exerciseList = listOf(
        Exercise(
            name = "Chest Press",
            muscleGroup = MuscleGroup.CHEST,
            description = "Foundational upper-body horizontal push strength standard",
            imageRes = R.drawable.ic_chest_press,
            exerciseType = ExerciseType.WEIGHT
        ),
        Exercise(
            name = "Incline Chest Press",
            muscleGroup = MuscleGroup.CHEST,
            description = "Targets the upper pectorals for a well-rounded chest.",
            imageRes = R.drawable.ic_incline_chest_press,
            exerciseType = ExerciseType.WEIGHT
        ),
        Exercise(
            name = "Lat Pulldown",
            muscleGroup = MuscleGroup.BACK,
            description = "Excellent exercise for building back width.",
            imageRes = R.drawable.ic_lat_pulldown,
            exerciseType = ExerciseType.WEIGHT
        ),
        Exercise(
            name = "Bent-Over Row",
            muscleGroup = MuscleGroup.BACK,
            description = "Classic movement for back thickness and strength.",
            imageRes = R.drawable.ic_bent_over_row,
            exerciseType = ExerciseType.WEIGHT
        ),
        Exercise(
            name = "Pull-ups",
            muscleGroup = MuscleGroup.BACK,
            description = "The ultimate bodyweight test for upper body strength.",
            imageRes = R.drawable.ic_pull_ups,
            exerciseType = ExerciseType.BODYWEIGHT
        ),
        Exercise(
            name = "Barbell Squat",
            muscleGroup = MuscleGroup.LEGS,
            description = "The king of lower body exercises.",
            imageRes = R.drawable.ic_barbell_squat,
            exerciseType = ExerciseType.WEIGHT
        ),
        Exercise(
            name = "Leg Press",
            muscleGroup = MuscleGroup.LEGS,
            description = "Focuses on the quads with reduced lower back strain.",
            imageRes = R.drawable.ic_leg_press,
            exerciseType = ExerciseType.WEIGHT
        ),
        Exercise(
            name = "Deadlift",
            muscleGroup = MuscleGroup.LEGS,
            description = "Fundamental compound movement for overall power.",
            imageRes = R.drawable.ic_deadlift,
            exerciseType = ExerciseType.WEIGHT
        ),
        Exercise(
            name = "Overhead Shoulder Press",
            muscleGroup = MuscleGroup.SHOULDERS,
            description = "Key exercise for shoulder strength and stability.",
            imageRes = R.drawable.ic_overhead_shoulder_press,
            exerciseType = ExerciseType.WEIGHT
        ),
        Exercise(
            name = "Lateral Raises",
            muscleGroup = MuscleGroup.SHOULDERS,
            description = "Isolates the medial deltoid for broader shoulders.",
            imageRes = R.drawable.ic_lateral_raises,
            exerciseType = ExerciseType.WEIGHT
        ),
        Exercise(
            name = "Barbell Bicep Curl",
            muscleGroup = MuscleGroup.ARMS,
            description = "Classic bicep builder.",
            imageRes = R.drawable.ic_barbell_bicep_curl,
            exerciseType = ExerciseType.WEIGHT
        ),
        Exercise(
            name = "Tricep Pushdown",
            muscleGroup = MuscleGroup.ARMS,
            description = "Effective isolation for the triceps.",
            imageRes = R.drawable.ic_tricep_pushdown,
            exerciseType = ExerciseType.WEIGHT
        ),
        Exercise(
            name = "Plank",
            muscleGroup = MuscleGroup.ABS,
            description = "Core stability and isometric strength.",
            imageRes = R.drawable.ic_plank,
            exerciseType = ExerciseType.BODYWEIGHT
        ),
        Exercise(
            name = "Push-ups",
            muscleGroup = MuscleGroup.CHEST,
            exerciseType = ExerciseType.BODYWEIGHT,
            description = "Classic bodyweight horizontal push for chest, shoulders, and triceps.",
            imageRes = R.drawable.ic_plank
        ),
        Exercise(
            name = "Dips (Chest Focus)",
            muscleGroup = MuscleGroup.CHEST,
            exerciseType = ExerciseType.BODYWEIGHT,
            description = "Bodyweight compound movement targeting the lower pectorals and triceps with a slight forward lean.",
            imageRes = R.drawable.ic_plank
        ),
        Exercise(
            name = "Hanging Leg Raise",
            muscleGroup = MuscleGroup.ABS,
            exerciseType = ExerciseType.BODYWEIGHT,
            description = "Advanced core movement targeting lower rectus abdominis and hip flexors.",
            imageRes = R.drawable.ic_plank
        ),
        Exercise(
            name = "Bulgarian Split Squat",
            muscleGroup = MuscleGroup.LEGS,
            description = "Unilateral squat targeting quads and glutes while fixing strength imbalances.",
            imageRes = R.drawable.ic_plank,
            exerciseType = ExerciseType.WEIGHT
        ),
        Exercise(
            name = "Standing Calf Raise",
            muscleGroup = MuscleGroup.LEGS,
            description = "Isolates the gastrocnemius muscle through full ankle plantarflexion.",
            imageRes = R.drawable.ic_plank,
            exerciseType = ExerciseType.BODYWEIGHT
        )
    )

    db.beginTransaction()
    try {
        for (exercise in exerciseList) {
            val values = ContentValues().apply {
                put("id", exercise.id.toString())
                put("name", exercise.name)
                put("muscleGroup", exercise.muscleGroup.name)
                put("description", exercise.description)
                put("imageRes", exercise.imageRes)
                put("exerciseType", exercise.exerciseType.name)
            }
            db.insert("exercises", SQLiteDatabase.CONFLICT_IGNORE, values)
        }
        db.setTransactionSuccessful()
    } finally {
        db.endTransaction()
    }
}