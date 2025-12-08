package com.cs407.cadence.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cs407.cadence.data.models.PlayedSong

@Composable
fun WorkoutResultsScreen(
        modifier: Modifier = Modifier,
        time: Int,
        distance: Double,
        calories: Int,
        averageBpm: Int,
        playedSongs: List<PlayedSong>,
        onDone: () -> Unit
) {
    Scaffold(
            containerColor = MaterialTheme.colorScheme.primary,
            topBar = {
                Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        contentAlignment = Alignment.Center
                ) {
                    Text(
                            text = "Workout Summary",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
    ) { innerPadding ->
        Column(
                modifier = modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    WorkoutSummaryCard(
                            time = time,
                            distance = distance,
                            calories = calories,
                            averageBpm = averageBpm
                    )
                }

                items(playedSongs) { song -> PlayedSongCard(song = song) }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                    shape = RoundedCornerShape(100.dp),
                    onClick = onDone,
                    contentPadding = PaddingValues(vertical = 15.dp, horizontal = 40.dp),
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                            ),
                    modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                        text = "Done",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun WorkoutSummaryCard(time: Int, distance: Double, calories: Int, averageBpm: Int) {
    Box(
            modifier =
                    Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(20.dp)
    ) {
        Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                        icon = Icons.Default.Timer,
                        label = "Time",
                        value = "$time min",
                        modifier = Modifier.weight(1f)
                )
                StatItem(
                        icon = Icons.Default.Place,
                        label = "Distance",
                        value = "${String.format("%.2f", distance)} mi",
                        modifier = Modifier.weight(1f)
                )
            }

            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                        icon = Icons.Default.LocalFireDepartment,
                        label = "Calories",
                        value = "$calories cal",
                        modifier = Modifier.weight(1f)
                )
                StatItem(
                        icon = Icons.Default.Favorite,
                        label = "Avg BPM",
                        value = "$averageBpm",
                        modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatItem(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        label: String,
        value: String,
        modifier: Modifier = Modifier
) {
    Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
    ) {
        Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
            )
            Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun PlayedSongCard(song: PlayedSong) {
    Box(
            modifier =
                    Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.1f))
                            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
            if (song.albumArtUrl != null && song.albumArtUrl.isNotEmpty()) {
                AsyncImage(
                        model = song.albumArtUrl,
                        contentDescription = "Album art",
                        modifier = Modifier.size(50.dp).clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                )
            } else {
                Box(
                        modifier =
                                Modifier.size(50.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                ) {
                    Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                            modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(15.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = song.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1
                )
                if (song.artist.isNotEmpty()) {
                    Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                            maxLines = 1
                    )
                }
            }
        }
    }
}
