//package com.cs407.cadence.ui.screens
//
//import android.app.Activity
//import android.content.pm.PackageManager
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedButton
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
////import com.spotify.sdk.android.auth.AuthorizationClient
////import com.spotify.sdk.android.auth.AuthorizationRequest
////import com.spotify.sdk.android.auth.AuthorizationResponse
//
//@Composable
//fun MusicAuthScreen(
//    onAuthComplete: () -> Unit,
//    onSkip: () -> Unit
//) {
//    val context = LocalContext.current
//
//    val appInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
//    val metadata = appInfo.metaData
//    val clientId = metadata.getString("com.cs407.cadence.SPOTIFY_CLIENT_ID") ?: ""
//
//    val scheme = metadata.getString("com.cs407.cadence.auth") ?: "com.cs407.cadence.auth"
//    val host = metadata.getString("callback") ?: "callback"
//    val redirectUri = "$scheme://$host"
//
//    // handle result from spotify
//    val spotifyAuthLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        val response = AuthorizationClient.getResponse(result.resultCode, result.data)
//        when (response.type) {
//            AuthorizationResponse.Type.CODE -> {
//                println("auth code: ${response.code}")
//                onAuthComplete()
//            }
//            AuthorizationResponse.Type.ERROR -> {
//                println("auth error: ${response.error}")
//                onSkip()
//            }
//            else -> {
//                println("spotify auth error")
//                onSkip()
//            }
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                Brush.verticalGradient(
//                    colors = listOf(
//                        MaterialTheme.colorScheme.primary,
//                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
//                    )
//                )
//            )
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(32.dp),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                text = "Connect your music",
//                style = MaterialTheme.typography.titleMedium,
//                color = MaterialTheme.colorScheme.onPrimary,
//                textAlign = TextAlign.Center
//            )
//            Spacer(modifier = Modifier.height(16.dp))
//            Text(
//                text = "To use Cadence, you'll need to connect a music player. If you choose to skip this for now, you can later connect a music player through Settings.",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
//                textAlign = TextAlign.Center
//            )
//
//            Spacer(modifier = Modifier.height(40.dp))
//
//            Column(
//                verticalArrangement = Arrangement.spacedBy(20.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                // CONNECT WITH SPOTIFY
//                Button(
//                    onClick = {
//                        val request = AuthorizationRequest.Builder(
//                            clientId, AuthorizationResponse.Type.CODE, redirectUri // <-- Use the newly constructed URI
//                        ).setScopes(arrayOf("user-read-private", "user-top-read")).build()
//
//                        val intent = AuthorizationClient.createLoginActivityIntent(context as Activity, request)
//                        spotifyAuthLauncher.launch(intent)
//                    },
//                    shape = RoundedCornerShape(100.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(0xFF1DB954),
//                        contentColor = Color.White
//                    ),
//                    contentPadding = PaddingValues(vertical = 15.dp, horizontal = 30.dp)
//                ) {
//                    Text(
//                        "Spotify",
//                        style = MaterialTheme.typography.titleMedium
//                    )
//                }
//
//                // SKIP
//                OutlinedButton(
//                    shape = RoundedCornerShape(100.dp),
//                    onClick = onSkip,
//                    contentPadding = PaddingValues(vertical = 15.dp, horizontal = 30.dp),
//                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
//                    border = BorderStroke(3.dp, MaterialTheme.colorScheme.secondary)
//                ) {
//                    Text(
//                        text = "Skip",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = MaterialTheme.colorScheme.secondary
//                    )
//                }
//            }
//        }
//    }
//}
