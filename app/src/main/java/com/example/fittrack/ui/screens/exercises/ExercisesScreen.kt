package com.example.fittrack.ui.screens.exercises

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fittrack.R
import com.example.fittrack.model.Equipment
import com.example.fittrack.model.Exercise
import com.example.fittrack.model.ExerciseType
import com.example.fittrack.model.MuscleGroup
import com.example.fittrack.model.User
import com.example.fittrack.ui.screens.components.home.FitTrackBottomNav
import com.example.fittrack.ui.screens.components.exercise.ExercisesList
import com.example.fittrack.ui.theme.FitTrackColors
import com.example.fittrack.ui.theme.FitTrackTheme
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@Composable
fun ExercisesScreen(
    onExerciseClick: (String) -> Unit,
    onNavItemSelected: (String) -> Unit = {},
    onAddExerciseClick: () -> Unit = {},
    viewModel: ExercisesViewModel = koinViewModel()
) {
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()

    ExercisesContent(
        loggedInUser = loggedInUser,
        exercises = exercises,
        onExerciseClick = onExerciseClick,
        onNavItemSelected = onNavItemSelected,
        onAddExerciseClick = onAddExerciseClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesContent(
    loggedInUser: User?,
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
            if(loggedInUser != null){
                FloatingActionButton(onClick = onAddExerciseClick) {
                    Icon(Icons.Default.Add, contentDescription = "Add Exercise")
                }
            }
        }
    ) { paddingValues ->
        if (exercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = FitTrackColors.Primary)
                    Spacer(Modifier.height(16.dp))
                    Text("Loading exercises...", color = FitTrackColors.OnSurfaceVariant)
                }
            }
        } else {
            ExercisesList(
                exercises = exercises,
                onExerciseClick = onExerciseClick,
                modifier = Modifier.padding(paddingValues)
            )
        }
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
            description = "Foundational upper-body horizontal push strength standard",
            imageRes = R.drawable.ic_chest_press,
            exerciseType = ExerciseType.WEIGHT_REPS,
            equipment = Equipment.BARBELL
        )
    )
    FitTrackTheme {
        ExercisesContent(
            exercises = sampleExercises,
            onExerciseClick = {},
            onNavItemSelected = {},
            onAddExerciseClick = {},
            loggedInUser = null
        )
    }
}
