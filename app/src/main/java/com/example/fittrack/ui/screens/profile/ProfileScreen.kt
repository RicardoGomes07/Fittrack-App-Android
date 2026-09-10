package com.example.fittrack.ui.screens.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fittrack.model.User
import com.example.fittrack.ui.screens.components.home.FitTrackBottomNav
import com.example.fittrack.ui.screens.components.profile.ProfileHero
import com.example.fittrack.ui.screens.components.profile.ProfileNotLogged
import com.example.fittrack.ui.screens.components.profile.ProfileTopBar
import com.example.fittrack.ui.theme.FitTrackColors
import com.example.fittrack.ui.theme.FitTrackTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    selectedNavItem: String = "profile",
    onNavItemSelected: (String) -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    viewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()
    val isInitialized by viewModel.isInitialized.collectAsStateWithLifecycle()

    if (!isInitialized) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = FitTrackColors.Primary)
        }
    } else if (loggedInUser == null) {
        ProfileNotLogged(
            selectedNavItem = selectedNavItem,
            onNavItemSelected = onNavItemSelected,
            onNavigateToLogin = onNavigateToLogin
        )
    } else {
        ProfileContent(
            state = uiState,
            user = loggedInUser!!,
            selectedNavItem = selectedNavItem,
            onNavItemSelected = onNavItemSelected,
            onToggleUnits = viewModel::toggleUnitSystem,
            onLogout = viewModel::logout
        )
    }
}
@Composable
fun ProfileContent(
    state: ProfileUiState,
    user: User,
    selectedNavItem: String,
    onNavItemSelected: (String) -> Unit,
    onToggleUnits: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        containerColor = FitTrackColors.Background,
        topBar = {
            ProfileTopBar(state, user)
        },
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
                .verticalScroll(rememberScrollState())
        ) {
            ProfileHero(state, user)
            LifetimeImpactSection(state)
            VolumeHeatmapSection()
            PersonalRecordsSection(state)
            SettingsSection(state, onToggleUnits, onLogout)
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun LifetimeImpactSection(state: ProfileUiState) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("LIFETIME IMPACT", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = FitTrackColors.OnSurfaceVariant)
            Text("Updated 2h ago", style = MaterialTheme.typography.labelSmall, color = FitTrackColors.Secondary)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ImpactCard(
                modifier = Modifier.weight(1f),
                title = "Workouts",
                value = state.workouts.toString(),
                trend = "+${state.workoutsChange} this mo",
                icon = Icons.Default.FitnessCenter,
                iconColor = FitTrackColors.Primary
            )
            ImpactCard(
                modifier = Modifier.weight(1f),
                title = "Tonnage",
                value = "${state.tonnage}t",
                subtitle = "Vol. load lifted",
                icon = Icons.Default.Scale,
                iconColor = FitTrackColors.Secondary
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ImpactCard(
                modifier = Modifier.weight(1f),
                title = "Trained",
                value = "${state.hoursTrained}hrs",
                subtitle = "Avg ${state.avgSessionMinutes}m / session",
                icon = Icons.Default.Schedule,
                iconColor = FitTrackColors.SecondaryFixedDim
            )
            ImpactCard(
                modifier = Modifier.weight(1f),
                title = "Milestones",
                value = state.monthlyPRs.toString(),
                unit = "PRs",
                subtitle = "Broken this month",
                icon = Icons.Default.MilitaryTech,
                iconColor = FitTrackColors.Primary,
                highlight = true
            )
        }
    }
}

@Composable
private fun ImpactCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String? = null,
    trend: String? = null,
    subtitle: String? = null,
    icon: ImageVector,
    iconColor: Color,
    highlight: Boolean = false
) {
    Surface(
        modifier = modifier,
        color = FitTrackColors.SurfaceContainerHigh,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = if (highlight) 8.dp else 2.dp,
        border = if (highlight) BorderStroke(1.dp, FitTrackColors.Primary.copy(alpha = 0.2f)) else null
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = FitTrackColors.OnSurfaceVariant)
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = if (highlight) FitTrackColors.Primary else FitTrackColors.OnSurface)
                    if (unit != null) {
                        Text(unit, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = FitTrackColors.OnSurface)
                    }
                }
                if (trend != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = FitTrackColors.Primary, modifier = Modifier.size(14.dp))
                        Text(trend, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = FitTrackColors.Primary)
                    }
                }
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = FitTrackColors.OnSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun VolumeHeatmapSection() {
    Surface(
        modifier = Modifier.padding(16.dp),
        color = FitTrackColors.SurfaceContainerHigh,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = FitTrackColors.Primary, modifier = Modifier.size(18.dp))
                    Text("Volume Heatmap", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = FitTrackColors.OnSurface)
                }
                Text("Past 28 Days", style = MaterialTheme.typography.labelSmall, color = FitTrackColors.OnSurfaceVariant)
            }

            // Grid
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                        Text(it, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = FitTrackColors.OnSurfaceVariant.copy(alpha = 0.6f))
                    }
                }
                
                repeat(4) { week ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        repeat(7) { day ->
                            val color = when {
                                (week + day) % 3 == 0 -> FitTrackColors.Primary
                                (week + day) % 5 == 0 -> FitTrackColors.Secondary
                                else -> FitTrackColors.SurfaceContainerLowest
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(color),
                                contentAlignment = Alignment.Center
                            ) {
                                if (week == 0 && day == 0) Text("1", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FitTrackColors.OnPrimary)
                            }
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LegendItem("Heavy Lift", FitTrackColors.Primary)
                LegendItem("Recovery", FitTrackColors.Secondary)
                LegendItem("Rest Day", FitTrackColors.SurfaceContainerLowest)
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(2.dp)))
        Text(label, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = FitTrackColors.OnSurfaceVariant)
    }
}

@Composable
private fun PersonalRecordsSection(state: ProfileUiState) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = FitTrackColors.Primary, modifier = Modifier.size(20.dp))
                Text("Personal Records", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = FitTrackColors.OnSurface)
            }
            TextButton(onClick = { }) {
                Text("View All", style = MaterialTheme.typography.labelLarge, color = FitTrackColors.Primary)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }

        val prs = state.prs
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PRCard(modifier = Modifier.weight(1f), pr = prs[0])
                PRCard(modifier = Modifier.weight(1f), pr = prs[1])
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PRCard(modifier = Modifier.weight(1f), pr = prs[2])
                PRCard(modifier = Modifier.weight(1f), pr = prs[3])
            }
        }
    }
}

@Composable
private fun PRCard(modifier: Modifier = Modifier, pr: PersonalRecord) {
    Surface(
        modifier = modifier,
        color = FitTrackColors.SurfaceContainerHigh,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(pr.name, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = FitTrackColors.OnSurface)
                Surface(
                    color = if (pr.isNew) FitTrackColors.PrimaryContainer.copy(alpha = 0.2f) else FitTrackColors.SurfaceContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        if (pr.isNew) "NEW" else "1RM",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (pr.isNew) FitTrackColors.Primary else FitTrackColors.Secondary
                    )
                }
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(pr.value.toString(), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = if (pr.isNew) FitTrackColors.Primary else FitTrackColors.OnSurface)
                Text(pr.unit, modifier = Modifier.padding(start = 2.dp, bottom = 2.dp), style = MaterialTheme.typography.labelMedium, color = FitTrackColors.OnSurfaceVariant)
            }
            Text(pr.date, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = FitTrackColors.OnSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
@Composable
private fun SettingsSection(state: ProfileUiState, onToggleUnits: () -> Unit, onLogout: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("SETTINGS & CONFIGURATION", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = FitTrackColors.OnSurfaceVariant)
        
        Surface(color = FitTrackColors.SurfaceContainerHigh, shape = RoundedCornerShape(16.dp)) {
            Column {
                SettingsItem(
                    icon = Icons.Default.Timer,
                    iconColor = FitTrackColors.Primary,
                    title = "Workout Timers & Audio",
                    subtitle = "90s default rest • Voice bell on"
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = FitTrackColors.SurfaceContainerHighest)
                SettingsItem(
                    icon = Icons.Default.Calculate,
                    iconColor = FitTrackColors.Secondary,
                    title = "Formulas & Increments",
                    subtitle = "Epley Formula • 2.5 kg plates"
                )
            }
        }

        Surface(color = FitTrackColors.SurfaceContainerHigh, shape = RoundedCornerShape(16.dp)) {
            Column {
                SettingsItem(
                    icon = Icons.Default.Sync,
                    iconColor = FitTrackColors.Primary,
                    title = "Health Connect & Devices",
                    subtitle = "Synced with Pixel Watch 3",
                    badgeColor = FitTrackColors.Primary
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = FitTrackColors.SurfaceContainerHighest)
                SettingsItem(
                    icon = Icons.Default.NotificationsActive,
                    iconColor = FitTrackColors.OnSurfaceVariant,
                    title = "Daily Reminders",
                    subtitle = "17:30 Gym time alert active"
                )
            }
        }

        Surface(color = FitTrackColors.SurfaceContainerHigh, shape = RoundedCornerShape(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(36.dp).background(FitTrackColors.SurfaceContainer, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Scale, contentDescription = null, tint = FitTrackColors.OnSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                    Text("Unit System", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = FitTrackColors.OnSurface)
                }
                
                Surface(color = FitTrackColors.SurfaceContainer, shape = CircleShape) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        UnitToggleButton("kg", selected = state.unitSystem == "kg", onClick = onToggleUnits)
                        UnitToggleButton("lbs", selected = state.unitSystem == "lbs", onClick = onToggleUnits)
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FitTrackColors.SurfaceContainerHigh),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Account Security & Privacy", style = MaterialTheme.typography.labelLarge)
            }
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FitTrackColors.Error.copy(alpha = 0.1f), contentColor = FitTrackColors.Error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sign Out of FitTrack", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
            Text(
                "FitTrack v1.0.0 (Build 22) • Android Compose Engine",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = FitTrackColors.OnSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun SettingsItem(icon: ImageVector, iconColor: Color, title: String, subtitle: String, badgeColor: Color? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { }.padding(16.dp, 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(36.dp).background(FitTrackColors.SurfaceContainer, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = FitTrackColors.OnSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = FitTrackColors.OnSurfaceVariant)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (badgeColor != null) {
                Box(modifier = Modifier.size(8.dp).background(badgeColor, CircleShape))
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = FitTrackColors.OnSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun UnitToggleButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) FitTrackColors.Primary.copy(alpha = 0.1f) else Color.Transparent)
            .clickable { if (!selected) onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = if (selected) FitTrackColors.Primary else FitTrackColors.OnSurfaceVariant
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF051424)
@Composable
private fun ProfileScreenPreview() {
    FitTrackTheme {
        val sampleUser = User(name = "Alex Morgan", nickname = "alex_lifts", password = "password", xp = 350, level = 1)
        ProfileContent(
            state = sampleUser.toProfileUiState("kg"),
            user = sampleUser,
            selectedNavItem = "profile",
            onNavItemSelected = {},
            onToggleUnits = {},
            onLogout = {}
        )
    }
}
