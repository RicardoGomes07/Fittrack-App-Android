package com.example.fittrack.ui.screens.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.fittrack.model.Exercise

@Composable
fun ExercisesList(
    exercises: List<Exercise>,
    modifier: Modifier = Modifier,
    onExerciseClick: (String) -> Unit
){
    LazyColumn(modifier = modifier) {
        items(exercises) { exercise ->
            ExerciseItem(
                exercise = exercise,
                onExerciseClick = onExerciseClick
            )
        }
    }
}

