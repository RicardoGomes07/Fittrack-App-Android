package com.example.fittrack.ui.screens.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.fittrack.ui.theme.FitTrackColors

@Composable
fun GreetingSection(
    dateLabel: String,
    userName: String
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
            "Welcome back, $userName",
            color = FitTrackColors.OnSurface,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}