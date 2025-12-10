package com.cs407.cadence.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.cadence.data.models.WorkoutSession
import com.cs407.cadence.ui.theme.CadenceTheme
import com.cs407.cadence.ui.viewModels.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material.icons.filled.Delete

@Composable
fun LogScreen(
        modifier: Modifier = Modifier,
        workoutViewModel: WorkoutViewModel = viewModel(),
        onNavigateToWorkoutSummary: (WorkoutSession) -> Unit = {}
) {

    val workoutHistory by workoutViewModel.workoutHistory.collectAsState()
    val isLoading by workoutViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) { workoutViewModel.loadWorkoutHistory() }

    Scaffold(
            topBar = {
                Box(
                        modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                ) {
                    Text(
                            text = "Log",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
    ) { innerPadding ->
        Box(modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 20.dp)) {
            if (isLoading) {

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (workoutHistory.isEmpty()) {

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                                imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                                contentDescription = "No workouts",
                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                                modifier = Modifier.size(64.dp)
                        )
                        Text(
                                text = "No workouts yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                        )
                        Text(
                                text = "Complete a workout to see it here!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                        )
                    }
                }
            } else {

                LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        Text(
                                text = "Most recent workout",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                        )
                    }

                    item {
                        MostRecentActivityCard(
                                workoutSession = workoutHistory.first(),
                                onClick = { onNavigateToWorkoutSummary(workoutHistory.first()) },
                                onDelete = { workoutViewModel.deleteWorkout(workoutHistory.first().sessionId)}
                        )
                    }

                    if (workoutHistory.size > 1) {
                        item {
                            Text(
                                    text = "Previous workouts",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                            )
                        }

                        items(workoutHistory.drop(1)) { workout ->
                            LogCard(
                                    workoutSession = workout,
                                    onClick = { onNavigateToWorkoutSummary(workout) },
                                    onDelete = { workoutViewModel.deleteWorkout(workout.sessionId)}
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(10.dp)) }
                }
            }
        }
    }
}

@Composable
fun LogCard(workoutSession: WorkoutSession, onClick: () -> Unit = {}, onDelete: () -> Unit = {}) {
    Box(
            modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start,
            ) {
                val formattedDate =
                        try {
                            val date =
                                    workoutSession.endTime?.toDate()
                                            ?: workoutSession.startTime.toDate()

                            val outputFormatter = SimpleDateFormat("E, MMM d", Locale.getDefault())
                            outputFormatter.format(date)
                        } catch (e: Exception) {

                            val fallbackFormatter =
                                    SimpleDateFormat("E, MMM d", Locale.getDefault())
                            fallbackFormatter.format(workoutSession.startTime.toDate())
                        }

                Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                )

                Row {
                    Text(
                            text = workoutSession.activity.ifEmpty { "Running" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                            text = " for ${workoutSession.time} min",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete, // Import if needed
                    contentDescription = "Delete workout",
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                )
            }

            androidx.compose.material3.IconButton(onClick = onClick) {
                Icon(
                        imageVector =
                                androidx.compose.material.icons.Icons.AutoMirrored.Filled
                                        .ArrowForward,
                        contentDescription = "View workout summary",
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun MostRecentActivityCard(
        modifier: Modifier = Modifier,
        workoutSession: WorkoutSession,
        onClick: () -> Unit = {},
        onDelete: () -> Unit = {}
) {
    Box(
            modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(20.dp)
    ) {
        Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                val formattedDate =
                        try {
                            val date =
                                    workoutSession.endTime?.toDate()
                                            ?: workoutSession.startTime.toDate()

                            val outputFormatter = SimpleDateFormat("E, MMM d", Locale.getDefault())
                            outputFormatter.format(date)
                        } catch (e: Exception) {
                            val fallbackFormatter =
                                    SimpleDateFormat("E, MMM d", Locale.getDefault())
                            fallbackFormatter.format(workoutSession.startTime.toDate())
                        }

                Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                )
                Row {
                    Text(
                            text = workoutSession.activity.ifEmpty { "Running" },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                            text = " for ${workoutSession.time} min",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Row(
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Stat(
                        icon = Icons.Default.Favorite,
                        value = workoutSession.bpm.toString(),
                        label = "bpm",
                )
                Stat(
                        icon = Icons.Default.Place,
                        value = String.format("%.2f", workoutSession.distance),
                        label = "mi",
                )
                Stat(
                        icon = Icons.Default.Timer,
                        value = workoutSession.time.toString(),
                        label = "min",
                )
                Stat(
                        icon = Icons.Default.LocalFireDepartment,
                        value = workoutSession.calories.toString(),
                        label = "cal",
                )
            }
        }
    }
}

@Preview
@Composable
fun LogCardPreview() {
    val placeholderData =
            WorkoutSession(
                    sessionId = "preview-session",
                    userId = "preview-user",
                    bpm = 180,
                    distance = 3.1,
                    time = 30,
                    calories = 100,
                    activity = "Running"
            )
    CadenceTheme { Box(modifier = Modifier.background(Color.White)) { LogCard(placeholderData) } }
}
