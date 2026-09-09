package com.example.fittrack.ui.screens.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrack.ui.theme.FitTrackColors
import com.example.fittrack.ui.theme.FitTrackTheme

private data class NavItem(val key: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val navItems = listOf(
    NavItem("dashboard", "Home", Icons.Filled.Dashboard),
    NavItem("exercises", "Exercises", Icons.Filled.FitnessCenter),
    //NavItem("analytics", "Analytics", Icons.Filled.Insights),
    NavItem("profile", "Profile", Icons.Filled.Person),
)

@Composable
fun FitTrackBottomNav(selectedItem: String, onItemSelected: (String) -> Unit) {
    NavigationBar(
        containerColor = FitTrackColors.SurfaceContainerLowest.copy(alpha = 0.95f),
        contentColor = FitTrackColors.OnSurfaceVariant,
        modifier = Modifier.navigationBarsPadding(),
    ) {
        navItems.forEach { item ->
            val selected = item.key == selectedItem
            NavigationBarItem(
                selected = selected,
                onClick = { onItemSelected(item.key) },
                icon = { Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(22.dp)) },
                label = { Text(item.label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FitTrackColors.Primary,
                    selectedTextColor = FitTrackColors.Primary,
                    indicatorColor = FitTrackColors.PrimaryContainer.copy(alpha = 0.2f),
                    unselectedIconColor = FitTrackColors.OnSurfaceVariant,
                    unselectedTextColor = FitTrackColors.OnSurfaceVariant,
                ),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF051424)
@Composable
private fun FitTrackBottomNavPreview() {
    FitTrackTheme {
        FitTrackBottomNav(
            selectedItem = "dashboard",
            onItemSelected = {}
        )
    }
}