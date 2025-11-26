package com.cs407.cadence

import com.cs407.cadence.ui.screens.LoginScreen
import com.cs407.cadence.ui.screens.SetNameScreen
import com.cs407.cadence.ui.screens.HomeScreen
import com.cs407.cadence.ui.screens.WorkoutSetupScreen
import com.cs407.cadence.ui.screens.WorkoutScreen
import com.cs407.cadence.ui.screens.LogScreen
import com.cs407.cadence.ui.screens.SettingsScreen
import com.cs407.cadence.ui.navigation.BottomNav
import com.cs407.cadence.ui.theme.CadenceTheme
import com.cs407.cadence.ui.viewModels.UserViewModel
import com.cs407.cadence.ui.viewModels.WorkoutViewModel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController



class MainActivity : ComponentActivity() {

    private val userViewModel: UserViewModel by viewModels()
    private val workoutViewModel: WorkoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CadenceTheme {
                CadenceApp(userViewModel, workoutViewModel)
            }
        }
    }
}

@Composable
fun CadenceApp(
    userViewModel: UserViewModel,
    workoutViewModel: WorkoutViewModel
) {
    val userState by userViewModel.userState.collectAsStateWithLifecycle()
    val navController = rememberNavController()


    LaunchedEffect(userState?.uid) {
        val destination = when {
            userState == null -> "login"
            userState?.name.isNullOrEmpty() -> "setName"
            else -> "home"
        }

        navController.navigate(destination) {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (currentRoute in listOf("home", "log", "settings")) {
                BottomNav(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(viewModel = userViewModel)
            }

            composable("setName") {
                SetNameScreen(
                    viewModel = userViewModel,
                    onNavigateToHome = {
                        navController.navigate("home") {
                            popUpTo("setName") { inclusive = true }
                        }
                    }
                )
            }

            composable("home") {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(
                        username = userState?.name,
                        onNavigateToWorkoutSetup = { navController.navigate("workoutSetup") },
                        workoutViewModel = workoutViewModel
                    )
                }
            }

            composable("workoutSetup") {
                WorkoutSetupScreen(
                    workoutViewModel = workoutViewModel,
                    onNavigateToWorkout = { navController.navigate("workout") },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("workout") {
                WorkoutScreen(
                    onNavigateToHome = { navController.navigate("home") },
                    workoutViewModel = workoutViewModel
                )
            }

            composable("log") {
                LogScreen(workoutViewModel = workoutViewModel)
            }

            composable("settings") {
                SettingsScreen(
                    viewModel = userViewModel,
                    displayName = userState?.name ?: "",
                    onDisplayNameChange = { newName: String ->
                        userViewModel.setDisplayName(newName)
                    },
                    onClearLog = { workoutViewModel.clearAllHistory() }
                )
            }
        }
    }
}
