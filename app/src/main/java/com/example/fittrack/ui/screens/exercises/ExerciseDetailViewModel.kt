package com.example.fittrack.ui.screens.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrack.data.AuthManager
import com.example.fittrack.data.model.ExerciseRepository
import com.example.fittrack.data.model.SetRepository
import com.example.fittrack.model.Exercise
import com.example.fittrack.model.ExerciseRecords
import com.example.fittrack.model.toExerciseRecords
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ExerciseDetailViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val authManager: AuthManager,
    private val setRepository: SetRepository
): ViewModel() {

    val loggedInUser = authManager.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    private val _exercise = MutableStateFlow<Exercise?>(null)
    val exercise: StateFlow<Exercise?> = _exercise

    /** Personal records for this exercise from the logged-in user's completed sets. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val records: StateFlow<ExerciseRecords?> = combine(_exercise, authManager.currentUser) { exercise, user -> exercise to user }
        .flatMapLatest { (exercise, user) ->
            if (exercise == null || user == null) flowOf(emptyList())
            else setRepository.getSetHistory(user.id, exercise.id)
        }
        .map { it.toExerciseRecords() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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
