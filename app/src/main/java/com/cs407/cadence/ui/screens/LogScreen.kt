package com.cs407.cadence.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.cadence.data.models.WorkoutSession
import com.cs407.cadence.data.repository.FirebaseWorkoutRepository
import com.cs407.cadence.ui.theme.CadenceTheme
import com.cs407.cadence.ui.viewModels.UserViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.items


@Composable
fun LogScreen(
    modifier: Modifier = Modifier
) {

    val userViewModel: UserViewModel = viewModel()
    val uid = userViewModel.userState.collectAsState().value?.uid
    var sessions by remember { mutableStateOf<List<WorkoutSession>>(emptyList()) }

//    val placeholderData = WorkoutSession(
//        id = 1,
//        date = "05/20/2004",
//        bpm = 180,
//        distance = 3.1,
//        time = 30,
//        calories = 100,
//        activity = "Running"
//    )

    LaunchedEffect(uid) {
        if (uid != null) {
            val repo = FirebaseWorkoutRepository()
            sessions = repo.getAllSessions(uid)
                .sortedByDescending { it.date }
        }
    }

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
                    text = "LOG",
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
            //if no logged workouts yet
            if (sessions.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No workouts logged yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                    )
                }
                return@Scaffold
            }

            LazyColumn(
                verticalArrangement = Arrangement
                    .spacedBy(10.dp),
            ) {
                item() {
                    Text(
                        text = "Most recent workout",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                    )
                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                    //MostRecentActivityCard(workoutSession = placeholderData)
                    //MostRecentActivityCard(sessions.first())
                    MostRecentActivityCard(
                        workoutSession = sessions.first()
                    )
                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                }

                item() {
                    Text(
                        text = "Previous workouts",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                    )
                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                    //LogCard(workoutSession = placeholderData)
                }

//                items(9) { index ->
//                    LogCard(workoutSession = placeholderData)
//                }
//
//                item() {}
                items(sessions.drop(1)) { session ->
                    LogCard(session)
                }
            }
        }
    }
}

@Composable
fun LogCard(
    workoutSession: WorkoutSession
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
        ) {
            val formattedDate = try {
                val inputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
                val outputFormatter = DateTimeFormatter.ofPattern("E, MMM d")
                LocalDate.parse(workoutSession.date, inputFormatter).format(outputFormatter)
            } catch (e: Exception) {
                workoutSession.date
            }
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
            )

            Row() {
                Text(
                    text = workoutSession.activity,
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
    }
}

@Composable
fun MostRecentActivityCard(
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
                val formattedDate = try {
                    val inputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
                    val outputFormatter = DateTimeFormatter.ofPattern("E, MMM d")
                    LocalDate.parse(workoutSession.date, inputFormatter).format(outputFormatter)
                } catch (e: Exception) {
                    workoutSession.date
                }
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                )
                Row() {
                    Text(
                        text = workoutSession.activity,
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

@Preview
@Composable
fun LogCardPreview() {
    val placeholderData = WorkoutSession(
        id = 1,
        date = "05/20/2004",
        bpm = 180,
        distance = 3.1,
        time = 30,
        calories = 100,
        activity = "Running"
    )
    CadenceTheme() {
        Box(modifier = Modifier.background(Color.White)){
            LogCard(placeholderData)
        }

    }
}