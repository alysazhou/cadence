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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cs407.cadence.ui.navigation.BottomNav
import com.cs407.cadence.ui.screens.*
import com.cs407.cadence.ui.theme.CadenceTheme
import com.cs407.cadence.ui.viewModels.UserViewModel
import com.cs407.cadence.data.repository.WorkoutRepository

class MainActivity : ComponentActivity() {
    private val userViewModel: UserViewModel by viewModels()
    private val workoutRepository = WorkoutRepository() //added so that distance/time are consistent across screens


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CadenceTheme {
                //CadenceApp(userViewModel)
                CadenceApp(userViewModel, workoutRepository) //pass repository into CadenceApp
            }
        }
    }
}
@Composable
fun CadenceApp(viewModel: UserViewModel, workoutRepository: WorkoutRepository ) {
    val userState by viewModel.userState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val isMusicConnected by viewModel.isMusicConnected.collectAsStateWithLifecycle()

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
                        navController.navigate("home") {
                            popUpTo("setName") { inclusive = true }
                        }
                    }
                )
            }

//            composable("musicAuth") {
//                MusicAuthScreen(
//                    onAuthComplete = {
//                        viewModel.setMusicConnected(true)
//                        navController.navigate("home") {
//                            popUpTo("musicAuth") { inclusive = true }
//                        }
//                    },
//                    onSkip = {
//                        viewModel.setMusicConnected(false)
//                        navController.navigate("home") {
//                            popUpTo("musicAuth") { inclusive = true }
//                        }
//                    }
//                )
//            }

            composable("home") {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    HomeScreen(
                        navToMap = { navController.navigate("map") },
                        onNavigateToWorkoutSetup = { navController.navigate("workoutSetup") },
                        username = userState?.name,
                        workoutRepository = workoutRepository
                    )
                }
            }

            composable("workoutSetup") {
                WorkoutSetupScreen(
                    onNavigateToWorkout = { selectedGenre ->
                        navController.navigate("workout/$selectedGenre")
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "workout/{genre}",
                arguments = listOf(navArgument("genre") { type = NavType.StringType })
            ) { backStackEntry ->
                val genre = backStackEntry.arguments?.getString("genre") ?: "pop" // Default to pop
                WorkoutScreen(
                    onNavigateToHome = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    workoutRepository = workoutRepository,
                    selectedGenre = genre // Pass the genre here
                )
            }

            composable("log") {
                LogScreen()
            }

            composable("settings") {
                //UPDATE THIS COMPOSABLE CALL
                SettingsScreen(
                    viewModel = viewModel,
                    displayName = userState?.name ?: "",
                    onDisplayNameChange = { newName -> viewModel.setDisplayName(newName) },
                    onClearLog = { /* TODO */ },
                    isMusicConnected = isMusicConnected,
                    onMusicAuth = { isConnected ->
                        viewModel.setMusicConnected(isConnected)
                    }
                )
            }
            composable("map") {
                MapScreen(navController = navController)
            }
        }
    }
}
