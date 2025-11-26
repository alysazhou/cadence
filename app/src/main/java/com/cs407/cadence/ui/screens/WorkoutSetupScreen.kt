package com.cs407.cadence.ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.cadence.data.Activity
import com.cs407.cadence.data.ActivityRepository
import com.cs407.cadence.ui.viewModels.HomeScreenViewModel
import com.cs407.cadence.ui.viewModels.HomeScreenViewModelFactory
import com.cs407.cadence.ui.viewModels.WorkoutViewModel

@Composable
fun WorkoutSetupScreen(
    workoutViewModel: WorkoutViewModel,
    viewModel: HomeScreenViewModel = viewModel(
        factory = HomeScreenViewModelFactory(LocalContext.current.applicationContext as Application)
    ),
    onNavigateToWorkout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()


    if (uiState.showActivitySelector) {
        ActivitySelectionDialog(
            activities = ActivityRepository.getAllActivities(),
            currentSelection = uiState.selectedActivity,
            onDismiss = { viewModel.onActivitySelectorDismiss() },
            onSelect = { activityName -> viewModel.onActivitySelected(activityName) }
        )
    }


    if (uiState.showGenreSelector) {
        val activity = ActivityRepository.findActivityByName(uiState.selectedActivity)
        GenreSelectionDialog(
            options = activity?.compatibleGenres ?: emptyList(),
            currentSelection = uiState.selectedGenre,
            onDismiss = { viewModel.onGenreSelectorDismiss() },
            onSelect = { genre -> viewModel.onGenreSelected(genre) }
        )
    }

    Scaffold(
        topBar = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Text(
                    style = MaterialTheme.typography.displayLarge,
                    text = "WORKOUT",
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {


            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.onActivityButtonClick() },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.CenterEnd)
                            .offset(x = -30.dp)
                            .alpha(0.15f),
                        tint = MaterialTheme.colorScheme.onSecondary
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "Activity",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.4f)
                            )
                            Text(
                                text = uiState.selectedActivity,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = "Change Activity",
                            modifier = Modifier.align(Alignment.CenterVertically),
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }


            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.onGenreButtonClick() },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.CenterEnd)
                            .offset(x = -30.dp)
                            .alpha(0.15f),
                        tint = MaterialTheme.colorScheme.onSecondary
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "Genre",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.4f)
                            )
                            Text(
                                text = uiState.selectedGenre,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = "Change Genre",
                            modifier = Modifier.align(Alignment.CenterVertically),
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }


            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    workoutViewModel.startWorkout()
                    onNavigateToWorkout()
                },
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(MaterialTheme.colorScheme.tertiary)
                        .padding(vertical = 15.dp, horizontal = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Start workout",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
    }
}



@Composable
fun ActivitySelectionDialog(
    activities: List<Activity>,
    currentSelection: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var tempSelection by remember { mutableStateOf(currentSelection) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.onSecondary)
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select activity",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(10.dp)
                )

                LazyColumn {
                    items(activities) { activity ->
                        val isSelected = activity.name == tempSelection

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
                                )
                                .fillMaxWidth()
                                .clickable { tempSelection = activity.name }
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = activity.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    text = activity.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsRun,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onSelect(tempSelection)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        text = "Done",
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
    }
}



@Composable
fun GenreSelectionDialog(
    options: List<String>,
    currentSelection: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var tempSelection by remember { mutableStateOf(currentSelection) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.onSecondary)
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select genre",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(10.dp)
                )

                LazyColumn {
                    items(options) { option ->
                        val isSelected = option == tempSelection

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
                                )
                                .fillMaxWidth()
                                .clickable { tempSelection = option }
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.weight(1f)
                            )

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onSelect(tempSelection)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        text = "Done",
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
    }
}
