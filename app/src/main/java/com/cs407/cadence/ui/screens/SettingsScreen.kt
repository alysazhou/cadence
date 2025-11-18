package com.cs407.cadence.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
// Removed duplicate import
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cs407.cadence.ui.theme.CadenceTheme
import com.cs407.cadence.ui.viewModels.UserViewModel

@Composable
fun SettingsScreen(
    viewModel: UserViewModel,
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    onClearLog: () -> Unit,
) {
    var isEditingUsername by remember { mutableStateOf(false) }
    var tempName by remember(displayName) { mutableStateOf(displayName) }
    var autoSkipEnabled by remember { mutableStateOf(true) }
    var autoStopEnabled by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showSignOutDialog) {
        ConfirmationDialog(
            title = "Sign out",
            message = "You're about to be signed out of your account.",
            confirmButtonText = "Confirm",
            onConfirm = {
                viewModel.logout()
                showSignOutDialog = false
            },
            onDismiss = { showSignOutDialog = false }
        )
    }

    if (showDeleteDialog) {
        var password by remember { mutableStateOf("") }
        val reauthError by viewModel.reauthError.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.clearErrors()
        }

        DeleteAccountDialog(
            password = password,
            onPasswordChange = { password = it },
            error = reauthError,
            onConfirm = {
                viewModel.deleteAccount(password)
            },
            onDismiss = { showDeleteDialog = false }
        )
    }


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                Text(
                    style = MaterialTheme.typography.displayLarge,
                    text = "SETTINGS",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // CHANGE DISPLAY NAME
                Text(
                    "Display name",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(10.dp)
                        .padding(start = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val maxChar = 12
                    BasicTextField(
                        singleLine = true,
                        value = tempName,
                        onValueChange = {
                            if (it.length <= maxChar) {
                                tempName = it
                            }
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onPrimary
                        ),
                        enabled = isEditingUsername,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        enabled = if (isEditingUsername) tempName.isNotBlank() else true,
                        onClick = {
                            if (isEditingUsername) {
                                onDisplayNameChange(tempName)
                            }
                            isEditingUsername = !isEditingUsername
                        }
                    ) {
                        Icon(
                            imageVector = if (isEditingUsername) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditingUsername) "Save Username" else "Edit Username",
                            tint = if (isEditingUsername && tempName.isBlank()) MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Music",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                )

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                        .padding(start = 10.dp, end = 5.dp)
                ) {
                    // AUTO-SKIP
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Auto-skip on pace change",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            modifier = Modifier.scale(0.8f),
                            checked = autoSkipEnabled,
                            onCheckedChange = { autoSkipEnabled = it },
                            colors = SwitchDefaults.colors(
                                uncheckedBorderColor = Color.Transparent,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSecondary,
                                uncheckedTrackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                checkedThumbColor = MaterialTheme.colorScheme.onSecondary,
                                checkedTrackColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }

                    // AUTO-STOP
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Auto-stop on stop",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            modifier = Modifier.scale(0.8f),
                            checked = autoStopEnabled,
                            onCheckedChange = { autoStopEnabled = it },
                            colors = SwitchDefaults.colors(
                                uncheckedBorderColor = Color.Transparent,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSecondary,
                                uncheckedTrackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                checkedThumbColor = MaterialTheme.colorScheme.onSecondary,
                                checkedTrackColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                }
            }

            // LOG SETTINGS
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Log",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Clear log history",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            // ACCOUNT SETTINGS
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Account",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                )

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    // SIGN OUT
                    Row(
                        modifier = Modifier
                            .clickable { showSignOutDialog = true }
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Sign out",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }

                    // DELETE ACCOUNT
                    Row(
                        modifier = Modifier
                            .clickable { showDeleteDialog = true }
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Delete account",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

        }
    }
}

@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
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
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = message,
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
                            color = MaterialTheme.colorScheme.secondary)
                    }

                    // CONFIRM ACTION
                    Button(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        onClick = onConfirm,
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text(
                            text = confirmButtonText,
                            style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteAccountDialog(
    password: String,
    onPasswordChange: (String) -> Unit,
    error: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
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
                    text = "Delete account",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "All of your workout data will be deleted! To confirm deletion, please re-enter your password.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    textStyle = MaterialTheme.typography.bodyMedium,
                    placeholder = { Text("Password", style = MaterialTheme.typography.bodyMedium) },
                    shape = RoundedCornerShape(8.dp),
                    value = password,
                    onValueChange = onPasswordChange,
                    isError = error != null,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                    )
                )

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // CONFIRM DELETE BUTTON
                    Button(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        onClick = onConfirm,
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Delete", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSecondary)
                    }

                    // CANCEL BUTTON
                    OutlinedButton(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        onClick = onDismiss,
                        shape = RoundedCornerShape(100.dp),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Cancel", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}