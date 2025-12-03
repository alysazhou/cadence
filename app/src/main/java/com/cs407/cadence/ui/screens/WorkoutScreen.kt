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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.cs407.cadence.ui.viewModels.WorkoutViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun WorkoutScreen(
        modifier: Modifier = Modifier,
        onNavigateToHome: () -> Unit,
        workoutViewModel: WorkoutViewModel = viewModel(),
        workoutRepository: WorkoutRepository,
        selectedGenre: String,
        selectedActivity: String = "Running"
) {
    val currentSession by workoutViewModel.currentSession.collectAsState()
    val scope = rememberCoroutineScope()

    var workoutLength by remember { mutableLongStateOf(0L) }
    var isPaused by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Calculate workout data based on the timer
    val durationMinutes = ((workoutLength / 60).toInt()).coerceAtLeast(1)
    val (minBpm, maxBpm) = getBpmRangeForActivity(selectedActivity)
    val targetBpm = (minBpm + maxBpm) / 2
    val currentTrackBpm by SpotifyService.currentTrackBpm.collectAsState()
    val bpm = currentTrackBpm ?: targetBpm
    val distanceMiles = 3.1 * (durationMinutes / 30.0)
    val calories = (100 * (durationMinutes / 30.0)).toInt().coerceAtLeast(10)
    val averagePace = if (distanceMiles > 0) durationMinutes / distanceMiles else 0.0

    val currentTrack by SpotifyService.currentTrack.collectAsState()
    var shouldDismissSplash by remember { mutableStateOf(false) }

    LaunchedEffect(currentTrack) {
        if (currentTrack != null && isLoading) {
            // First track has loaded, trigger splash screen fade
            shouldDismissSplash = true
            delay(500)
            isLoading = false
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
                    isLoading = false // Stop loading even on failure
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

    Scaffold(containerColor = MaterialTheme.colorScheme.primary) { innerPadding ->
        Box(
                modifier =
                        modifier.fillMaxSize()
                                .background(
                                        Brush.verticalGradient(
                                                colors =
                                                        listOf(
                                                                MaterialTheme.colorScheme.primary,
                                                                MaterialTheme.colorScheme.secondary
                                                                        .copy(alpha = 0.3f)
                                                        )
                                        )
                                )
        ) {
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier =
                            Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp)
            ) {
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
                            onSkipPrevious = {
                                // Spotify doesn't support previous track easily, so we'll skip back
                                // in current track
                                SpotifyService.skipToPosition(0)
                            }
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
                            value = "${String.format("%.1f", distanceMiles)} mi"
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

                                // Pause or resume Spotify
                                if (isPaused) {
                                    SpotifyService.pause()
                                } else {
                                    SpotifyService.resume()
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
                            contentPadding = PaddingValues(vertical = 15.dp, horizontal = 30.dp),
                            colors =
                                    ButtonDefaults.buttonColors(containerColor = Color.Transparent),
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
                                // Stop Spotify music
                                SpotifyService.pause()
                                SpotifyService.disconnect()

                                scope.launch {
                                    currentSession?.let { session ->
                                        workoutViewModel.endWorkout(
                                                time = durationMinutes,
                                                distance = distanceMiles,
                                                averagePace = averagePace,
                                                calories = calories,
                                                bpm = bpm
                                        )
                                    }
                                    onNavigateToHome()
                                }
                            },
                            contentPadding = PaddingValues(vertical = 15.dp, horizontal = 30.dp),
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
            }

            // Show loading splash screen while waiting for first track
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
        "walking" -> Pair(120, 140)
        "jogging" -> Pair(140, 160)
        "running" -> Pair(160, 180)
        else -> Pair(160, 180) // default to running
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

    // Local state for smooth progress animation
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

    // Update local position when StateFlow changes
    LaunchedEffect(playbackPosition) {
        localPosition = playbackPosition
        lastUpdateTime = System.currentTimeMillis()
    }

    // Continuous polling for smooth progress animation
    LaunchedEffect(currentTrack, isPaused) {
        if (currentTrack != null && !isPaused) {
            while (isActive) {
                delay(100L) // Update every 100ms
                val elapsed = System.currentTimeMillis() - lastUpdateTime
                localPosition = (playbackPosition + elapsed).coerceAtMost(trackDuration)
            }
        }
    }

    // Calculate progress (0.0 to 1.0)
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
            val maxWidth = maxWidth - 40.dp // Account for padding

            // Background track (opaque)
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .height(3.dp)
                                    .background(
                                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                                            RoundedCornerShape(1.5.dp)
                                    )
            )

            // Progress track (white)
            Box(
                    modifier =
                            Modifier.fillMaxWidth(progress)
                                    .height(3.dp)
                                    .background(
                                            MaterialTheme.colorScheme.onPrimary,
                                            RoundedCornerShape(1.5.dp)
                                    )
            )

            // Progress indicator (white circle)
            Box(
                    modifier =
                            Modifier.offset(x = maxWidth * progress)
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
            // REWIND BUTTON (skip to beginning of track)
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
            // fade out over 500ms
            alpha.animateTo(targetValue = 0f, animationSpec = tween(durationMillis = 500))
            // notify that splash is complete
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
