package com.example.fittrack.ui.screens.exercises

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fittrack.R
import com.example.fittrack.model.Exercise
import com.example.fittrack.model.MuscleGroup
import com.example.fittrack.ui.screens.components.FitTrackBottomNav
import com.example.fittrack.ui.screens.components.ExercisesList
import com.example.fittrack.ui.theme.FitTrackTheme
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@Composable
fun ExercisesScreen(
    onExerciseClick: (String) -> Unit,
    onNavItemSelected: (String) -> Unit = {},
    viewModel: ExercisesViewModel = koinViewModel()
) {
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()

    ExercisesContent(
        exercises = exercises,
        onExerciseClick = onExerciseClick,
        onNavItemSelected = onNavItemSelected,
        onAddExerciseClick = { /* TODO: Add exercise */ }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesContent(
    exercises: List<Exercise>,
    onExerciseClick: (String) -> Unit,
    onNavItemSelected: (String) -> Unit,
    onAddExerciseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Exercises") })
        },
        bottomBar = {
            FitTrackBottomNav(
                selectedItem = "exercises",
                onItemSelected = onNavItemSelected
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExerciseClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Exercise")
            }
        }
    ) { paddingValues ->
        ExercisesList(
            exercises = exercises,
            onExerciseClick = onExerciseClick,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExercisesScreenPreview() {
    val sampleExercises = listOf(
        Exercise(
            id = UUID.randomUUID(),
            name = "Bench Press",
            muscleGroup = MuscleGroup.CHEST,
            imageRes = R.drawable.ic_chest_press
        ),
        Exercise(
            id = UUID.randomUUID(),
            name = "Squat",
            muscleGroup = MuscleGroup.LEGS,
            imageRes = R.drawable.ic_barbell_squat
        ),
        Exercise(
            id = UUID.randomUUID(),
            name = "Deadlift",
            muscleGroup = MuscleGroup.BACK,
            imageRes = R.drawable.ic_deadlift
        )
    )
    FitTrackTheme {
        ExercisesContent(
            exercises = sampleExercises,
            onExerciseClick = {},
            onNavItemSelected = {},
            onAddExerciseClick = {}
        )
    }
}
