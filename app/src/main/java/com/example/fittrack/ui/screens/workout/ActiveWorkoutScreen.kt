package com.example.fittrack.ui.screens.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fittrack.model.Equipment
import com.example.fittrack.model.Exercise
import com.example.fittrack.model.ExerciseType
import com.example.fittrack.model.MuscleGroup
import com.example.fittrack.model.WorkoutSet
import com.example.fittrack.ui.theme.FitTrackColors
import com.example.fittrack.ui.theme.FitTrackTheme
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@Composable
fun ActiveWorkoutScreen(
    addExerciseId: String? = null,
    onBack: () -> Unit,
    viewModel: ActiveWorkoutViewModel = koinViewModel()
) {
    LaunchedEffect(addExerciseId) {
        addExerciseId?.let { viewModel.addExerciseFromRoute(it) }
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ActiveWorkoutContent(
        state = state,
        onBack = onBack,
        onStart = viewModel::startWorkout,
        onAddExercise = viewModel::addExercise,
        onRemoveExercise = viewModel::removeExercise,
        onAddSet = viewModel::addSet,
        onUpdateSet = viewModel::updateSetValues,
        onToggleSet = viewModel::toggleSetCompleted,
        onDeleteSet = viewModel::deleteSet,
        onAdjustRest = viewModel::adjustRest,
        onSkipRest = viewModel::skipRest,
        onFinish = { viewModel.finishWorkout(onBack) },
        onDiscard = { viewModel.discardWorkout(onBack) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutContent(
    state: ActiveWorkoutUiState,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onAddExercise: (UUID) -> Unit,
    onRemoveExercise: (UUID) -> Unit,
    onAddSet: (UUID) -> Unit,
    onUpdateSet: (UUID, Double, Int) -> Unit,
    onToggleSet: (WorkoutSet) -> Unit,
    onDeleteSet: (WorkoutSet) -> Unit,
    onAdjustRest: (Int) -> Unit,
    onSkipRest: () -> Unit,
    onFinish: () -> Unit,
    onDiscard: () -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    var showDiscardConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = FitTrackColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Workout", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        if (state.hasSession) {
                            Text(
                                formatDuration(state.elapsedSeconds),
                                style = MaterialTheme.typography.labelMedium,
                                color = FitTrackColors.Primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.hasSession) {
                        TextButton(onClick = { showDiscardConfirm = true }) {
                            Text("Discard", color = FitTrackColors.Error)
                        }
                        Button(
                            onClick = onFinish,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FitTrackColors.PrimaryContainer,
                                contentColor = FitTrackColors.OnPrimaryContainer
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Finish", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FitTrackColors.Background,
                    titleContentColor = FitTrackColors.OnSurface,
                    navigationIconContentColor = FitTrackColors.OnSurface
                )
            )
        },
        bottomBar = {
            state.restRemainingSeconds?.let { RestTimerBar(it, onAdjustRest, onSkipRest) }
        }
    ) { paddingValues ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = FitTrackColors.Primary)
            }

            !state.hasSession -> NoWorkout(onStart, Modifier.padding(paddingValues))

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.exercises, key = { it.exercise.id }) { log ->
                    ExerciseCard(log, onAddSet, onRemoveExercise, onUpdateSet, onToggleSet, onDeleteSet)
                }
                item {
                    Button(
                        onClick = { showPicker = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FitTrackColors.SurfaceContainerHigh,
                            contentColor = FitTrackColors.Primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add exercise", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    if (showPicker) {
        ExercisePickerDialog(
            exercises = state.availableExercises,
            onPick = {
                onAddExercise(it)
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }

    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            containerColor = FitTrackColors.SurfaceContainerHigh,
            title = { Text("Discard workout?", color = FitTrackColors.OnSurface) },
            text = { Text("Everything logged in this workout will be deleted.", color = FitTrackColors.OnSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { showDiscardConfirm = false; onDiscard() }) {
                    Text("Discard", color = FitTrackColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirm = false }) { Text("Keep", color = FitTrackColors.Primary) }
            }
        )
    }
}

@Composable
private fun NoWorkout(onStart: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = FitTrackColors.Primary.copy(alpha = 0.6f), modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            "No workout in progress",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = FitTrackColors.OnSurface
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FitTrackColors.PrimaryContainer, contentColor = FitTrackColors.OnPrimaryContainer),
            shape = CircleShape
        ) {
            Text("Start workout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun ExerciseCard(
    log: ExerciseLog,
    onAddSet: (UUID) -> Unit,
    onRemoveExercise: (UUID) -> Unit,
    onUpdateSet: (UUID, Double, Int) -> Unit,
    onToggleSet: (WorkoutSet) -> Unit,
    onDeleteSet: (WorkoutSet) -> Unit
) {
    Surface(color = FitTrackColors.SurfaceContainer, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    log.exercise.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = FitTrackColors.OnSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onRemoveExercise(log.exercise.id) }) {
                    Icon(Icons.Default.Close, contentDescription = "Remove exercise", tint = FitTrackColors.OnSurfaceVariant)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ColumnLabel("SET", Modifier.width(28.dp))
                ColumnLabel("KG", Modifier.weight(1f))
                ColumnLabel("REPS", Modifier.weight(1f))
                Spacer(Modifier.width(96.dp))
            }
            log.sets.forEachIndexed { index, set ->
                key(set.id) {
                    SetRow(index + 1, set, onUpdateSet, onToggleSet, onDeleteSet)
                }
            }
            TextButton(onClick = { onAddSet(log.exercise.id) }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add set", color = FitTrackColors.Primary)
            }
        }
    }
}

@Composable
private fun ColumnLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
        color = FitTrackColors.OnSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun SetRow(
    number: Int,
    set: WorkoutSet,
    onUpdateSet: (UUID, Double, Int) -> Unit,
    onToggleSet: (WorkoutSet) -> Unit,
    onDeleteSet: (WorkoutSet) -> Unit
) {
    // The text fields own what the user types; the database is written from both current values,
    // so quick edits to one field can't overwrite the other with a stale value.
    var weightText by remember { mutableStateOf(if (set.weight == 0.0) "" else formatWeight(set.weight)) }
    var repsText by remember { mutableStateOf(if (set.reps == 0) "" else set.reps.toString()) }

    fun commit(weight: String, reps: String) =
        onUpdateSet(set.id, weight.replace(',', '.').toDoubleOrNull() ?: 0.0, reps.toIntOrNull() ?: 0)

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "$number",
            modifier = Modifier.width(28.dp),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = FitTrackColors.OnSurfaceVariant,
            textAlign = TextAlign.Center
        )
        SetField(weightText, KeyboardType.Decimal, Modifier.weight(1f)) {
            weightText = it
            commit(it, repsText)
        }
        SetField(repsText, KeyboardType.Number, Modifier.weight(1f)) {
            repsText = it
            commit(weightText, it)
        }
        IconButton(onClick = { onToggleSet(set) }) {
            Icon(
                if (set.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (set.isCompleted) "Mark set not done" else "Mark set done",
                tint = if (set.isCompleted) FitTrackColors.Primary else FitTrackColors.OnSurfaceVariant
            )
        }
        IconButton(onClick = { onDeleteSet(set) }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete set", tint = FitTrackColors.OnSurfaceVariant)
        }
    }
}

@Composable
private fun SetField(
    value: String,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    val allowed: (Char) -> Boolean =
        if (keyboardType == KeyboardType.Decimal) { c -> c.isDigit() || c == '.' || c == ',' } else { c -> c.isDigit() }
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.all(allowed)) onValueChange(it) },
        modifier = modifier,
        singleLine = true,
        placeholder = { Text("0", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
        textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center, color = FitTrackColors.OnSurface),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FitTrackColors.Primary,
            unfocusedBorderColor = FitTrackColors.Outline,
            cursorColor = FitTrackColors.Primary
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun RestTimerBar(remainingSeconds: Int, onAdjust: (Int) -> Unit, onSkip: () -> Unit) {
    Surface(color = FitTrackColors.SurfaceContainerHighest, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Default.Timer, contentDescription = null, tint = FitTrackColors.Secondary)
            Spacer(Modifier.width(4.dp))
            Text(
                "Rest ${formatDuration(remainingSeconds.toLong())}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = FitTrackColors.OnSurface,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { onAdjust(-REST_STEP_SECONDS) }) { Text("-${REST_STEP_SECONDS}s", color = FitTrackColors.Secondary) }
            TextButton(onClick = { onAdjust(REST_STEP_SECONDS) }) { Text("+${REST_STEP_SECONDS}s", color = FitTrackColors.Secondary) }
            TextButton(onClick = onSkip) { Text("Skip", color = FitTrackColors.Primary) }
        }
    }
}

@Composable
private fun ExercisePickerDialog(exercises: List<Exercise>, onPick: (UUID) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FitTrackColors.SurfaceContainerHigh,
        title = { Text("Add exercise", color = FitTrackColors.OnSurface) },
        text = {
            if (exercises.isEmpty()) {
                Text("Every exercise is already in this workout.", color = FitTrackColors.OnSurfaceVariant)
            } else {
                LazyColumn {
                    items(exercises, key = { it.id }) { exercise ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPick(exercise.id) }
                                .padding(vertical = 10.dp)
                        ) {
                            Text(exercise.name, color = FitTrackColors.OnSurface, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                exercise.muscleGroup.name,
                                color = FitTrackColors.Secondary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close", color = FitTrackColors.Primary) } }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF051424)
@Composable
private fun ActiveWorkoutPreview() {
    val bench = Exercise(
        name = "Chest Press",
        muscleGroup = MuscleGroup.CHEST,
        exerciseType = ExerciseType.WEIGHT_REPS,
        equipment = Equipment.BARBELL
    )
    val sessionId = UUID.randomUUID()
    FitTrackTheme {
        ActiveWorkoutContent(
            state = ActiveWorkoutUiState(
                isLoading = false,
                hasSession = true,
                elapsedSeconds = 754,
                exercises = listOf(
                    ExerciseLog(
                        bench,
                        listOf(
                            WorkoutSet(exerciseId = bench.id, sessionId = sessionId, reps = 8, weight = 80.0, position = 0, isCompleted = true),
                            WorkoutSet(exerciseId = bench.id, sessionId = sessionId, reps = 8, weight = 80.0, position = 1)
                        )
                    )
                ),
                restRemainingSeconds = 62
            ),
            onBack = {}, onStart = {}, onAddExercise = {}, onRemoveExercise = {}, onAddSet = {},
            onUpdateSet = { _, _, _ -> }, onToggleSet = {}, onDeleteSet = {}, onAdjustRest = {},
            onSkipRest = {}, onFinish = {}, onDiscard = {}
        )
    }
}
