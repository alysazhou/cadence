package com.cs407.cadence.ui.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cs407.cadence.data.network.SpotifyAuthManager

@Composable
fun SpotifyAuthDialog(onConnect: () -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Column(
                    modifier = Modifier.padding(30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                        text = "Connect music player",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                        text =
                                "To use Cadence, you'll need to connect a third-party music player account.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // CONNECT TO SPOTIFY
                    Button(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                            onClick = {
                                try {
                                    if (!SpotifyAuthManager.isConfigured()) {
                                        Toast.makeText(
                                                        context,
                                                        "Spotify is not configured. Please add SPOTIFY_CLIENT_ID to local.properties",
                                                        Toast.LENGTH_LONG
                                                )
                                                .show()
                                        Log.e(
                                                "SpotifyAuthDialog",
                                                "Spotify Client ID missing from BuildConfig"
                                        )
                                        onDismiss()
                                        return@Button
                                    }
                                    onConnect()
                                } catch (e: Exception) {
                                    Log.e("SpotifyAuthDialog", "Error starting Spotify auth", e)
                                    Toast.makeText(
                                                    context,
                                                    "Error: ${e.message}",
                                                    Toast.LENGTH_LONG
                                            )
                                            .show()
                                    onDismiss()
                                }
                            },
                            shape = RoundedCornerShape(100.dp),
                            colors =
                                    ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF1DB954), // Spotify green
                                            contentColor = Color.White
                                    )
                    ) { Text(text = "Spotify", style = MaterialTheme.typography.titleSmall) }

                    // CANCEL
                    OutlinedButton(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                            onClick = onDismiss,
                            shape = RoundedCornerShape(100.dp),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(
                                style = MaterialTheme.typography.titleSmall,
                                text = "Cancel",
                                color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}
