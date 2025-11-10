package com.cs407.cadence

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cs407.cadence.data.repository.UserRepository
import com.cs407.cadence.ui.navigation.BottomNav
import com.cs407.cadence.ui.viewModels.AuthState
import com.cs407.cadence.ui.screens.HomeScreen
import com.cs407.cadence.ui.screens.WorkoutScreen
import com.cs407.cadence.ui.screens.LogScreen
import com.cs407.cadence.ui.screens.LoginScreen
import com.cs407.cadence.ui.screens.SettingsScreen
import com.cs407.cadence.ui.screens.WorkoutSetupScreen
import com.cs407.cadence.ui.viewModels.LoginScreenViewModel
import com.cs407.cadence.ui.viewModels.LoginScreenViewModelFactory
import com.cs407.cadence.ui.theme.CadenceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // authentification test runs once when the app starts
//        val userRepo = UserRepository(this)
//
//        val newUser = userRepo.registerUser("Redet")
//        println("User registered: $newUser")
//
//        val retrievedUser = userRepo.getUser()
//        println("User retrieved: $retrievedUser")
//
//        userRepo.deleteUser()
//        println("User deleted. Current user: ${userRepo.getUser()}")
        // end of AUTH test — above code only prints to Logcat, not UI

        // CRUD backend test that runs once when the app starts
//        val repo = WorkoutRepository()
//        val newSession = WorkoutSession(
//            id = 1,
//            date = "2025-10-28",
//            durationMinutes = 45,
//            distanceKm = 7.2,
//            pace = 6.2
//        )
//        repo.createSession(newSession)
//        println(repo.getAllSessions()) // shows one workout
//
//        val updated = newSession.copy(pace = 5.9)
//        repo.updateSession(updated)
//        println(repo.getAllSessions()) // shows updated pace
//
//        repo.deleteAllSessions()
//        println(repo.getAllSessions()) // empty list

        // end of CRUD test ... above code only prints to Logcat, not UI

        setContent {
            CadenceTheme {

                // LOGIN
                val userRepository = UserRepository(applicationContext)
                val loginViewModel: LoginScreenViewModel = viewModel(
                    factory = LoginScreenViewModelFactory(userRepository)
                )
                val authState by loginViewModel.authState.collectAsStateWithLifecycle()


                // AUTH CHECK
                val startDestination = when (authState) {
                    AuthState.AUTHENTICATED -> "home"
                    AuthState.UNAUTHENTICATED -> "login"
                    AuthState.UNKNOWN -> "loading"
                }

                if (authState == AuthState.UNKNOWN) {
                    // TODO: loading screen
                }
                else {
                    // NAVIGATION
                    val navController = rememberNavController()
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
                            startDestination = startDestination,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("home") {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.background
                                ) {
                                    HomeScreen(
                                        onNavigateToWorkoutSetup = { navController.navigate("workoutSetup") }
                                    )
                                }
                            }

                            composable("login") {
                                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.onPrimary) {
                                    LoginScreen(
                                        onRegister = { username -> loginViewModel.registerUser(username) }
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
                                WorkoutScreen(

                                    onNavigateToHome = {
                                        navController.navigate("home")
                                    }
                                )
                            }

                            composable("log") {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.background
                                ) {
                                    LogScreen()
                                }
                            }

                            composable("settings") {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.background
                                ) {
                                    SettingsScreen(
                                        displayName = "placeholder",
                                        onDisplayNameChange = { },
                                        onClearLog = { },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

