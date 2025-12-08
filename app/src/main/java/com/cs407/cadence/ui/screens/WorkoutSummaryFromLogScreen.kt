package com.cs407.cadence.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cs407.cadence.data.models.PlayedSong

@Composable
fun WorkoutSummaryFromLogScreen(
        modifier: Modifier = Modifier,
        time: Int,
        distance: Double,
        calories: Int,
        averageBpm: Int,
        playedSongs: List<PlayedSong>,
        onBack: () -> Unit
) {
    Scaffold(
            containerColor = MaterialTheme.colorScheme.primary,
            topBar = {
                Box(
                        modifier =
                                Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 10.dp),
                        contentAlignment = Alignment.CenterStart
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate back",
                                tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(
                            text = "Workout Summary",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.align(Alignment.Center)
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
        }
    }
}
