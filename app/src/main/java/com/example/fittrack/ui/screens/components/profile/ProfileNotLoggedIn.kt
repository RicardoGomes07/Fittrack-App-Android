package com.example.fittrack.ui.screens.components.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fittrack.ui.screens.components.home.FitTrackBottomNav
import com.example.fittrack.ui.theme.FitTrackColors

@Composable
fun ProfileNotLogged(
    selectedNavItem: String,
    onNavItemSelected: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Scaffold(
        containerColor = FitTrackColors.Background,
        bottomBar = {
            FitTrackBottomNav(
                selectedItem = selectedNavItem,
                onItemSelected = onNavItemSelected
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = FitTrackColors.SurfaceContainerHighest
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Not Signed In",
                style = MaterialTheme.typography.headlineMedium,
                color = FitTrackColors.OnSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Sign in to track your progress and view your personal records.",
                style = MaterialTheme.typography.bodyLarge,
                color = FitTrackColors.OnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FitTrackColors.Primary),
                shape = CircleShape
            ) {
                Text("Sign In / Sign Up", color = FitTrackColors.OnPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}