package com.example.fittrack.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fittrack.model.Gender
import com.example.fittrack.ui.theme.FitTrackColors
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(Gender.MALE) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSignUpSuccess()
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FitTrackColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            color = FitTrackColors.OnSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Join FitTrack to start tracking",
            style = MaterialTheme.typography.bodyMedium,
            color = FitTrackColors.OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FitTrackColors.Primary,
                unfocusedBorderColor = FitTrackColors.Outline,
                focusedLabelColor = FitTrackColors.Primary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Nickname") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FitTrackColors.Primary,
                unfocusedBorderColor = FitTrackColors.Outline,
                focusedLabelColor = FitTrackColors.Primary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FitTrackColors.Primary,
                unfocusedBorderColor = FitTrackColors.Outline,
                focusedLabelColor = FitTrackColors.Primary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FitTrackColors.Primary,
                    unfocusedBorderColor = FitTrackColors.Outline,
                    focusedLabelColor = FitTrackColors.Primary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Height (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FitTrackColors.Primary,
                    unfocusedBorderColor = FitTrackColors.Outline,
                    focusedLabelColor = FitTrackColors.Primary
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Gender Selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Gender", style = MaterialTheme.typography.labelLarge, color = FitTrackColors.OnSurfaceVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Gender.entries.forEach { gender ->
                    FilterChip(
                        selected = selectedGender == gender,
                        onClick = { selectedGender = gender },
                        label = { Text(gender.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FitTrackColors.Primary,
                            selectedLabelColor = FitTrackColors.OnPrimary,
                            containerColor = FitTrackColors.SurfaceContainerHigh,
                            labelColor = FitTrackColors.OnSurfaceVariant
                        ),
                        border = null
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { 
                viewModel.signUp(
                    name, 
                    nickname, 
                    password, 
                    weight.toDoubleOrNull() ?: 0.0, 
                    height.toIntOrNull() ?: 0,
                    selectedGender
                ) 
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FitTrackColors.PrimaryContainer, contentColor = FitTrackColors.OnPrimaryContainer),
            shape = CircleShape,
            enabled = name.isNotEmpty() && nickname.isNotEmpty() && password.isNotEmpty() && !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = FitTrackColors.OnPrimaryContainer, modifier = Modifier.size(24.dp))
            } else {
                Text("Sign Up", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        TextButton(
            onClick = onNavigateToLogin,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Already have an account? Sign In", color = FitTrackColors.Primary)
        }
        
        Spacer(Modifier.height(32.dp))
    }
}
