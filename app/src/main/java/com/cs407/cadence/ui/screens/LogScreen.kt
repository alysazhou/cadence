package com.cs407.cadence.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.cadence.ui.components.LogCard
import com.cs407.cadence.data.models.WorkoutSession
import com.cs407.cadence.ui.theme.CadenceTheme

@Composable
fun LogScreen(
    modifier: Modifier = Modifier
) {

    val placeholderData = WorkoutSession(
        id = 1,
        date = "00/00/0000",
        bpm = 180,
        distance = 3.1,
        time = 30,
        calories = 100
    )

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Log history",
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
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
            LazyColumn(
                verticalArrangement = Arrangement
                    .spacedBy(20.dp),
            ) {
                item() {
                    Text(
                        text = "Most recent activity",
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp,
                        color = Color.Gray
                    )
                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                    LogCard(
                        workoutSession = placeholderData,
                        cardColor = MaterialTheme.colorScheme.onPrimary,
                        dateColor = Color.White,
                        iconColor = Color.White,
                        labelColor = Color.White,
                    )
                }

                item() {
                    Text(
                        text = "Previous workouts",
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp,
                        color = Color.Gray
                    )
                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                    LogCard(workoutSession = placeholderData)
                }

                items(9) { index ->
                    LogCard(workoutSession = placeholderData)
                }

                item() {}
            }
        }
    }
}

@Preview
@Composable
fun LogScreenPreview() {
    CadenceTheme() {
        LogScreen()
    }
}