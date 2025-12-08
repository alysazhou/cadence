package com.cs407.cadence.ui.screens

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.cadence.R
import com.cs407.cadence.data.network.SpotifyService
import com.cs407.cadence.data.repository.WorkoutRepository
import com.cs407.cadence.data.services.LocationService
import com.cs407.cadence.data.services.MovementDetectionService
import com.cs407.cadence.data.services.StepCounterService
import com.cs407.cadence.ui.viewModels.WorkoutViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun WorkoutScreen(
        modifier: Modifier = Modifier,
        onNavigateToHome: () -> Unit,
        onNavigateToResults:
                (Int, Double, Int, Int, List<com.cs407.cadence.data.models.PlayedSong>) -> Unit,
        workoutViewModel: WorkoutViewModel = viewModel(),
        workoutRepository: WorkoutRepository,
        selectedGenre: String,
        selectedActivity: String = "Running",
        autoStopEnabled: Boolean = false
) {
    val currentSession by workoutViewModel.currentSession.collectAsState()
    val scope = rememberCoroutineScope()

    var workoutLength by remember { mutableLongStateOf(0L) }
    var isPaused by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    
    Log.d("WorkoutScreen", "WorkoutScreen started with autoStopEnabled: $autoStopEnabled")

    // Track played songs
    val playedSongs = remember { mutableListOf<com.cs407.cadence.data.models.PlayedSong>() }
    var currentTrackStartTime by remember { mutableLongStateOf(0L) }
    var lastTrackedTrackId by remember { mutableStateOf<String?>(null) }
    var lastTrackedTrackInfo by remember {
        mutableStateOf<com.cs407.cadence.data.models.PlayedSong?>(null)
    }
    val collectedBpms = remember { mutableListOf<Int>() }

    // step tracking
    var distanceMeters by remember { mutableDoubleStateOf(0.0) }
    var currentSteps by remember { mutableIntStateOf(0) }

    // auto-stop movement tracking
    var lastMovementTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var timeSinceLastMovement by remember { mutableLongStateOf(0L) }
    var timeWithMovement by remember { mutableLongStateOf(0L) }
    var wasAutoPaused by remember { mutableStateOf(false) }
    var isCurrentlyMoving by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        wasAutoPaused = false
        isCurrentlyMoving = true
    }
    
    Log.d("WorkoutScreen", "State - wasAutoPaused: $wasAutoPaused, isPaused: $isPaused")

    // initialize services
    LaunchedEffect(Unit) {
        StepCounterService.initialize(context)
        MovementDetectionService.initialize(context)
        
        if (!StepCounterService.isAvailable(context)) {
            Log.w("WorkoutScreen", "Step counter not available on this device")
        }
        if (!MovementDetectionService.isAvailable(context)) {
            Log.w("WorkoutScreen", "Accelerometer not available on this device")
        }
        
        // assume user is moving when workout starts
        lastMovementTime = System.currentTimeMillis()
    }

    // step counting
    LaunchedEffect(isPaused) {
        Log.d("WorkoutScreen", "Step counting LaunchedEffect triggered (isPaused: $isPaused)")
        val available = StepCounterService.isAvailable(context)
        Log.d("WorkoutScreen", "Step counter available: $available")
        
        if (!isPaused && available) {
            Log.d("WorkoutScreen", "Starting step counter...")
            StepCounterService.resetSteps()
            StepCounterService.startCounting(context).collect { update ->
                currentSteps = update.steps
                distanceMeters = update.distanceMeters
                Log.d("WorkoutScreen", "Steps: ${update.steps}, Distance: ${distanceMeters}m")
            }
        } else {
            Log.d("WorkoutScreen", "Step counter NOT started (isPaused: $isPaused, available: $available)")
        }
    }

    // accelerometer movement detection
    LaunchedEffect(autoStopEnabled) {
        if (autoStopEnabled && MovementDetectionService.isAvailable(context)) {
            MovementDetectionService.startDetection(context).collect { movementState ->
                isCurrentlyMoving = movementState.isMoving
                
                if (movementState.isMoving) {
                    lastMovementTime = System.currentTimeMillis()
                    Log.d("WorkoutScreen", "Movement detected via accelerometer (accel: ${movementState.acceleration} m/s²)")
                } else {
                    Log.d("WorkoutScreen", "Stationary detected via accelerometer (accel: ${movementState.acceleration} m/s²)")
                }
            }
        }
    }

    // auto-stop monitoring
    LaunchedEffect(autoStopEnabled, isPaused) {
        if (autoStopEnabled && !isPaused) {
            Log.d("WorkoutScreen", "Auto-stop monitoring started")
            while (isActive) {
                delay(1000) // check every second
                val currentTime = System.currentTimeMillis()
                timeSinceLastMovement = currentTime - lastMovementTime
                
                if (timeSinceLastMovement >= 2000) {
                    Log.d("WorkoutScreen", "No movement for ${timeSinceLastMovement/1000}s (isPaused: $isPaused, wasAutoPaused: $wasAutoPaused)")
                }
                
                // auto-pause if no movement for 3 seconds
                if (timeSinceLastMovement >= 3000 && !isPaused) {
                    Log.d("WorkoutScreen", "Auto-pausing: no movement detected for 3 seconds")
                    isPaused = true
                    wasAutoPaused = true
                    scope.launch {
                        SpotifyService.pause()
                    }
                }
            }
        } else {
            Log.d("WorkoutScreen", "Auto-stop monitoring not active (enabled: $autoStopEnabled, paused: $isPaused)")
        }
    }

    // auto-resume monitoring
    LaunchedEffect(autoStopEnabled, isPaused, wasAutoPaused) {
        if (autoStopEnabled && isPaused && wasAutoPaused) {
            Log.d("WorkoutScreen", "Auto-resume monitoring started")
            while (isActive) {
                delay(500)
                
                // Check if currently moving 
                if (isCurrentlyMoving) {
                    timeWithMovement += 500
                    Log.d("WorkoutScreen", "Movement accumulating: ${timeWithMovement}ms")
                    
                    // resume if movement detected for 3 seconds
                    if (timeWithMovement >= 3000) {
                        Log.d("WorkoutScreen", "Auto-resuming: movement detected for 3 seconds")
                        isPaused = false
                        wasAutoPaused = false
                        timeWithMovement = 0L
                        scope.launch {
                            SpotifyService.resume()
                        }
                    }
                } else {
                    // reset movement timer if not currently moving
                    if (timeWithMovement > 0) {
                        Log.d("WorkoutScreen", "Movement interrupted, resetting timer")
                    }
                    timeWithMovement = 0L
                }
            }
        } else {
            timeWithMovement = 0L
        }
    }

    // calculate workout data
    val durationMinutes = (workoutLength / 60).toInt()
    val durationMinutesForSaving = durationMinutes.coerceAtLeast(1) // Only coerce when saving
    val (minBpm, maxBpm) = getBpmRangeForActivity(selectedActivity)
    val targetBpm = (minBpm + maxBpm) / 2
    val currentTrackBpm by SpotifyService.currentTrackBpm.collectAsState()
    val bpm = currentTrackBpm ?: targetBpm

    // convert meters to miles
    val distanceMiles = distanceMeters / 1609.34
    
    // debug logging
    LaunchedEffect(distanceMeters, workoutLength) {
        if (workoutLength % 5 == 0L) { // log every 5 seconds
            Log.d("WorkoutScreen", "Stats - Steps: $currentSteps, Distance: ${distanceMiles} mi (${distanceMeters}m), Time: ${workoutLength}s")
        }
    }

    // calculate average speed for calorie calculation (updates every second)
    val avgSpeedKmh =
            if (workoutLength > 0 && distanceMeters > 0) {
                (distanceMeters / 1000.0) / (workoutLength / 3600.0)
            } else 0.0

    // improved calorie calculation (dynamically updates with workoutLength and distance)
    val calories = remember(workoutLength, distanceMeters, selectedActivity) {
        // only calculate calories after at least 30 seconds or 10 steps
        if (workoutLength < 30L || currentSteps < 10) {
            0
        } else {
            LocationService.calculateCalories(selectedActivity, durationMinutes, avgSpeedKmh)
        }
    }

    val averagePace = if (distanceMiles > 0) durationMinutes / distanceMiles else 0.0

    val currentTrack by SpotifyService.currentTrack.collectAsState()
    var shouldDismissSplash by remember { mutableStateOf(false) }

    LaunchedEffect(currentTrack) {
        if (currentTrack != null && isLoading) {
            shouldDismissSplash = true
            delay(500)
            isLoading = false
        }
    }

    // song is only "played" after 30s
    LaunchedEffect(currentTrack?.uri, isPaused) {
        currentTrack?.let { track ->
            val trackId = track.uri.split(":").lastOrNull() ?: return@let

            if (lastTrackedTrackId != null && lastTrackedTrackId != trackId) {
                val elapsedTime = (System.currentTimeMillis() - currentTrackStartTime) / 1000
                if (elapsedTime >= 30 && lastTrackedTrackInfo != null) {
                    if (playedSongs.none { it.id == lastTrackedTrackId }) {
                        playedSongs.add(lastTrackedTrackInfo!!)
                        lastTrackedTrackInfo?.bpm?.let { collectedBpms.add(it) }
                    }
                }
            }

            lastTrackedTrackId = trackId
            currentTrackStartTime = System.currentTimeMillis()

            val albumArtUrl =
                    track.imageUri?.raw?.let { uri ->
                        if (uri.startsWith("spotify:image:")) {
                            "https://i.scdn.co/image/${uri.removePrefix("spotify:image:")}"
                        } else uri
                    }

            val trackBpm = SpotifyService.currentTrackBpm.value
            val artistName = track.artist?.name ?: "Unknown Artist"

            Log.d("WorkoutScreen", "Tracking song: ${track.name} by $artistName (BPM: $trackBpm)")

            lastTrackedTrackInfo =
                    com.cs407.cadence.data.models.PlayedSong(
                            id = trackId,
                            name = track.name,
                            artist = artistName,
                            albumArtUrl = albumArtUrl,
                            bpm = trackBpm
                    )
        }
    }

    LaunchedEffect(Unit) {
        val clientId = com.cs407.cadence.BuildConfig.SPOTIFY_CLIENT_ID
        val clientSecret = com.cs407.cadence.BuildConfig.SPOTIFY_CLIENT_SECRET
        val redirectUri = "com.cs407.cadence.auth://callback"

        SpotifyService.buildConnectionParams(clientId, redirectUri, clientSecret)

        SpotifyService.connect(
                context = context,
                onSuccess = {
                    Log.d("WorkoutScreen", "Connection successful. Playing BPM-filtered tracks.")
                    scope.launch {
                        val (minBpm, maxBpm) = getBpmRangeForActivity(selectedActivity)
                        val targetBpm = (minBpm + maxBpm) / 2
                        val bpmRange = (maxBpm - minBpm) / 2
                        SpotifyService.playRecommendedTracks(
                                context,
                                selectedGenre,
                                targetBpm,
                                bpmRange = bpmRange
                        )
                    }
                },
                onFailure = { throwable ->
                    Log.e("WorkoutScreen", "Spotify connection failure: ${throwable.message}")
                    isLoading = false
                }
        )
    }

    LaunchedEffect(isPaused, isLoading) {
        if (!isPaused && !isLoading) {
            while (true) {
                delay(1000L)
                workoutLength++
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
                containerColor = MaterialTheme.colorScheme.primary,
                topBar = {
                    if (!isLoading) {
                        Box(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .height(80.dp)
                                                .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                        ) {
                            Text(
                                    text = "Workout",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
        ) { innerPadding ->
            Box(
                    modifier =
                            modifier.fillMaxSize()
                                    .background(
                                            Brush.verticalGradient(
                                                    colors =
                                                            listOf(
                                                                    MaterialTheme.colorScheme
                                                                            .primary,
                                                                    MaterialTheme.colorScheme
                                                                            .secondary.copy(
                                                                            alpha = 0.3f
                                                                    )
                                                            )
                                            )
                                    )
            ) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top,
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(innerPadding)
                                        .padding(horizontal = 20.dp)
                                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    // ACTIVITY TIMER
                    Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Workout duration",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(48.dp)
                        )
                        Text(
                                text = formatTime(workoutLength),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.secondary,
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        MusicCard(
                                isPaused = isPaused,
                                onPlayPauseClick = {
                                    isPaused = !isPaused
                                    if (isPaused) {
                                        SpotifyService.pause()
                                        scope.launch {
                                            currentSession?.let { workoutViewModel.pauseWorkout() }
                                        }
                                    } else {
                                        SpotifyService.resume()
                                        scope.launch {
                                            currentSession?.let { workoutViewModel.resumeWorkout() }
                                        }
                                    }
                                },
                                onSkipNext = { SpotifyService.playNextTrack() },
                                onSkipPrevious = { SpotifyService.skipToPosition(0) }
                        )

                        // TEMPO
                        WorkoutStatCard(
                                icon = Icons.Default.MusicNote,
                                label = "Tempo",
                                value = "$bpm beats/min"
                        )

                        // DISTANCE
                        WorkoutStatCard(
                                icon = Icons.Default.Place,
                                label = "Distance",
                                value = "${String.format("%.2f", distanceMiles)} mi"
                        )

                        // CALORIES
                        WorkoutStatCard(
                                icon = Icons.Default.LocalFireDepartment,
                                label = "Calories",
                                value = "$calories cal"
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // BUTTONS
                    Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                                shape = RoundedCornerShape(100.dp),
                                onClick = {
                                    isPaused = !isPaused
                                    // Reset auto-pause state when manually pausing/resuming
                                    wasAutoPaused = false
                                    Log.d("WorkoutScreen", "Manual ${if (isPaused) "pause" else "resume"} - reset wasAutoPaused")

                                    if (isPaused) {
                                        SpotifyService.pause()
                                    } else {
                                        SpotifyService.resume()
                                        // Reset movement timer on manual resume
                                        lastMovementTime = System.currentTimeMillis()
                                    }

                                    scope.launch {
                                        currentSession?.let { session ->
                                            if (isPaused) {
                                                workoutViewModel.pauseWorkout()
                                            } else {
                                                workoutViewModel.resumeWorkout()
                                            }
                                        }
                                    }
                                },
                                contentPadding =
                                        PaddingValues(vertical = 15.dp, horizontal = 30.dp),
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor = Color.Transparent
                                        ),
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(
                                    text = if (isPaused) "Resume" else "Pause",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        // END WORKOUT BUTTON
                        Button(
                                shape = RoundedCornerShape(100.dp),
                                onClick = {
                                    if (lastTrackedTrackId != null && lastTrackedTrackInfo != null
                                    ) {
                                        val elapsedTime =
                                                (System.currentTimeMillis() -
                                                        currentTrackStartTime) / 1000
                                        if (elapsedTime >= 30) {
                                            if (playedSongs.none { it.id == lastTrackedTrackId }) {
                                                playedSongs.add(lastTrackedTrackInfo!!)
                                                lastTrackedTrackInfo?.bpm?.let {
                                                    collectedBpms.add(it)
                                                }
                                            }
                                        }
                                    }

                                    // Stop music
                                    SpotifyService.pause()
                                    SpotifyService.disconnect()

                                    // Calculate average BPM
                                    val averageBpm =
                                            if (collectedBpms.isNotEmpty()) {
                                                collectedBpms.average().toInt()
                                            } else {
                                                targetBpm
                                            }

                                    scope.launch {
                                        val playedSongsAsMap =
                                                playedSongs.map { song ->
                                                    mapOf(
                                                            "id" to song.id,
                                                            "name" to song.name,
                                                            "artist" to song.artist,
                                                            "albumArtUrl" to song.albumArtUrl,
                                                            "bpm" to song.bpm
                                                    )
                                                }

                                        currentSession?.let { session ->
                                            workoutViewModel.endWorkout(
                                                    time = durationMinutesForSaving,
                                                    distance = distanceMiles,
                                                    averagePace = averagePace,
                                                    calories = calories,
                                                    bpm = averageBpm,
                                                    playedSongsDetails = playedSongsAsMap
                                            )
                                        }

                                        // Navigate to results screen
                                        onNavigateToResults(
                                                durationMinutesForSaving,
                                                distanceMiles,
                                                calories,
                                                averageBpm,
                                                playedSongs.toList()
                                        )
                                    }
                                },
                                contentPadding =
                                        PaddingValues(vertical = 15.dp, horizontal = 30.dp),
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary
                                        )
                        ) {
                            Text(
                                    text = "Stop",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Show loading splash screen
            if (isLoading) {
                WorkoutLoadingSplash(shouldDismiss = shouldDismissSplash)
            }
        }
    }
}

fun formatTime(seconds: Long): String {
    val mins = (seconds / 60) % 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}

fun getBpmRangeForActivity(activity: String): Pair<Int, Int> {
    return when (activity.lowercase()) {
        "walking" -> Pair(100, 120)
        "jogging" -> Pair(120, 140)
        "running" -> Pair(140, 160)
        else -> Pair(140, 160) // default to running
    }
}

@Composable
fun MusicCard(
        isPaused: Boolean = false,
        onPlayPauseClick: () -> Unit = {},
        onSkipNext: () -> Unit = {},
        onSkipPrevious: () -> Unit = {}
) {
    val currentTrack by SpotifyService.currentTrack.collectAsState()
    val playbackPosition by SpotifyService.playbackPosition.collectAsState()
    val trackDuration by SpotifyService.trackDuration.collectAsState()

    var localPosition by remember { mutableStateOf(0L) }
    var lastUpdateTime by remember { mutableStateOf(System.currentTimeMillis()) }

    val trackName = currentTrack?.name ?: "Loading..."
    val artistName = currentTrack?.artist?.name ?: ""

    // Convert Spotify image URI to HTTP URL
    val albumArtUrl =
            currentTrack?.imageUri?.raw?.let { uri ->
                if (uri.startsWith("spotify:image:")) {
                    "https://i.scdn.co/image/${uri.removePrefix("spotify:image:")}"
                } else uri
            }

    LaunchedEffect(playbackPosition) {
        localPosition = playbackPosition
        lastUpdateTime = System.currentTimeMillis()
    }

    LaunchedEffect(currentTrack, isPaused) {
        if (currentTrack != null && !isPaused) {
            while (isActive) {
                delay(100L) // Update every 100ms
                val elapsed = System.currentTimeMillis() - lastUpdateTime
                localPosition = (playbackPosition + elapsed).coerceAtMost(trackDuration)
            }
        }
    }

    // Calculate progress
    val progress =
            if (trackDuration > 0) {
                (localPosition.toFloat() / trackDuration.toFloat()).coerceIn(0f, 1f)
            } else 0f

    LaunchedEffect(currentTrack) {
        if (currentTrack != null) {
            Log.d("MusicCard", "Track updated: $trackName by $artistName, Album art: $albumArtUrl")
        }
    }

    Column(
            modifier =
                    Modifier.clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(top = 20.dp, bottom = 10.dp, start = 20.dp, end = 20.dp),
    ) {
        Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
        ) {
            coil.compose.AsyncImage(
                    model = albumArtUrl,
                    contentDescription = "album art",
                    placeholder = painterResource(id = R.drawable.default_album_cover),
                    error = painterResource(id = R.drawable.default_album_cover),
                    modifier = Modifier.width(100.dp).height(100.dp).clip(RoundedCornerShape(8.dp))
            )
            Column(
                    verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                // SONG INFO
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                            text = trackName,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 2
                    )
                    Text(
                            text = artistName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 1
                    )
                }
            }
        }

        // Progress bar with live tracking
        BoxWithConstraints(
                modifier =
                        Modifier.padding(top = 20.dp, bottom = 10.dp).fillMaxWidth().height(20.dp),
                contentAlignment = Alignment.CenterStart
        ) {
            val maxWidth = maxWidth - 40.dp

            // Background track
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .height(3.dp)
                                    .background(
                                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                                            RoundedCornerShape(1.5.dp)
                                    )
            )

            // Progress track
            Box(
                    modifier =
                            Modifier.fillMaxWidth(progress)
                                    .height(3.dp)
                                    .background(
                                            MaterialTheme.colorScheme.onPrimary,
                                            RoundedCornerShape(1.5.dp)
                                    )
            )

            // Progress indicator
            Box(
                    modifier =
                            Modifier.offset(x = (maxWidth * progress) - 6.dp)
                                    .size(12.dp)
                                    .background(
                                            MaterialTheme.colorScheme.onPrimary,
                                            androidx.compose.foundation.shape.CircleShape
                                    )
            )
        }

        // MUSIC CONTROLS
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
        ) {
            // REWIND BUTTON
            IconButton(onClick = onSkipPrevious) {
                Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Skip to beginning",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(30.dp)
                )
            }

            // PLAY/PAUSE BUTTON
            IconButton(onClick = onPlayPauseClick) {
                Icon(
                        imageVector =
                                if (!isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (!isPaused) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(30.dp)
                )
            }

            // SKIP NEXT BUTTON
            IconButton(onClick = onSkipNext) {
                Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Skip to next",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
fun WorkoutStatCard(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        label: String,
        value: String
) {
    Box(
            modifier =
                    Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 20.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
    ) {
        Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(60.dp).align(Alignment.CenterEnd).alpha(0.15f),
                tint = MaterialTheme.colorScheme.onPrimary
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start,
            ) {
                Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                )
                Text(
                        text = value,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun WorkoutLoadingSplash(shouldDismiss: Boolean = false, onSplashComplete: () -> Unit = {}) {
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(shouldDismiss) {
        if (shouldDismiss) {
            alpha.animateTo(targetValue = 0f, animationSpec = tween(durationMillis = 500))
            onSplashComplete()
        }
    }

    Box(
            modifier =
                    Modifier.fillMaxSize()
                            .zIndex(10f)
                            .graphicsLayer { this.alpha = alpha.value }
                            .background(MaterialTheme.colorScheme.primary)
                            .background(
                                    Brush.verticalGradient(
                                            colors =
                                                    listOf(
                                                            MaterialTheme.colorScheme.primary,
                                                            MaterialTheme.colorScheme.secondary
                                                                    .copy(alpha = 0.3f)
                                                    )
                                    )
                            ),
            contentAlignment = Alignment.Center
    ) {
        // logo in center
        val logoImage =
                if (isSystemInDarkTheme()) {
                    com.cs407.cadence.R.drawable.cadence_logo_turquoise
                } else {
                    com.cs407.cadence.R.drawable.cadence_logo_blue
                }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                    painter = painterResource(logoImage),
                    contentDescription = "Cadence logo",
                    modifier = Modifier.width(180.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                    text = "CADENCE",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
