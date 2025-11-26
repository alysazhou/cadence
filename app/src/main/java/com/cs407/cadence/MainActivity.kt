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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cs407.cadence.ui.navigation.BottomNav
import com.cs407.cadence.ui.screens.*
import com.cs407.cadence.ui.theme.CadenceTheme
import com.cs407.cadence.data.repository.WorkoutRepository

class MainActivity : ComponentActivity() {

    private val userViewModel: UserViewModel by viewModels()
    private val workoutViewModel: WorkoutViewModel by viewModels()
    private val workoutRepository = WorkoutRepository()

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
                        //I added navigation callback for the Map button,
                        //which allows HomeScreen to trigger navigation to the new MapScreen
                        navToMap = { navController.navigate("map") },

                        onNavigateToWorkoutSetup = { navController.navigate("workoutSetup") },
                        username = userState?.name,
                        workoutViewModel = workoutViewModel,
                        workoutRepository = TODO(),
                        modifier = TODO()
                    )
                }
            }

            composable("workoutSetup") {
                WorkoutSetupScreen(
                    workoutViewModel = workoutViewModel,
                    onNavigateToWorkout = { selectedGenre ->
                        navController.navigate("workout/$selectedGenre")
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("workout") {
                WorkoutScreen(
                    onNavigateToHome = { navController.navigate("home") },
                    workoutViewModel = workoutViewModel,
                    workoutRepository = workoutRepository,
                    selectedGenre = genre
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
                    onClearLog = { workoutViewModel.clearAllHistory() },
                    isMusicConnected = isMusicConnected,
                    onMusicAuth = { isConnected ->
                        viewModel.setMusicConnected(isConnected)
                    }
                )
            }

            //This is a new composable route for the Map screen that registers the
            //"map" route w/ the NavHost (so the app can navigate to MapScreen)

            //This route is triggered by the (temp) "Open Map" button on HomeScreen
            composable("map") {
                MapScreen(navController = navController)
            }
        }
    }
}
