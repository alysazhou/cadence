package com.cs407.cadence.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable
fun StatsRow(
    modifier: Modifier = Modifier,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    labelColor: Color = Color.Black
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 2. Pass the colors down to each Stat composable
        Stat(
            icon = Icons.Default.Favorite,
            value = "180",
            label = " bpm",
            iconColor = iconColor,
            labelColor = labelColor
        )
        Stat(
            icon = Icons.Default.Place,
            value = "3.1",
            label = " mi",
            iconColor = iconColor,
            labelColor = labelColor
        )
        Stat(
            icon = Icons.Default.Timer,
            value = "28",
            label = " min",
            iconColor = iconColor,
            labelColor = labelColor
        )
        Stat(
            icon = Icons.Default.LocalFireDepartment,
            value = "350",
            label = " cal",
            iconColor = iconColor,
            labelColor = labelColor
        )
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

