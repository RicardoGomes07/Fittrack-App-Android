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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fittrack.model.Exercise
import com.example.fittrack.ui.screens.components.FitTrackBottomNav
import com.example.fittrack.ui.screens.components.ExercisesList
import org.koin.androidx.compose.koinViewModel

@Composable
fun ExercisesScreen(
    onExerciseClick: (String) -> Unit,
    onNavItemSelected: (String) -> Unit = {},
    onAddExerciseClick: () -> Unit = {},
    viewModel: ExercisesViewModel = koinViewModel()
) {
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()

    ExercisesContent(
        exercises = exercises,
        onExerciseClick = onExerciseClick,
        onNavItemSelected = onNavItemSelected,
        onAddExerciseClick = onAddExerciseClick
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
