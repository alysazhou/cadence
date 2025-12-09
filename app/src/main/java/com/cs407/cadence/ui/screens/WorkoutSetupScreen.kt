package com.cs407.cadence.ui.screens

import android.app.Application
import android.util.Log
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.cadence.data.Activity
import com.cs407.cadence.data.ActivityRepository
import com.cs407.cadence.data.SpotifyAuthState
import com.cs407.cadence.data.network.SpotifyAuthManager
import com.cs407.cadence.data.network.SpotifyService
import com.cs407.cadence.data.network.SpotifyWebApiClient
import com.cs407.cadence.ui.components.SpotifyAuthDialog
import com.cs407.cadence.ui.viewModels.HomeScreenViewModel
import com.cs407.cadence.ui.viewModels.HomeScreenViewModelFactory
import com.cs407.cadence.ui.viewModels.WorkoutViewModel
import kotlinx.coroutines.launch

@Composable
fun WorkoutSetupScreen(
        workoutViewModel: WorkoutViewModel,
        onNavigateToWorkout: (String, String) -> Unit,
        onNavigateBack: () -> Unit,
) {
    val homeScreenViewModel: HomeScreenViewModel =
            viewModel(
                    factory =
                            HomeScreenViewModelFactory(
                                    LocalContext.current.applicationContext as Application
                            )
            )
    val uiState by homeScreenViewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val coroutineScope = rememberCoroutineScope()
    var showSpotifyAuthDialog by remember { mutableStateOf(false) }
    var authDialogShowing by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Monitor lifecycle and dismiss dialog when user returns authenticated
    DisposableEffect(lifecycleOwner, authDialogShowing) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && authDialogShowing) {
                if (SpotifyAuthState.isAuthenticated(context)) {
                    showSpotifyAuthDialog = false
                    authDialogShowing = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // DIALOGS

    if (showSpotifyAuthDialog) {
        SpotifyAuthDialog(
                onConnect = {
                    authDialogShowing = true
                    activity?.let { act ->
                        val spotifyAuthManager = SpotifyAuthManager(context)
                        spotifyAuthManager.startAuth(act)
                    }
                },
                onDismiss = { 
                    showSpotifyAuthDialog = false
                    authDialogShowing = false
                }
        )
    }

    if (uiState.showActivitySelector) {
        ActivitySelectionDialog(
                activities = ActivityRepository.getAllActivities(),
                currentSelection = uiState.selectedActivity,
                onDismiss = { homeScreenViewModel.onActivitySelectorDismiss() },
                onSelect = { activityName -> homeScreenViewModel.onActivitySelected(activityName) }
        )
    }

    if (uiState.showGenreSelector) {
        val activity = ActivityRepository.findActivityByName(uiState.selectedActivity)
        val genreOptions = activity?.compatibleGenres ?: emptyList()

        GenreSelectionDialog(
                options = genreOptions,
                onDismiss = { homeScreenViewModel.onGenreSelectorDismiss() },
                currentSelection = uiState.selectedGenre,
                onSelect = { genre -> homeScreenViewModel.onGenreSelected(genre) }
        )
    }

    Scaffold(
            topBar = {
                Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 10.dp)
                ) {
                    IconButton(
                            onClick = { onNavigateBack() },
                            modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate back to Home Screen",
                                tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(
                            text = "Customize Workout",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
    ) { innerPadding ->
        Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            //            Column {
            //                Text(
            //                    "Customize your activity",
            //                    style = MaterialTheme.typography.titleMedium,
            //                    color = MaterialTheme.colorScheme.onPrimary
            //                )
            //            }

            // ACTIVITY & GENRE BUTTONS
            Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // ACTIVITY BUTTON
                Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { homeScreenViewModel.onActivityButtonClick() },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                )
                ) {
                    Box(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 20.dp),
                            contentAlignment = Alignment.Center
                    ) {
                        Icon(
                                imageVector = Icons.Default.DirectionsRun,
                                contentDescription = null,
                                modifier =
                                        Modifier.size(60.dp)
                                                .align(Alignment.CenterEnd)
                                                .offset(x = -30.dp)
                                                .alpha(0.15f),
                                tint = MaterialTheme.colorScheme.onSecondary
                        )

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.Start,
                            ) {
                                Text(
                                        text = "Activity",
                                        style = MaterialTheme.typography.labelMedium,
                                        color =
                                                MaterialTheme.colorScheme.onSecondary.copy(
                                                        alpha = 0.4f
                                                ),
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
                                    modifier =
                                            Modifier.size(30.dp).align(Alignment.CenterVertically),
                                    tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }

                // GENRE BUTTON
                Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { homeScreenViewModel.onGenreButtonClick() },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                )
                ) {
                    Box(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            contentAlignment = Alignment.Center
                    ) {
                        Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                modifier =
                                        Modifier.size(60.dp)
                                                .align(Alignment.CenterEnd)
                                                .offset(x = -30.dp)
                                                .alpha(0.15f),
                                tint = MaterialTheme.colorScheme.onSecondary
                        )
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                        text = "Genre",
                                        style = MaterialTheme.typography.labelMedium,
                                        color =
                                                MaterialTheme.colorScheme.onSecondary.copy(
                                                        alpha = 0.4f
                                                )
                                )
                                Text(
                                        text = uiState.selectedGenre,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                            Icon(
                                    imageVector = Icons.Default.Autorenew,
                                    contentDescription = "Change Genre",
                                    modifier =
                                            Modifier.size(30.dp).align(Alignment.CenterVertically),
                                    tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
            }

            // START WORKOUT BUTTON
            Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        // Check if Spotify token is valid
                        coroutineScope.launch {
                            val tokenValid =
                                    SpotifyAuthState.isTokenValid(context) &&
                                            SpotifyWebApiClient.validateToken(context)

                            if (!tokenValid) {
                                Log.d(
                                        "WorkoutSetup",
                                        "Token invalid or expired, showing auth dialog"
                                )
                                showSpotifyAuthDialog = true
                            } else {
                                workoutViewModel.startWorkout()

                                try {
                                    val clientId = com.cs407.cadence.BuildConfig.SPOTIFY_CLIENT_ID
                                    val clientSecret =
                                            com.cs407.cadence.BuildConfig.SPOTIFY_CLIENT_SECRET
                                    val redirectUri = "com.cs407.cadence.auth://callback"
                                    SpotifyService.buildConnectionParams(
                                            clientId,
                                            redirectUri,
                                            clientSecret
                                    )

                                    SpotifyService.connect(
                                            context = context,
                                            onSuccess = {
                                                Log.d(
                                                        "WorkoutSetup",
                                                        "Spotify connected successfully"
                                                )
                                            },
                                            onFailure = { throwable ->
                                                Log.e(
                                                        "WorkoutSetup",
                                                        "Failed to connect to Spotify: ${throwable.message}"
                                                )
                                            }
                                    )
                                } catch (e: Exception) {
                                    Log.e("WorkoutSetup", "Error setting up Spotify: ${e.message}")
                                }

                                // Navigate regardless of Spotify connection status
                                onNavigateToWorkout(uiState.selectedGenre, uiState.selectedActivity)
                            }
                        }
                    },
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Box(
                        modifier =
                                Modifier.clip(RoundedCornerShape(100.dp))
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp)
        ) {
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                            Modifier.clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.onSecondary)
                                    .padding(10.dp)
            ) {
                Text(
                        text = "Select activity",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(10.dp).padding(bottom = 5.dp)
                )

                LazyColumn {
                    items(activities) { activity ->
                        val isSelected = activity.name == tempSelection

                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier =
                                        Modifier.clip(RoundedCornerShape(8.dp))
                                                .background(
                                                        if (isSelected)
                                                                MaterialTheme.colorScheme.surface
                                                        else Color.Transparent
                                                )
                                                .fillMaxWidth()
                                                .clickable { tempSelection = activity.name }
                                                .padding(20.dp)
                        ) {
                            Column(
                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                    modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                        text = activity.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                        text = activity.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color =
                                                MaterialTheme.colorScheme.onPrimary.copy(
                                                        alpha = 0.3f
                                                )
                                )
                            }
                            if (isSelected) {
                                Icon(
                                        imageVector = Icons.Default.DirectionsRun,
                                        contentDescription = "Selected Activity",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                ),
                        onClick = {
                            onSelect(tempSelection)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                            text = "Done",
                            style = MaterialTheme.typography.titleSmall,
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp)
        ) {
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                            Modifier.clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.onSecondary)
                                    .padding(10.dp)
            ) {
                Text(
                        text = "Select genre",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(10.dp).padding(bottom = 5.dp)
                )

                LazyColumn {
                    items(options) { option ->
                        val isSelected = option == tempSelection

                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier =
                                        Modifier.clip(RoundedCornerShape(8.dp))
                                                .background(
                                                        if (isSelected)
                                                                MaterialTheme.colorScheme.surface
                                                        else Color.Transparent
                                                )
                                                .fillMaxWidth()
                                                .clickable { tempSelection = option }
                                                .padding(horizontal = 20.dp, vertical = 20.dp)
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
                                        contentDescription = "Selected Genre",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                ),
                        onClick = {
                            onSelect(tempSelection)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                            text = "Done",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
    }
}
