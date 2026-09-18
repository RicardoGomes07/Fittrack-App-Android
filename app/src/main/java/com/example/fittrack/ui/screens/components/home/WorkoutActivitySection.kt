package com.example.fittrack.ui.screens.components.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fittrack.model.SessionSummary
import com.example.fittrack.ui.screens.workout.formatDuration
import com.example.fittrack.ui.screens.workout.formatSessionDate
import com.example.fittrack.ui.theme.FitTrackColors
import com.example.fittrack.ui.theme.FitTrackTheme
import java.util.UUID
import kotlin.math.roundToInt

@Composable
fun WorkoutActivitySection(
    hasActiveWorkout: Boolean,
    recentSessions: List<SessionSummary>,
    onStartWorkout: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onStartWorkout,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FitTrackColors.Primary,
                contentColor = FitTrackColors.OnPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (hasActiveWorkout) "Resume workout" else "Start workout", fontWeight = FontWeight.Bold)
        }

        if (recentSessions.isEmpty()) {
            Text(
                "No workouts yet. Start your first one!",
                style = MaterialTheme.typography.bodyMedium,
                color = FitTrackColors.OnSurfaceVariant
            )
        } else {
            recentSessions.forEach { SessionSummaryCard(it) }
        }
    }
}

@Composable
private fun SessionSummaryCard(session: SessionSummary) {
    val duration = session.endedAt?.let { formatDuration((it - session.startedAt) / 1000) }
    Surface(color = FitTrackColors.SurfaceContainer, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    formatSessionDate(session.startedAt),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = FitTrackColors.OnSurface
                )
                if (duration != null) {
                    Text(duration, style = MaterialTheme.typography.labelLarge, color = FitTrackColors.Primary)
                }
            }
            Text(
                "${session.exerciseCount} exercises · ${session.setCount} sets · ${session.volume.roundToInt()} kg",
                style = MaterialTheme.typography.bodyMedium,
                color = FitTrackColors.OnSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF051424)
@Composable
private fun WorkoutActivitySectionPreview() {
    FitTrackTheme {
        WorkoutActivitySection(
            hasActiveWorkout = false,
            recentSessions = listOf(
                SessionSummary(UUID.randomUUID(), 1_757_000_000_000, 1_757_003_100_000, 4, 12, 5240.0)
            ),
            onStartWorkout = {}
        )
    }
}
