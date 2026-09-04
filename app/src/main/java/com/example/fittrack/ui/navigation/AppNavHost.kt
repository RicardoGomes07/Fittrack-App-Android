package com.example.fittrack.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fittrack.model.Screen
import com.example.fittrack.ui.screens.exercises.ExerciseDetailsScreen
import com.example.fittrack.ui.screens.exercises.ExercisesScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.EXERCISES.name
    ) {
        composable(Screen.EXERCISES.name) {
            ExercisesScreen(
                onExerciseClick = { id ->
                    navController.navigate("${Screen.EXERCISE_DETAIL.name}/$id")
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
