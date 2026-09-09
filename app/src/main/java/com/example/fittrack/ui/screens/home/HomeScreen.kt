package com.example.fittrack.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fittrack.ui.screens.components.home.FitTrackBottomNav
import com.example.fittrack.ui.screens.components.home.FitTrackTopBar
import com.example.fittrack.ui.screens.components.home.GreetingSection
import com.example.fittrack.ui.screens.components.home.MotivationBanner
import com.example.fittrack.ui.screens.components.home.NotLoggedInCard
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
                isLoading = true,
                loggedIn = false
            )
        )
    }
}
