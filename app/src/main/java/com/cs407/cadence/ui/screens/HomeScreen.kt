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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.cadence.R
import com.cs407.cadence.data.ActivityRepository
import com.cs407.cadence.ui.components.ActivitySelectionDialog
import com.cs407.cadence.ui.components.GenreSelectionDialog
import com.cs407.cadence.ui.components.LogCard
import com.cs407.cadence.data.models.WorkoutSession


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel = viewModel(
        factory = HomeScreenViewModelFactory(androidx.compose.ui.platform.LocalContext.current.applicationContext as Application)
    ),
    onNavigateToWorkout: () -> Unit
) {
    val placeholderData = WorkoutSession(
        id = 1,
        date = "00/00/0000",
        bpm = 180,
        distance = 3.1,
        time = 30,
        calories = 100
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = ActivityRepository.findActivityByName(uiState.selectedActivity)

    Scaffold(
        topBar = {

        }
    ) { innerPadding ->
        Box(

            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {

            // ACTIVITY SELECTION DIALOG
            if (uiState.showActivitySelector) {
                ActivitySelectionDialog(
                    activities = ActivityRepository.getAllActivities(),
                    currentSelection = uiState.selectedActivity,
                    onDismiss = { viewModel.onActivitySelectorDismiss() },
                    onSelect = { activityName -> viewModel.onActivitySelected(activityName) }
                )
            }

            // GENRE SELECTION DIALOG
            if (uiState.showGenreSelector) {
                val activity = ActivityRepository.findActivityByName(uiState.selectedActivity)
                val genreOptions = activity?.compatibleGenres ?: emptyList()

                GenreSelectionDialog(
                    options = genreOptions,
                    onDismiss = { viewModel.onGenreSelectorDismiss() },
                    currentSelection = uiState.selectedGenre,
                    onSelect = { genre -> viewModel.onGenreSelected(genre) }
                )
            }

            Column(
                verticalArrangement = Arrangement
                    .spacedBy(20.dp),
            ) {
                // HEADER
                Box(
                    contentAlignment = Alignment.TopStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.onPrimary)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Welcome back,",
                            fontWeight = FontWeight.Medium,
                            fontSize = 24.sp,
                            color = Color.White
                        )
                        Text(
                            text = viewModel.username ?: "User",
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Quickstart your customized workout here by selecting an activity and genre.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    // ACTIVITY BUTTON
                    Button(
                        modifier = Modifier
                            .weight(1f),
                        onClick = { viewModel.onActivityButtonClick() },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(20.dp),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Activity",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = uiState.selectedActivity,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Black
                            )
                        }
                    }

                    // GENRE BUTTON
                    Button(
                        modifier = Modifier
                            .weight(1f),
                        onClick = { viewModel.onGenreButtonClick() },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(20.dp),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Genre",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary

                            )
                            Text(
                                text = uiState.selectedGenre,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Black
                            )
                        }
                    }
                }

                // START BUTTON
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigateToWorkout() }
                ) {
                    Surface(
                        modifier = Modifier
                            .height(160.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondary
                    ){}
                    // rotating record
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
                            .padding(horizontal =20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier
                                .padding(vertical = 25.dp),
                        ){
                            Text(
                                text = "Start\nactivity",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start Icon",
                                tint = Color.Black, // Adjust color as needed
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

                // LAST WORKOUT
                Column(){
                    Text(
                        text = "Last workout",
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp,
                        color = Color.Gray
                    )
                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                    LogCard(
                        workoutSession = placeholderData,
                        cardColor = MaterialTheme.colorScheme.primary,
                        dateColor = Color.Black,
                        iconColor = MaterialTheme.colorScheme.onPrimary,
                        labelColor = Color.Black
                    )
                }
            }
        }

    }
}

// STATS
@Composable
fun Stat(icon: ImageVector, value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(30.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black
        )

    }
}

// Hi
