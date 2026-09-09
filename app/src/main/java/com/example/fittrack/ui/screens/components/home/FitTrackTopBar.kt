package com.example.fittrack.ui.screens.components.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrack.R
import com.example.fittrack.ui.theme.FitTrackColors
import com.example.fittrack.ui.theme.FitTrackTheme

@Composable
fun FitTrackTopBar(
    streakDays: Int,
    onNotificationsClick: () -> Unit,
) {
    Surface(color = FitTrackColors.Background, tonalElevation = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                )
                Column {
                    Text(
                        "FITTRACK",
                        color = FitTrackColors.Primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                    )
                    Text(
                        "Dashboard",
                        color = FitTrackColors.OnSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(FitTrackColors.SurfaceContainerHigh)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("\uD83D\uDD25", fontSize = 12.sp)
                    Text("$streakDays Days", color = FitTrackColors.Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = onNotificationsClick, modifier = Modifier.size(40.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(FitTrackColors.SurfaceContainerHigh),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = FitTrackColors.OnSurface, modifier = Modifier.size(20.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FitTrackColors.SurfaceContainerHigh),
                )
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF051424)
@Composable
private fun FitTrackTopBarPreview() {
    FitTrackTheme {
        FitTrackTopBar(
            2,
            {}
        )
    }
}