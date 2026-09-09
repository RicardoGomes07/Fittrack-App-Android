package com.example.fittrack.ui.screens.exercises

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fittrack.model.*
import com.example.fittrack.ui.theme.FitTrackColors
import com.example.fittrack.ui.theme.FitTrackTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseAddScreen(
    onBack: () -> Unit,
    viewModel: ExercisesViewModel = koinViewModel()
) {
    ExerciseAddContent(
        onBack = onBack,
        onAddExercise = { input ->
            viewModel.addExercise(input)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseAddContent(
    onBack: () -> Unit,
    onAddExercise: (ExerciseInput) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedMuscleGroup by remember { mutableStateOf(MuscleGroup.CHEST) }
    var selectedType by remember { mutableStateOf(ExerciseType.WEIGHT_REPS) }
    var selectedEquipment by remember { mutableStateOf(Equipment.BARBELL) }

    Scaffold(
        containerColor = FitTrackColors.Background,
        topBar = {
            TopAppBar(
                title = { Text("Add Exercise", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FitTrackColors.Background,
                    titleContentColor = FitTrackColors.OnSurface,
                    navigationIconContentColor = FitTrackColors.OnSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onAddExercise(
                            ExerciseInput(
                                name = name,
                                muscleGroup = selectedMuscleGroup,
                                exerciseType = selectedType,
                                equipment = selectedEquipment,
                                description = description
                            )
                        )
                        onBack()
                    }
                },
                containerColor = FitTrackColors.Primary,
                contentColor = FitTrackColors.OnPrimary
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save Exercise")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Exercise Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FitTrackColors.Primary,
                    unfocusedBorderColor = FitTrackColors.Outline,
                    focusedLabelColor = FitTrackColors.Primary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Muscle Group Selector
            SelectorSection(
                title = "Muscle Group",
                options = MuscleGroup.entries,
                selectedOption = selectedMuscleGroup,
                onOptionSelected = { selectedMuscleGroup = it }
            )

            // Exercise Type Selector
            SelectorSection(
                title = "Exercise Type",
                options = ExerciseType.entries,
                selectedOption = selectedType,
                onOptionSelected = { selectedType = it }
            )

            // Equipment Selector
            SelectorSection(
                title = "Equipment",
                options = Equipment.entries,
                selectedOption = selectedEquipment,
                onOptionSelected = { selectedEquipment = it }
            )

            // Description Field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FitTrackColors.Primary,
                    unfocusedBorderColor = FitTrackColors.Outline,
                    focusedLabelColor = FitTrackColors.Primary
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(Modifier.height(80.dp)) // Extra space for FAB
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T : Enum<T>> SelectorSection(
    title: String,
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = FitTrackColors.OnSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selectedOption,
                    onClick = { onOptionSelected(option) },
                    label = { 
                        Text(
                            option.name.replace("_", " ").lowercase()
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FitTrackColors.Primary,
                        selectedLabelColor = FitTrackColors.OnPrimary,
                        containerColor = FitTrackColors.SurfaceContainerHigh,
                        labelColor = FitTrackColors.OnSurfaceVariant
                    ),
                    border = null,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF051424)
@Composable
private fun ExerciseAddScreenPreview() {
    FitTrackTheme {
        ExerciseAddContent(
            onBack = {},
            onAddExercise = {}
        )
    }
}
