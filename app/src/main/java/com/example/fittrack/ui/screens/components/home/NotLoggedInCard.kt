package com.example.fittrack.ui.screens.components.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fittrack.ui.theme.FitTrackColors

@Composable
fun NotLoggedInCard(onSignUpClick: () -> Unit) {
    Surface(
        color = FitTrackColors.SurfaceContainer,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                tint = FitTrackColors.Primary.copy(alpha = 0.6f),
                modifier = Modifier.size(64.dp)
            )

            Text(
                "Track your journey",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = FitTrackColors.OnSurface,
                textAlign = TextAlign.Center
            )

            Text(
                "Log workouts, track your PRs, and see your progress over time by creating an account.",
                style = MaterialTheme.typography.bodyMedium,
                color = FitTrackColors.OnSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onSignUpClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = FitTrackColors.Primary,
                    contentColor = FitTrackColors.OnPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Get Started in Profile", fontWeight = FontWeight.Bold)
            }
        }
    }
}