package com.cs407.cadence.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.cadence.ui.data.WorkoutData

@Composable
fun LogCard(
    modifier: Modifier = Modifier,
    workoutData: WorkoutData,
    cardColor: Color = MaterialTheme.colorScheme.primary,
    dateColor: Color = Color.Black,
    iconColor: Color = MaterialTheme.colorScheme.onPrimary,
    labelColor: Color = Color.Black,
    hasLabel: Boolean = true,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(cardColor)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (hasLabel) {
                Text(
                    text = workoutData.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = dateColor

                )
            }

            // row of stats
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // BPM
                Stat(
                    icon = Icons.Default.Favorite,
                    value = workoutData.bpm.toString(),
                    label = "bpm",
                    iconColor = iconColor,
                    labelColor = labelColor
                )

                // DISTANCE
                Stat(
                    icon = Icons.Default.Place,
                    value = workoutData.distance.toString(),
                    label = "mi",
                    iconColor = iconColor,
                    labelColor = labelColor
                )
                Stat(
                    icon = Icons.Default.Timer,
                    value = workoutData.time.toString(),
                    label = "min",
                    iconColor = iconColor,
                    labelColor = labelColor
                )
                Stat(
                    icon = Icons.Default.LocalFireDepartment,
                    value = workoutData.calories.toString(),
                    label = "cal",
                    iconColor = iconColor,
                    labelColor = labelColor
                )
            }
        }
    }
}

@Composable
private fun Stat(
    icon: ImageVector,
    value: String,
    label: String,
    iconColor: Color,
    labelColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(30.dp)
        )
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = labelColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor
        )
    }
}