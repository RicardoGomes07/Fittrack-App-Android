package com.example.fittrack.ui.screens.exercises

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fittrack.R
import com.example.fittrack.model.Exercise
import com.example.fittrack.model.MuscleGroup
import com.example.fittrack.ui.theme.FitTrackTheme
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@Composable
fun ExerciseDetailsScreen(
    exerciseId: String?,
    onBack: () -> Unit,
    viewModel: ExerciseDetailViewModel = koinViewModel()
){
    LaunchedEffect(exerciseId) {
        exerciseId?.let { viewModel.loadExercise(it) }
    }

    val exercise by viewModel.exercise.collectAsStateWithLifecycle()

    ExerciseDetailsContent(
        exercise = exercise,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailsContent(
    exercise: Exercise?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Exercise Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            exercise?.let {
                Image(
                    painter = painterResource(id = it.imageRes ?: 0),
                    contentDescription = it.name,
                    modifier = Modifier
                        .size(200.dp)
                        .padding(bottom = 24.dp)
                )
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = it.muscleGroup.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            } ?: Text("Loading...")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExerciseDetailsScreenPreview() {
    val sampleExercise = Exercise(
        id = UUID.randomUUID(),
        name = "Bench Press",
        muscleGroup = MuscleGroup.CHEST,
        imageRes = R.drawable.ic_chest_press
    )
    FitTrackTheme {
        ExerciseDetailsContent(
            exercise = sampleExercise,
            onBack = {}
        )
    }
}
