package com.cs407.cadence.ui.screens

import android.util.Log
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import com.cs407.cadence.data.models.WorkoutSession
import com.cs407.cadence.data.network.SpotifyService
import com.cs407.cadence.data.repository.WorkoutRepository
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.cadence.R
import com.cs407.cadence.ui.viewModels.WorkoutViewModel
import kotlinx.coroutines.delay
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
    val context = LocalContext.current

    // Calculate workout data based on the timer
    val durationMinutes = ((workoutLength / 60).toInt()).coerceAtLeast(1)
    val (minBpm, maxBpm) = getBpmRangeForActivity(selectedActivity)
    val targetBpm = (minBpm + maxBpm) / 2
    val bpm = targetBpm
    val distanceMiles = 3.1 * (durationMinutes / 30.0)
    val calories = (100 * (durationMinutes / 30.0)).toInt().coerceAtLeast(10)
    val averagePace = if (distanceMiles > 0) durationMinutes / distanceMiles else 0.0

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
                    SpotifyService.playRecommendedTracks(context, selectedGenre, targetBpm, bpmRange = bpmRange)
                }
            },
            onFailure = { throwable ->
                Log.e("WorkoutScreen", "Spotify connection failure: ${throwable.message}")
            }
        )
    }


    LaunchedEffect(isPaused) {
        if (!isPaused) {
            while (true) {
                delay(1000L)
                workoutLength++
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.primary,
        topBar = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                Text(
                    style = MaterialTheme.typography.displayLarge,
                    text = "WORKOUT",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
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

                    Spacer(modifier = Modifier.height(10.dp))

                    MusicCard(isPaused = isPaused)

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
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
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
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(
                                text = "Stop",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

// helper to format stopwatch
fun formatTime(seconds: Long): String {
    val mins = (seconds / 60) % 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}

// helper to get BPM range based on activity type
fun getBpmRangeForActivity(activity: String): Pair<Int, Int> {
    return when (activity.lowercase()) {
        "walking" -> Pair(90, 110)
        "jogging" -> Pair(110, 130)
        "running" -> Pair(130, 150)
        else -> Pair(130, 150) // default to running
    }
}

@Composable
fun MusicCard(isPaused: Boolean = false) {
    var isPlaying by remember { mutableStateOf(true) }
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 20.dp, bottom = 10.dp, start = 20.dp, end = 20.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.default_album_cover),
                contentDescription = "album art",
                modifier = Modifier
                    .width(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                // SONG INFO
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Song title",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Song artist",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // TODO: REPLACE WITH PROGRESS BAR
        Box(
            modifier = Modifier
                .padding(top = 20.dp, bottom = 10.dp)
                .height(3.dp)
                .background(MaterialTheme.colorScheme.onPrimary)
                .fillMaxWidth()
        ) {}

        // MUSIC CONTROLS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // REWIND BUTTON
            IconButton(onClick = { /* TODO */ }) {
                Icon(
                    imageVector = Icons.Default.FastRewind,
                    contentDescription = "Rewind",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }

            // PLAY/PAUSE BUTTON
            IconButton(onClick = { isPlaying = !isPlaying }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }

            // FAST FORWARD BUTTON
            IconButton(onClick = { /* TODO */ }) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "Fast Forward",
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
        modifier = Modifier
            .fillMaxWidth()
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