package com.example.fittrack.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fittrack.ui.screens.components.FitTrackBottomNav
import com.example.fittrack.ui.screens.components.FitTrackTopBar
import com.example.fittrack.ui.screens.components.GreetingSection
import com.example.fittrack.ui.screens.components.MotivationBanner
import com.example.fittrack.ui.theme.FitTrackColors
import com.example.fittrack.ui.theme.FitTrackTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    selectedNavItem: String = "dashboard",
    onNavItemSelected: (String) -> Unit = {},
    onStartWorkout: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        state = state,
        selectedNavItem = selectedNavItem,
        onNavItemSelected = onNavItemSelected,
        onStartWorkout = onStartWorkout,
        onNotificationsClick = onNotificationsClick
    )
}

@Composable
fun HomeContent(
    state: HomeUiState,
    selectedNavItem: String = "dashboard",
    onNavItemSelected: (String) -> Unit = {},
    onStartWorkout: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
) {
    Scaffold(
        containerColor = FitTrackColors.Background,
        topBar = {
            FitTrackTopBar(
                streakDays = state.streakDays,
                onNotificationsClick = onNotificationsClick,
            )
        },
        bottomBar = {
            FitTrackBottomNav(
                selectedItem = selectedNavItem,
                onItemSelected = onNavItemSelected,
            )
        },
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = FitTrackColors.Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FitTrackColors.Background)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(Modifier.height(12.dp))

                GreetingSection(
                    dateLabel = state.dateLabel,
                    userName = state.userName,
                    isLoggedIn = state.loggedIn
                )

                Spacer(Modifier.height(12.dp))
                MotivationBanner(
                    message = state.motivationMessage
                )
                Spacer(Modifier.height(24.dp))

                if (!state.loggedIn) {
                    NotLoggedInCard(onSignUpClick = { onNavItemSelected("profile") })
                } else {
                    // TODO: Show summary of recent workouts or active routine
                    Text(
                        "Your Activity",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = FitTrackColors.OnSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun NotLoggedInCard(onSignUpClick: () -> Unit) {
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

@Preview(showBackground = true, backgroundColor = 0xFF051424)
@Composable
private fun HomeScreenPreview() {
    FitTrackTheme {
        HomeContent(
            state = HomeUiState(
                userName = "Ricardo",
                dateLabel = "Today, Monday Sep 7",
                streakDays = 5,
                motivationMessage = "Consistency is the key to success.",
                isLoading = false,
                loggedIn = true
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF051424)
@Composable
private fun HomeScreenGuestPreview() {
    FitTrackTheme {
        HomeContent(
            state = HomeUiState(
                userName = "Anonymous",
                dateLabel = "Today, Monday Sep 7",
                streakDays = 0,
                motivationMessage = "Consistency is the key to success.",
                isLoading = false,
                loggedIn = false
            )
        )
    }
}
