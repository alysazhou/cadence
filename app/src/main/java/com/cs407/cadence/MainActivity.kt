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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cs407.cadence.data.models.WorkoutSession
import com.cs407.cadence.data.repository.WorkoutRepository
import com.cs407.cadence.ui.navigation.BottomNav
import com.cs407.cadence.ui.screens.HomeScreen
import com.cs407.cadence.ui.screens.WorkoutScreen
import com.cs407.cadence.ui.screens.LogScreen
import com.cs407.cadence.ui.theme.CadenceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // backend test that runs once when the app starts

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

        // end of test ... above code only prints to Logcat, not UI

        setContent {
            CadenceTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    containerColor = Color.Transparent,
                    bottomBar = {
                        if (currentRoute == "home" || currentRoute == "log") {
                            BottomNav(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.background
                            ) {
                                HomeScreen(
                                    onNavigateToWorkout = { navController.navigate("workout") }
                                )
                            }
                        }

                        composable("workout") {
                            WorkoutScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
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
                    }
                }
            }
        }
    }
}

