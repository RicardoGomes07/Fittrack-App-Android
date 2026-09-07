package com.example.fittrack.ui.screens.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrack.data.model.ExerciseRepository
import com.example.fittrack.model.Exercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ExerciseDetailViewModel(
    private val exerciseRepository: ExerciseRepository
): ViewModel() {

    private val _exercise = MutableStateFlow<Exercise?>(null)
    val exercise: StateFlow<Exercise?> = _exercise

    fun loadExercise(id: String) {
        viewModelScope.launch {
            try {
                val uuid = UUID.fromString(id)
                _exercise.value = exerciseRepository.getExerciseById(uuid)
            } catch (e: Exception) {
                // Handle invalid UUID
            }
        }
    }
}
