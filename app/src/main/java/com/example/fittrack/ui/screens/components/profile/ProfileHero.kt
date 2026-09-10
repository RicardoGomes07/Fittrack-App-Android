package com.example.fittrack.ui.screens.components.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrack.model.User
import com.example.fittrack.ui.screens.profile.ProfileUiState
import com.example.fittrack.ui.theme.FitTrackColors

@Composable
fun ProfileHero(state: ProfileUiState, user: User) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // Ambient Glow
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(width = 250.dp, height = 120.dp)
                .offset(y = (-40).dp)
                .blur(60.dp)
                .background(FitTrackColors.Primary.copy(alpha = 0.1f), CircleShape)
        )

        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Avatar with Progress
                    Box(contentAlignment = Alignment.Center) {
                        val progress = if (state.maxXp > 0) state.xp.toFloat() / state.maxXp.toFloat() else 0f
                        val sweepAngle = progress * 360f

                        Canvas(modifier = Modifier.size(80.dp)) {
                            drawCircle(color = FitTrackColors.SurfaceContainerHighest, radius = size.minDimension / 2, style = Stroke(width = 3.dp.toPx()))
                            drawArc(
                                color = FitTrackColors.Primary,
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(FitTrackColors.SurfaceContainerLowest),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(user.name.first().toString(), style = MaterialTheme.typography.headlineMedium, color = FitTrackColors.Primary)
                        }
                        Surface(
                            modifier = Modifier.align(Alignment.BottomEnd).offset(x = 4.dp, y = 4.dp),
                            color = FitTrackColors.Primary,
                            shape = CircleShape,
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                "L${state.level}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = FitTrackColors.OnPrimary
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(user.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = FitTrackColors.OnSurface)
                            Icon(Icons.Default.Verified, contentDescription = null, tint = FitTrackColors.Primary, modifier = Modifier.size(18.dp))
                        }
                        Text("@${user.nickname}", style = MaterialTheme.typography.bodyMedium, color = FitTrackColors.OnSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "XP: ${state.xp}/${state.maxXp}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = FitTrackColors.Primary
                            )
                            Text("•", color = FitTrackColors.OnSurfaceVariant.copy(alpha = 0.5f))
                            Text("Member since ${state.memberSince}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = FitTrackColors.OnSurfaceVariant.copy(alpha = 0.8f))
                        }
                    }
                }

                IconButton(
                    onClick = { },
                    modifier = Modifier.size(44.dp).background(FitTrackColors.SurfaceContainerHigh, CircleShape)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = FitTrackColors.OnSurface, modifier = Modifier.size(20.dp))
                }
            }

            // Badges Bar
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                StatusBadge(text = "Athlete", dotColor = FitTrackColors.Primary)
                StatusBadge(text = "${state.consistency}% Consistency", icon = Icons.Default.Timer, contentColor = FitTrackColors.Secondary)
            }

            // Physical Metrics
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(FitTrackColors.SurfaceContainer)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetricItem(label = "Weight", value = "${state.weight}${state.unitSystem}")
                VerticalDivider(modifier = Modifier.height(24.dp), color = FitTrackColors.Outline.copy(alpha = 0.2f))
                MetricItem(label = "Height", value = "${state.height}cm")
                VerticalDivider(modifier = Modifier.height(24.dp), color = FitTrackColors.Outline.copy(alpha = 0.2f))
                MetricItem(label = "Gender", value = state.gender)
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = FitTrackColors.OnSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = FitTrackColors.OnSurface)
    }
}
