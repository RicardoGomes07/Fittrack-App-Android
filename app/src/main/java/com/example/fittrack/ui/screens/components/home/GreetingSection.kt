package com.example.fittrack.ui.screens.components.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.fittrack.ui.theme.FitTrackColors

@Composable
fun GreetingSection(
    dateLabel: String,
    userName: String,
    isLoggedIn: Boolean = true
) {
    Column {
        Text(
            dateLabel.uppercase(),
            color = FitTrackColors.OnSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
        )
        Text(
            if (isLoggedIn) "Welcome back, $userName" else "Welcome Anonymous",
            color = FitTrackColors.OnSurface,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
