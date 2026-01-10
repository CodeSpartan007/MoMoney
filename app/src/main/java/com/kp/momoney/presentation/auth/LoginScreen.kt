package com.kp.momoney.presentation.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kp.momoney.R
import com.kp.momoney.presentation.common.LoadingOverlay

@Composable
fun LoginScreen(
        onLoginSuccess: () -> Unit,
        onNavigateToRegister: () -> Unit,
        viewModel: AuthViewModel = hiltViewModel()
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val isPasswordVisible by viewModel.isPasswordVisible.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val resetPasswordState by viewModel.resetPasswordState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showResetPasswordDialog by remember { mutableStateOf(false) }
    var resetPasswordEmail by remember { mutableStateOf("") }

    // Google Sign-In launcher
    val googleSignInLauncher =
            rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
            ) { result -> result.data?.let { intent -> viewModel.onGoogleSignInResult(intent) } }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        } else if (authState is AuthState.Error) {
            snackbarHostState.showSnackbar((authState as AuthState.Error).message)
        }
    }

    LaunchedEffect(resetPasswordState) {
        val state = resetPasswordState
        when {
            state is ResetPasswordState.Success -> {
                Toast.makeText(context, "Reset link sent to your email", Toast.LENGTH_SHORT).show()
                showResetPasswordDialog = false
                resetPasswordEmail = ""
                viewModel.resetPasswordState()
            }
            state is ResetPasswordState.Error -> {
                val errorState = state as ResetPasswordState.Error
                Toast.makeText(context, errorState.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                        painter = painterResource(id = R.drawable.ic_logo_mini),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(80.dp).padding(bottom = 16.dp)
                )
                Text(text = "Welcome back", style = MaterialTheme.typography.headlineMedium)
                Text(
                        text = "Sign in to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                        value = email,
                        onValueChange = viewModel::onEmailChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email") },
                        singleLine = true,
                        isError = emailError != null,
                        supportingText = {
                            if (emailError != null) {
                                Text(text = emailError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                        value = password,
                        onValueChange = viewModel::onPasswordChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password") },
                        singleLine = true,
                        isError = passwordError != null,
                        supportingText = {
                            if (passwordError != null) {
                                Text(
                                        text = passwordError!!,
                                        color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        visualTransformation =
                                if (isPasswordVisible) VisualTransformation.None
                                else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = viewModel::togglePasswordVisibility) {
                                val icon =
                                        if (isPasswordVisible) Icons.Default.VisibilityOff
                                        else Icons.Default.Visibility
                                val description =
                                        if (isPasswordVisible) "Hide password" else "Show password"
                                Icon(imageVector = icon, contentDescription = description)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                // Sign Up Text
                Text(
                        text = "Don't have an account? Sign Up",
                        modifier =
                                Modifier.clickable(onClick = onNavigateToRegister)
                                        .padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                        onClick = viewModel::login,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(
                                modifier = Modifier.height(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                        )
                    } else {
                        Text("Login")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Divider
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                            text = "OR",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google Sign-In Button
                OutlinedButton(
                        onClick = {
                            val signInIntent = viewModel.getGoogleSignInIntent()
                            googleSignInLauncher.launch(signInIntent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = authState !is AuthState.Loading
                ) {
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                    ) {
                        // Google Icon - using a styled "G" text as placeholder
                        // In production, you'd use the actual Google logo vector drawable
                        Text(
                                text = "G",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Sign in with Google")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Forgot Password Button
                TextButton(
                        onClick = { showResetPasswordDialog = true },
                        modifier = Modifier.fillMaxWidth()
                ) { Text(text = "Forgot Password?", style = MaterialTheme.typography.bodySmall) }
            }

            // Loading Overlay
            LoadingOverlay(isLoading = authState is AuthState.Loading)
        }

        // Reset Password Dialog
        if (showResetPasswordDialog) {
            val focusRequester = remember { FocusRequester() }

            LaunchedEffect(Unit) { focusRequester.requestFocus() }

            AlertDialog(
                    onDismissRequest = {
                        showResetPasswordDialog = false
                        resetPasswordEmail = ""
                    },
                    title = { Text("Reset Password") },
                    text = {
                        OutlinedTextField(
                                value = resetPasswordEmail,
                                onValueChange = { resetPasswordEmail = it },
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                                singleLine = true,
                                keyboardOptions =
                                        KeyboardOptions(keyboardType = KeyboardType.Email),
                                enabled = resetPasswordState !is ResetPasswordState.Loading
                        )
                    },
                    confirmButton = {
                        Button(
                                onClick = { viewModel.resetPassword(resetPasswordEmail) },
                                enabled = resetPasswordState !is ResetPasswordState.Loading
                        ) {
                            if (resetPasswordState is ResetPasswordState.Loading) {
                                CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                )
                            } else {
                                Text("Send Email")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                                onClick = {
                                    showResetPasswordDialog = false
                                    resetPasswordEmail = ""
                                },
                                enabled = resetPasswordState !is ResetPasswordState.Loading
                        ) { Text("Cancel") }
                    }
            )
        }
    }
}
