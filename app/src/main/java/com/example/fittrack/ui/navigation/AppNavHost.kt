package com.example.fittrack.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.fittrack.model.Screen
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
        composable(Screen.EXERCISE_ADD.name) {
            ExerciseAddScreen()
        }
        composable(Screen.PROFILE.name){
            ProfileScreen(
                onNavItemSelected = { route ->
                    val destination = when (route) {
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
        composable("${Screen.EXERCISE_DETAIL.name}/{exerciseId}") { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId")
            ExerciseDetailsScreen(
                exerciseId = exerciseId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
