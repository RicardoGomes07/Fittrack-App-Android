package com.example.fittrack.ui.screens.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrack.model.User
import com.example.fittrack.ui.screens.profile.ProfileUiState
import com.example.fittrack.ui.theme.FitTrackColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopBar(state: ProfileUiState, user: User) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(32.dp).background(FitTrackColors.SurfaceContainerHigh, CircleShape), contentAlignment = Alignment.Center) {
                    Text(user.name.first().toString(), color = FitTrackColors.Primary, style = MaterialTheme.typography.labelLarge)
                }
                Column {
                    Text(
                        "FITTRACK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = FitTrackColors.Primary
                    )
                    Text(
                        "Profile",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = FitTrackColors.OnSurface
                    )
                }
            }
        },
        actions = {
            Row(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clip(CircleShape)
                    .background(FitTrackColors.SurfaceContainerHigh)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("🔥", fontSize = 12.sp)
                Text("${state.streakDays}-Day Streak", color = FitTrackColors.Primary, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = FitTrackColors.Background.copy(alpha = 0.85f),
            titleContentColor = FitTrackColors.OnSurface
        )
    )
}
