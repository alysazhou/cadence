package com.cs407.cadence.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cs407.cadence.R
import com.cs407.cadence.ui.viewModels.UserViewModel

@Composable
fun LoginScreen(viewModel: UserViewModel, onSpotifyLogin: (() -> Unit)? = null) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val emailError by viewModel.emailError.collectAsStateWithLifecycle()
    val passwordError by viewModel.passwordError.collectAsStateWithLifecycle()

    Box(
            modifier =
                    Modifier.fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary)
                            .background(
                                    Brush.verticalGradient(
                                            colors =
                                                    listOf(
                                                            MaterialTheme.colorScheme.primary,
                                                            MaterialTheme.colorScheme.secondary
                                                                    .copy(alpha = 0.3f)
                                                    )
                                    )
                            )
    ) {
        Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // LOGO
            val logoImage =
                    if (isSystemInDarkTheme()) {
                        R.drawable.cadence_logo_turquoise
                    } else {
                        R.drawable.cadence_logo_blue
                    }

            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                        painter = painterResource(logoImage),
                        contentDescription = "Cadence logo",
                        modifier = Modifier.width(150.dp)
                )
                Text(
                        text = "CADENCE",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
            Spacer(modifier = Modifier.height(20.dp))

            // EMAIL INPUT
            OutlinedTextField(
                    textStyle = MaterialTheme.typography.bodyMedium,
                    value = email,
                    onValueChange = { email = it },
                    placeholder = {
                        Text(text = "E-mail address", style = MaterialTheme.typography.bodyMedium)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors =
                            OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                    unfocusedLabelColor = Color.Gray,
                                    focusedLabelColor = Color.Black,
                                    unfocusedPlaceholderColor =
                                            MaterialTheme.colorScheme.onPrimary.copy(0.4f),
                                    focusedPlaceholderColor =
                                            MaterialTheme.colorScheme.onPrimary.copy(0.4f)
                            ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // PASSWORD INPUT
            OutlinedTextField(
                    visualTransformation = PasswordVisualTransformation(),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    value = password,
                    onValueChange = { password = it },
                    placeholder = {
                        Text(text = "Password", style = MaterialTheme.typography.bodyMedium)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors =
                            OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                    unfocusedLabelColor = Color.Gray,
                                    focusedLabelColor = Color.Black,
                                    unfocusedPlaceholderColor =
                                            MaterialTheme.colorScheme.onPrimary.copy(0.4f),
                                    focusedPlaceholderColor =
                                            MaterialTheme.colorScheme.onPrimary.copy(0.4f)
                            ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
            )
            Spacer(modifier = Modifier.height(10.dp))

            emailError?.let {
                Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                )
            }
            passwordError?.let {
                Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // SIGN IN BUTTON
                OutlinedButton(
                        shape = RoundedCornerShape(100.dp),
                        onClick = { viewModel.signIn(email, password) },
                        contentPadding = PaddingValues(vertical = 15.dp, horizontal = 30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = BorderStroke(3.dp, MaterialTheme.colorScheme.secondary)
                ) {
                    Text(
                            text = "Sign in",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                    )
                }

                // REGISTER BUTTON
                Button(
                        contentPadding = PaddingValues(vertical = 15.dp, horizontal = 30.dp),
                        onClick = { viewModel.register(email, password) },
                        shape = RoundedCornerShape(100.dp),
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                ),
                ) {
                    Text(
                            text = "Register",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
    }
}
