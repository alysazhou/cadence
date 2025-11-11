package com.cs407.cadence.ui.screens

import android.app.Application
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.cadence.R
import com.cs407.cadence.data.models.WorkoutSession
import com.cs407.cadence.ui.viewModels.HomeScreenViewModel
import com.cs407.cadence.ui.viewModels.HomeScreenViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel = viewModel(
        factory = HomeScreenViewModelFactory(LocalContext.current.applicationContext as Application)
    ),
    onNavigateToWorkoutSetup: () -> Unit,
    username: String?
) {
    val placeholderData = WorkoutSession(
        id = 1,
        date = "05/20/2004",
        bpm = 180,
        distance = 3.1,
        time = 30,
        calories = 100,
        activity = "Running"
    )

    val displayName = username ?: "User"
    val days = (-2..2).map { LocalDate.now().plusDays(it.toLong()) }

    // hardcoded values
    val workoutDates = listOf(
        LocalDate.now().minusDays(2),
        LocalDate.now()
    )

    Scaffold(
        topBar = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                Text(
                    style = MaterialTheme.typography.displayLarge,
                    text = "CADENCE",
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // HEADER
                Column(
                ) {
                    Text(
                        text = "Welcome back,",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text =  displayName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text =  "Let's keep a streak going!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // WORKOUT CALENDAR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        days.forEach { day ->
                            val hasWorkout = workoutDates.contains(day) && day.isBefore(LocalDate.now().plusDays(1))
                            DayCard(
                                modifier = Modifier.weight(1f),
                                date = day,
                                hasWorkout = hasWorkout,
                                isToday = day.isEqual(LocalDate.now())
                            )
                        }
                    }

                    // START BUTTON (now navigates to setup screen)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clickable { onNavigateToWorkoutSetup() } // Updated navigation
                    ) {
                        StartButton()
                    }

                    // LAST WORKOUT
                    RecentActivityCard(workoutSession = placeholderData)
                }
            }
        }
    }
}

// cards that show the day of the week
@Composable
fun DayCard(modifier: Modifier = Modifier, date: LocalDate, hasWorkout: Boolean, isToday: Boolean) {
    val cardColor = if (isToday) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.surface
    }

    val dateColor = if (isToday) {
        MaterialTheme.colorScheme.onSecondary
    } else {
        MaterialTheme.colorScheme.onSurface
    }


    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT,
        Locale.getDefault())
    val dayOfMonth = date.dayOfMonth.toString()

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = null
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 15.dp, horizontal = 10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayOfWeek.capitalize(),
                style = MaterialTheme.typography.labelSmall,
                color = dateColor
            )
            Text(
                text = dayOfMonth,
                style = MaterialTheme.typography.titleLarge,
                color = dateColor
            )

            Spacer(modifier = Modifier.height(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                contentDescription = "Workout completed",
                tint = dateColor,
                modifier = Modifier
                    .size(20.dp)
                    .alpha(if (hasWorkout) 1f else 0f)
            )
        }
    }
}

@Composable
fun StartButton() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .height(160.dp)
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.secondary
            ),
    ) {}
    val infiniteTransition = rememberInfiniteTransition(label = "infinite rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(vertical = 25.dp),
        ) {
            Text(
                text = "Start\nworkout",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondary
            )
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Start Icon",
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
        Image(
            painter = painterResource(id = R.drawable.play_button),
            contentDescription = "start button",
            modifier = Modifier
                .height(180.dp)
                .rotate(rotation)
        )
    }
}

@Composable
fun RecentActivityCard(
    modifier: Modifier = Modifier,
    workoutSession: WorkoutSession,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "Recent activity",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                )
                val formattedDate = try {
                    val inputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
                    val outputFormatter = DateTimeFormatter.ofPattern("E, MMM d")
                    LocalDate.parse(workoutSession.date, inputFormatter).format(outputFormatter)
                } catch (e: Exception) {
                    workoutSession.date
                }
                Text(
                    text = formattedDate,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // row of stats
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
                    value = workoutSession.distance.toString(),
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

@Composable
fun Stat(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(30.dp)
        )
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
        )
    }
}
