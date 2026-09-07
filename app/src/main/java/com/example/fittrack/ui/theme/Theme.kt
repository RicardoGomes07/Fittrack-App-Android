package com.example.fittrack.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val FitTrackDarkColorScheme = darkColorScheme(
    primary = FitTrackColors.Primary,
    onPrimary = FitTrackColors.OnPrimary,
    primaryContainer = FitTrackColors.PrimaryContainer,
    onPrimaryContainer = FitTrackColors.OnPrimaryContainer,
    secondary = FitTrackColors.Secondary,
    onSecondary = FitTrackColors.OnSecondary,
    secondaryContainer = FitTrackColors.SecondaryContainer,
    onSecondaryContainer = FitTrackColors.OnSecondaryContainer,
    background = FitTrackColors.Background,
    onBackground = FitTrackColors.OnSurface,
    surface = FitTrackColors.Background,
    onSurface = FitTrackColors.OnSurface,
    surfaceVariant = FitTrackColors.SurfaceContainerHighest,
    onSurfaceVariant = FitTrackColors.OnSurfaceVariant,
    outline = FitTrackColors.Outline,
    error = FitTrackColors.Error,
)

@Composable
fun FitTrackTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = FitTrackDarkColorScheme, content = content)
}