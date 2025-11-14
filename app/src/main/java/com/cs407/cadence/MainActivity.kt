package com.cs407.cadence

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
import com.cs407.cadence.ui.navigation.BottomNav
import com.cs407.cadence.ui.screens.*
import com.cs407.cadence.ui.theme.CadenceTheme
import com.cs407.cadence.ui.viewModels.UserViewModel

class MainActivity : ComponentActivity() {
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CadenceTheme {
                CadenceApp(userViewModel)
            }
        }
    }
}
@Composable
fun CadenceApp(viewModel: UserViewModel) {
    val userState by viewModel.userState.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    LaunchedEffect(userState?.uid) {
        val route = when {
            // not logged in (uid is null)
            userState == null -> "login"

            // logged in with no display name
            userState?.name.isNullOrEmpty() -> "setName"

            // logged in with display name
            else -> "home"
        }

        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (currentRoute == "home" || currentRoute == "log" || currentRoute == "settings") {
                BottomNav(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(viewModel = viewModel)
            }

            composable("setName") {
                SetNameScreen(
                    viewModel = viewModel,
                    onNavigateToHome = {
                        navController.navigate("home") { popUpTo("setName") { inclusive = true } }
                    }
                )
            }

            composable("home") {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    HomeScreen(
                        username = userState?.name,
                        onNavigateToWorkoutSetup = { navController.navigate("workoutSetup") }
                    )
                }
            }

            composable("workoutSetup") {
                WorkoutSetupScreen(
                    onNavigateToWorkout = { navController.navigate("workout") },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("workout") {
                WorkoutScreen(onNavigateToHome = { navController.navigate("home") })
            }

            composable("log") {
                LogScreen()
            }

            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    displayName = userState?.name ?: "",
                    onDisplayNameChange = { newName -> viewModel.setDisplayName(newName) },
                    onClearLog = { /* TODO */ },
                )
            }
        }
    }
}
