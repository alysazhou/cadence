package com.cs407.cadence

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cs407.cadence.data.SpotifyAuthState
import com.cs407.cadence.data.network.SpotifyAuthManager
import com.cs407.cadence.data.repository.WorkoutRepository
import com.cs407.cadence.ui.navigation.BottomNav
import com.cs407.cadence.ui.screens.*
import com.cs407.cadence.ui.screens.HomeScreen
import com.cs407.cadence.ui.screens.LogScreen
import com.cs407.cadence.ui.screens.LoginScreen
import com.cs407.cadence.ui.screens.MusicAuthScreen
import com.cs407.cadence.ui.screens.SetNameScreen
import com.cs407.cadence.ui.screens.SettingsScreen
import com.cs407.cadence.ui.screens.SplashScreen
import com.cs407.cadence.ui.screens.WorkoutScreen
import com.cs407.cadence.ui.screens.WorkoutSetupScreen
import com.cs407.cadence.ui.theme.CadenceTheme
import com.cs407.cadence.ui.viewModels.UserViewModel
import com.cs407.cadence.ui.viewModels.WorkoutViewModel

class MainActivity : ComponentActivity() {
    private val userViewModel: UserViewModel by viewModels()
    private val workoutViewModel: WorkoutViewModel by viewModels()
    private val workoutRepository = WorkoutRepository()

    private lateinit var spotifyAuthManager: SpotifyAuthManager
    
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            android.util.Log.d("MainActivity", "Location permission granted")
        } else {
            android.util.Log.w("MainActivity", "Location permission denied")
        }
    }
    
    private val activityRecognitionPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            android.util.Log.d("MainActivity", "Activity recognition permission granted")
        } else {
            android.util.Log.w("MainActivity", "Activity recognition permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        spotifyAuthManager = SpotifyAuthManager(this)


        com.cs407.cadence.data.network.RapidApiClient.setApiKey(BuildConfig.RAPIDAPI_KEY)
        
        // Check and request permissions on every app start
        checkLocationPermission()
        checkActivityRecognitionPermission()

        setContent {
            CadenceTheme {
                CadenceApp(
                        userViewModel,
                        workoutViewModel,
                        workoutRepository,
                        onSpotifyLogin = { spotifyAuthManager.startAuth(this) }
                )
            }
        }
    }
    
    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                android.util.Log.d("MainActivity", "Location permission already granted")
            }
            else -> {
                // Request permission
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
    
    private fun checkActivityRecognitionPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    android.util.Log.d("MainActivity", "Activity recognition permission already granted")
                }
                else -> {
                    // Request permission
                    activityRecognitionPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && data != null) {
            spotifyAuthManager.handleAuthResponse(
                    data,
                    onSuccess = { accessToken ->
                        SpotifyAuthState.saveAccessToken(this, accessToken)
                    },
                    onError = { error ->
                    }
            )
        }
    }
}

@Composable
fun CadenceApp(
        userViewModel: UserViewModel,
        workoutViewModel: WorkoutViewModel,
        workoutRepository: WorkoutRepository,
        onSpotifyLogin: (() -> Unit)? = null
) {
    val userState by userViewModel.userState.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    var showSplash by remember { mutableStateOf(true) }
    var isSpotifyAuthenticated by remember {
        mutableStateOf(SpotifyAuthState.isAuthenticated(navController.context))
    }
    var isContentReady by remember { mutableStateOf(false) }

    // navigate to appropriate screen while splash is showing
    LaunchedEffect(userState?.uid) {
        val destination =
                when {
                    userState == null -> "login"
                    userState?.name.isNullOrEmpty() -> "setName"
                    !isSpotifyAuthenticated -> "musicAuth"
                    else -> "home"
                }

        navController.navigate(destination) {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
        }

        // content is ready after navigation
        isContentReady = true
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // overlay splash on top of content
    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
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
                    LoginScreen(viewModel = userViewModel, onSpotifyLogin = onSpotifyLogin)
                }

                composable("setName") {
                    SetNameScreen(
                            viewModel = userViewModel,
                            onNavigateToHome = {
                                navController.navigate("musicAuth") {
                                    popUpTo("setName") { inclusive = true }
                                }
                            }
                    )
                }

                composable("musicAuth") {
                    MusicAuthScreen(
                            onAuthComplete = {
                                isSpotifyAuthenticated = true
                                navController.navigate("home") {
                                    popUpTo("musicAuth") { inclusive = true }
                                }
                            },
                            onSkip = {
                                navController.navigate("home") {
                                    popUpTo("musicAuth") { inclusive = true }
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
                                // I added navigation callback for the Map button,
                                // which allows HomeScreen to trigger navigation to the new
                                // MapScreen
                                navToMap = { navController.navigate("map") },
                                onNavigateToWorkoutSetup = {
                                    navController.navigate("workoutSetup")
                                },
                                username = userState?.name,
                                workoutViewModel = workoutViewModel,
                                workoutRepository = workoutRepository,
                                modifier = Modifier
                        )
                    }
                }

                composable("workoutSetup") {
                    WorkoutSetupScreen(
                            workoutViewModel = workoutViewModel,
                            onNavigateToWorkout = { selectedGenre, selectedActivity ->
                                navController.navigate("workout/$selectedGenre/$selectedActivity")
                            },
                            onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                        route = "workout/{genre}/{activity}",
                        arguments =
                                listOf(
                                        navArgument("genre") { type = NavType.StringType },
                                        navArgument("activity") { type = NavType.StringType }
                                )
                ) { backStackEntry ->
                    val genre = backStackEntry.arguments?.getString("genre") ?: "pop"
                    val activity = backStackEntry.arguments?.getString("activity") ?: "Running"
                    val context = LocalContext.current
                    val autoStopSetting = com.cs407.cadence.data.AppSettings.isAutoStopEnabled(context)
                    android.util.Log.d("MainActivity", "Starting workout with autoStopEnabled: $autoStopSetting")
                    WorkoutScreen(
                            onNavigateToHome = { navController.navigate("home") },
                            onNavigateToResults = { time, distance, calories, avgBpm, songs ->
                                // Serialize the data and navigate
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                        "workout_time",
                                        time
                                )
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                        "workout_distance",
                                        distance
                                )
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                        "workout_calories",
                                        calories
                                )
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                        "workout_bpm",
                                        avgBpm
                                )
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                        "workout_songs",
                                        ArrayList(songs)
                                )
                                navController.navigate("workoutResults")
                            },
                            workoutViewModel = workoutViewModel,
                            workoutRepository = workoutRepository,
                            selectedGenre = genre,
                            selectedActivity = activity,
                            autoStopEnabled = autoStopSetting
                    )
                }

                composable("workoutResults") {
                    val previousBackStackEntry = navController.previousBackStackEntry
                    val time =
                            previousBackStackEntry?.savedStateHandle?.get<Int>("workout_time") ?: 0
                    val distance =
                            previousBackStackEntry?.savedStateHandle?.get<Double>(
                                    "workout_distance"
                            )
                                    ?: 0.0
                    val calories =
                            previousBackStackEntry?.savedStateHandle?.get<Int>("workout_calories")
                                    ?: 0
                    val avgBpm =
                            previousBackStackEntry?.savedStateHandle?.get<Int>("workout_bpm") ?: 0
                    val songs =
                            previousBackStackEntry?.savedStateHandle?.get<
                                    ArrayList<com.cs407.cadence.data.models.PlayedSong>>(
                                    "workout_songs"
                            )
                                    ?: arrayListOf()

                    WorkoutResultsScreen(
                            time = time,
                            distance = distance,
                            calories = calories,
                            averageBpm = avgBpm,
                            playedSongs = songs,
                            onDone = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = false }
                                }
                            }
                    )
                }

                composable("workoutSummaryFromLog") {
                    val previousBackStackEntry = navController.previousBackStackEntry
                    val time =
                            previousBackStackEntry?.savedStateHandle?.get<Int>("workout_time") ?: 0
                    val distance =
                            previousBackStackEntry?.savedStateHandle?.get<Double>(
                                    "workout_distance"
                            )
                                    ?: 0.0
                    val calories =
                            previousBackStackEntry?.savedStateHandle?.get<Int>("workout_calories")
                                    ?: 0
                    val avgBpm =
                            previousBackStackEntry?.savedStateHandle?.get<Int>("workout_bpm") ?: 0
                    val songs =
                            previousBackStackEntry?.savedStateHandle?.get<
                                    ArrayList<com.cs407.cadence.data.models.PlayedSong>>(
                                    "workout_songs"
                            )
                                    ?: arrayListOf()

                    WorkoutSummaryFromLogScreen(
                            time = time,
                            distance = distance,
                            calories = calories,
                            averageBpm = avgBpm,
                            playedSongs = songs,
                            onBack = { navController.popBackStack() }
                    )
                }

                composable("log") {
                    LogScreen(
                            workoutViewModel = workoutViewModel,
                            onNavigateToWorkoutSummary = { session ->
                                // Convert saved Map data back to PlayedSong objects
                                val playedSongs =
                                        session.playedSongsDetails.mapNotNull { map ->
                                            try {
                                                com.cs407.cadence.data.models.PlayedSong(
                                                        id = map["id"] as? String ?: "",
                                                        name = map["name"] as? String ?: "",
                                                        artist = map["artist"] as? String ?: "",
                                                        albumArtUrl = map["albumArtUrl"] as? String,
                                                        bpm = (map["bpm"] as? Number)?.toInt()
                                                )
                                            } catch (e: Exception) {
                                                null
                                            }
                                        }

                                // Navigate to summary from log with session data
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                        "workout_time",
                                        session.time
                                )
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                        "workout_distance",
                                        session.distance
                                )
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                        "workout_calories",
                                        session.calories
                                )
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                        "workout_bpm",
                                        session.bpm
                                )
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                        "workout_songs",
                                        ArrayList(playedSongs)
                                )
                                navController.navigate("workoutSummaryFromLog")
                            }
                    )
                }

                composable("settings") {
                    var isMusicConnected by remember {
                        mutableStateOf(SpotifyAuthState.isTokenValid(navController.context))
                    }
                    SettingsScreen(
                            viewModel = userViewModel,
                            displayName = userState?.name ?: "",
                            onDisplayNameChange = { newName: String ->
                                userViewModel.setDisplayName(newName)
                            },
                            onClearLog = { workoutViewModel.clearAllHistory() },
                            isMusicConnected = isMusicConnected,
                            onMusicAuth = { isConnected ->
                                if (isConnected) {
                                    isSpotifyAuthenticated = true
                                }
                                isMusicConnected = isConnected
                            },
                            onSpotifyLogin = onSpotifyLogin
                    )
                }

                // This is a new composable route for the Map screen that registers the
                // "map" route w/ the NavHost (so the app can navigate to MapScreen)

                // This route is triggered by the (temp) "Open Map" button on HomeScreen
                composable("map") { MapScreen(navController = navController) }
            }
        }

        // show splash overlay on top
        if (showSplash) {
            SplashScreen(onSplashComplete = { showSplash = false })
        }
    }
}
