package com.example.fittrack.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.fittrack.model.Screen
import com.example.fittrack.ui.screens.auth.LoginScreen
import com.example.fittrack.ui.screens.auth.SignUpScreen
import com.example.fittrack.ui.screens.exercises.ExerciseAddScreen
import com.example.fittrack.ui.screens.exercises.ExerciseDetailsScreen
import com.example.fittrack.ui.screens.exercises.ExercisesScreen
import com.example.fittrack.ui.screens.home.HomeScreen
import com.example.fittrack.ui.screens.profile.ProfileScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.HOME.name
    ) {
        composable(Screen.HOME.name) {
            HomeScreen(
                onNavItemSelected = { route ->
                    val destination = when (route) {
                        "exercises" -> Screen.EXERCISES.name
                        "profile" -> Screen.PROFILE.name
                        else -> null
                    }
                    destination?.let {
                        navController.navigate(it) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
        composable(Screen.LOGIN.name) {
            LoginScreen(
                onLoginSuccess = {
                    navController.popBackStack(Screen.PROFILE.name, inclusive = false)
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SIGN_UP.name)
                }
            )
        }
        composable(Screen.SIGN_UP.name) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.PROFILE.name) {
                        popUpTo(Screen.HOME.name) { inclusive = false }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.LOGIN.name) {
                        popUpTo(Screen.SIGN_UP.name) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.PROFILE.name) {
            ProfileScreen(
                onNavItemSelected = { route ->
                    val destination = when (route) {
                        "dashboard" -> Screen.HOME.name
                        "exercises" -> Screen.EXERCISES.name
                        "profile" -> Screen.PROFILE.name
                        else -> null
                    }
                    if (destination != null && destination != Screen.PROFILE.name) {
                        navController.navigate(destination) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.LOGIN.name)
                }
            )
        }
        composable(Screen.EXERCISES.name) {
            ExercisesScreen(
                onExerciseClick = { id ->
                    navController.navigate("${Screen.EXERCISE_DETAIL.name}/$id")
                },
                onNavItemSelected = { route ->
                    val destination = when (route) {
                        "dashboard" -> Screen.HOME.name
                        "profile" -> Screen.PROFILE.name
                        else -> null
                    }
                    destination?.let {
                        navController.navigate(it) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onAddExerciseClick = {
                    navController.navigate(Screen.EXERCISE_ADD.name)
                }
            )
        }
        composable(Screen.EXERCISE_ADD.name) {
            ExerciseAddScreen(onBack = { navController.popBackStack() })
        }
        composable("${Screen.EXERCISE_DETAIL.name}/{exerciseId}") { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId")
            ExerciseDetailsScreen(
                exerciseId = exerciseId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
