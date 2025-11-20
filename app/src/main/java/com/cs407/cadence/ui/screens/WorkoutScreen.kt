package com.cs407.cadence.ui.screens

import android.util.Log
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cs407.cadence.data.models.WorkoutSession
import com.cs407.cadence.data.network.SpotifyService
import com.cs407.cadence.data.repository.WorkoutRepository
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun WorkoutScreen(
    modifier: Modifier = Modifier,
    onNavigateToHome: () -> Unit,
    workoutRepository: WorkoutRepository,
    selectedGenre: String
) {
    var workoutLength by remember { mutableStateOf(0L) }
    val context = LocalContext.current

    // Calculate workout data based on the timer
    val durationMinutes = (workoutLength / 60).toInt().coerceAtLeast(1)
    val bpm = 180 // hardcoded for now
    val distanceMiles = 3.1 * (durationMinutes / 30.0)
    val calories = (100 * (durationMinutes / 30.0)).toInt().coerceAtLeast(10)

    LaunchedEffect(Unit) {
        val applicationInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        val metadata = applicationInfo.metaData
        val clientId = metadata.getString("com.cs407.cadence.SPOTIFY_CLIENT_ID") ?: ""
        val redirectUri = "com.cs407.cadence.auth://callback"

        SpotifyService.buildConnectionParams(clientId, redirectUri)


        SpotifyService.connect(
            context = context,
            onSuccess = {
                Log.d("WorkoutScreen", "Connection successful. Playing recommended tracks.")
                SpotifyService.playRecommendedTracks(selectedGenre, bpm)
            },
            onFailure = { throwable ->
                println("connection failure: ${throwable.message}")
            }
        )
    }

    // disconnect on finish workout
    DisposableEffect(Unit) {
        onDispose {
            SpotifyService.disconnect()
        }
    }

    // timer
    LaunchedEffect(key1 = true) {
        while (true) {
            delay(1000L)
            workoutLength++
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

                    // FIX 2: Removed call to non-existent 'MusicCard()' composable
                    // MusicCard()

                    // TEMPO
                    WorkoutStatCard(icon = Icons.Default.MusicNote, label = "Tempo", value = "$bpm beats/min")

                    // DISTANCE
                    WorkoutStatCard(icon = Icons.Default.Place, label = "Distance", value = "${String.format("%.1f", distanceMiles)} mi")
                }

                // FINISH WORKOUT BUTTON
                Button(
                    onClick = {
                        SpotifyService.pause()
                        SpotifyService.disconnect()
                        val newSession = WorkoutSession(
                            date = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                            bpm = bpm,
                            distance = distanceMiles,
                            time = durationMinutes,
                            calories = calories,
                            activity = "Running"
                        )
                        workoutRepository.createSession(newSession)
                        onNavigateToHome()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        text = "Finish Workout",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
    }
}

fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}

@Composable
fun WorkoutStatCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
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