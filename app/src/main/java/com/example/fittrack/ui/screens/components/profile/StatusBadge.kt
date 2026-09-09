package com.example.fittrack.ui.screens.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrack.ui.theme.FitTrackColors

@Composable
fun StatusBadge(
    text: String,
    iconStr: String? = null,
    icon: ImageVector? = null,
    dotColor: Color? = null,
    contentColor: Color = FitTrackColors.Primary
) {
    Surface(
        color = if (dotColor != null) FitTrackColors.PrimaryContainer.copy(alpha = 0.1f) else FitTrackColors.SurfaceContainerHigh,
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (iconStr != null) Text(iconStr, fontSize = 12.sp)
            if (icon != null) Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp), tint = contentColor)
            if (dotColor != null) {
                Box(modifier = Modifier.size(8.dp).background(dotColor, CircleShape))
            }
            Text(
                text,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
        }
    }
}