package com.example.fittrack.ui.screens.exercises

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fittrack.R
import com.example.fittrack.model.Exercise
import com.example.fittrack.model.MuscleGroup
import com.example.fittrack.ui.theme.FitTrackColors
import com.example.fittrack.ui.theme.FitTrackTheme
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@Composable
fun ExerciseDetailsScreen(
    exerciseId: String?,
    onBack: () -> Unit,
    viewModel: ExerciseDetailViewModel = koinViewModel()
) {
    LaunchedEffect(exerciseId) {
        exerciseId?.let { viewModel.loadExercise(it) }
    }

    val exercise by viewModel.exercise.collectAsStateWithLifecycle()

    ExerciseDetailsContent(
        exercise = exercise,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExerciseDetailsContent(
    exercise: Exercise?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = FitTrackColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Exercise Detail",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FitTrackColors.Background.copy(alpha = 0.85f),
                    titleContentColor = FitTrackColors.OnSurface,
                    navigationIconContentColor = FitTrackColors.OnSurface
                )
            )
        },
        bottomBar = {
            BottomStickyBar()
        }
    ) { paddingValues ->
        if (exercise == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = FitTrackColors.Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 100.dp) // Space for fixed bottom bar
            ) {
                Spacer(Modifier.height(12.dp))

                // Title & identity section
                ExerciseHeader(exercise)

                Spacer(Modifier.height(16.dp))

                // Visual Form Card
                FormGuideCard(exercise)

                Spacer(Modifier.height(16.dp))

                // PRs Widget
                PersonalRecordsWidget()

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ExerciseHeader(exercise: Exercise) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Badge(
                containerColor = FitTrackColors.SurfaceContainerHigh,
                contentColor = FitTrackColors.Secondary,
                icon = Icons.Default.FitnessCenter,
                text = "Compound Lift"
            )

            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(36.dp)
                    .background(FitTrackColors.SurfaceContainer, CircleShape)
            ) {
                Icon(
                    Icons.Default.Bookmark,
                    contentDescription = "Favorite",
                    tint = FitTrackColors.Secondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = exercise.name,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            color = FitTrackColors.OnSurface,
            lineHeight = 36.sp
        )

        Text(
            text = exercise.description,
            style = MaterialTheme.typography.bodySmall,
            color = FitTrackColors.OnSurfaceVariant
        )

        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Badge(
                containerColor = FitTrackColors.SurfaceContainer,
                contentColor = FitTrackColors.OnSurface,
                text = "PRIMARY: ${exercise.muscleGroup.name}",
                showDot = true,
                dotColor = FitTrackColors.Primary
            )
            Badge(
                containerColor = FitTrackColors.SurfaceContainer,
                contentColor = FitTrackColors.OnSurfaceVariant,
                text = "SECONDARY: Deltoid, Triceps",
                showDot = true,
                dotColor = FitTrackColors.Secondary
            )
            Badge(
                containerColor = FitTrackColors.SurfaceContainerHigh,
                contentColor = FitTrackColors.OnSurfaceVariant,
                icon = Icons.Default.Layers,
                text = "Barbell, Flat Bench"
            )
        }
    }
}

@Composable
private fun FormGuideCard(exercise: Exercise) {
    Surface(
        color = FitTrackColors.SurfaceContainer,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.SportsGymnastics, contentDescription = null, tint = FitTrackColors.Primary, modifier = Modifier.size(20.dp))
                    Text(
                        "Form & Execution Guide",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = FitTrackColors.OnSurface
                    )
                }
                Surface(
                    color = FitTrackColors.SurfaceContainerHighest,
                    shape = CircleShape
                ) {
                    Text(
                        "Biomechanics",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = FitTrackColors.Secondary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Illustrated Form Visual (Placeholder for now)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(FitTrackColors.SurfaceContainerLowest)
            ) {
                Image(
                    painter = painterResource(id = exercise.imageRes ?: R.drawable.ic_chest_press),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(24.dp).blur(1.dp),
                    contentScale = ContentScale.Fit,
                    alpha = 0.6f
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, FitTrackColors.SurfaceContainer.copy(alpha = 0.8f)),
                                startY = 100f
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HotspotTag(icon = Icons.Default.Straighten, text = "Path: Slight 'J' curve", color = FitTrackColors.Primary)
                    HotspotTag(icon = Icons.Default.Speed, text = "Tempo: 3-1-X-0", color = FitTrackColors.Secondary)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Step-by-Step
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StepItem(1, "Set shoulder blades retracted", "Pinch scapulae tight together and drive traps firmly into the bench surface.")
                StepItem(2, "Unrack with straight wrists", "Squeeze the knurling directly over forearm bones with elbows fully locked.")
                StepItem(3, "Touch lower sternum & drive", "Control bar to lower sternum, engage leg drive, and press forcefully upward.")
            }
        }
    }
}

@Composable
private fun StepItem(number: Int, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(FitTrackColors.SurfaceContainerLow)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier.size(20.dp),
            color = FitTrackColors.Primary.copy(alpha = 0.2f),
            shape = CircleShape
        ) {
            Text(
                number.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = FitTrackColors.Primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.wrapContentHeight()
            )
        }
        Column {
            Text(title, style = MaterialTheme.typography.labelMedium, color = FitTrackColors.OnSurface)
            Text(description, style = MaterialTheme.typography.bodySmall, color = FitTrackColors.OnSurfaceVariant)
        }
    }
}

@Composable
private fun HotspotTag(icon: ImageVector, text: String, color: Color) {
    Surface(
        color = FitTrackColors.SurfaceContainerHighest.copy(alpha = 0.9f),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.blur(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
            Text(text, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = color)
        }
    }
}

@Composable
private fun PersonalRecordsWidget() {
    Surface(
        color = FitTrackColors.SurfaceContainer,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = FitTrackColors.Primary, modifier = Modifier.size(20.dp))
                    Text(
                        "Personal Records",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = FitTrackColors.OnSurface
                    )
                }
                Text("Logged Oct 18, 2024", style = MaterialTheme.typography.bodySmall, color = FitTrackColors.OnSurfaceVariant)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PRBox(
                    modifier = Modifier.weight(1f),
                    label = "Current Best Rep PR",
                    value = "105",
                    unit = "kg",
                    extra = "× 3",
                    trend = "+5.0 kg this block"
                )
                PRBox(
                    modifier = Modifier.weight(1f),
                    label = "Calculated 1RM",
                    value = "114",
                    unit = "kg",
                    trend = "Formula: Epley (RPE 9.5)",
                    trendColor = FitTrackColors.OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PRBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    unit: String,
    extra: String? = null,
    trend: String,
    trendColor: Color = FitTrackColors.Secondary
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(FitTrackColors.SurfaceContainerLow)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = FitTrackColors.OnSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = if (label.contains("1RM")) FitTrackColors.Secondary else FitTrackColors.Primary
            )
            Text(unit, style = MaterialTheme.typography.bodySmall, color = FitTrackColors.OnSurface, modifier = Modifier.padding(start = 2.dp))
            if (extra != null) {
                Text(extra, style = MaterialTheme.typography.headlineSmall, color = FitTrackColors.OnSurface, modifier = Modifier.padding(start = 4.dp))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (trendColor == FitTrackColors.Secondary) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = trendColor, modifier = Modifier.size(14.dp))
            }
            Text(trend, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = trendColor)
        }
    }
}

@Composable
private fun Badge(
    containerColor: Color,
    contentColor: Color,
    icon: ImageVector? = null,
    text: String,
    showDot: Boolean = false,
    dotColor: Color = Color.Unspecified
) {
    Surface(
        color = containerColor,
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = contentColor)
            }
            if (showDot) {
                Box(modifier = Modifier.size(6.dp).background(dotColor, CircleShape))
            }
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                color = contentColor
            )
        }
    }
}

@Composable
private fun BottomStickyBar() {
    Surface(
        color = FitTrackColors.Background.copy(alpha = 0.9f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FitTrackColors.PrimaryContainer, contentColor = FitTrackColors.OnPrimaryContainer),
                    shape = CircleShape
                ) {
                    Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add to Today's Workout", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }

                IconButton(
                    onClick = { },
                    modifier = Modifier.size(48.dp).background(FitTrackColors.SurfaceContainerHighest, CircleShape)
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = "Video Demo", tint = FitTrackColors.Secondary)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF051424)
@Composable
fun ExerciseDetailsScreenPreview() {
    val sampleExercise = Exercise(
        id = UUID.randomUUID(),
        name = "Barbell Bench Press",
        muscleGroup = MuscleGroup.CHEST,
        description = "Foundational upper-body horizontal push strength standard",
        imageRes = R.drawable.ic_chest_press
    )
    FitTrackTheme {
        ExerciseDetailsContent(
            exercise = sampleExercise,
            onBack = {}
        )
    }
}
