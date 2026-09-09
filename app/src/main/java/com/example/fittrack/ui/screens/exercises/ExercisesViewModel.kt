package com.example.fittrack.ui.screens.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrack.data.model.ExerciseRepository
import com.example.fittrack.model.Exercise
import com.example.fittrack.model.ExerciseInput
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ExercisesViewModel(
    private val exerciseRepository: ExerciseRepository
): ViewModel() {

    val exercises: StateFlow<List<Exercise>> = exerciseRepository.getExercises()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addExercise(input: ExerciseInput) {
        viewModelScope.launch {
            try{
                val newExercise = Exercise(
                    id = UUID.randomUUID(),
                    name = input.name,
                    muscleGroup = input.muscleGroup,
                    exerciseType = input.exerciseType,
                    description = input.description,
                    imageRes = input.imageRes,
                    equipment = input.equipment
                )
                exerciseRepository.insertExercise(newExercise)
            } catch (e: Exception) {
                // Handle exceptionI
            }
        }
    }
}
