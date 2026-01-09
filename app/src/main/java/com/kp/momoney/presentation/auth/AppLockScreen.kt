package com.kp.momoney.presentation.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.biometric.BiometricManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import android.app.Activity
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.kp.momoney.util.showBiometricPrompt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kp.momoney.R
import kotlinx.coroutines.launch

@Composable
fun AppLockScreen(
    onUnlockSuccess: () -> Unit,
    isSetupMode: Boolean = false,
    viewModel: AppLockViewModel = hiltViewModel()
) {
    val inputPin by viewModel.inputPin.collectAsState()
    val confirmPin by viewModel.confirmPin.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isConfirming by viewModel.isConfirming.collectAsState()
    val shouldShake by viewModel.shouldShake.collectAsState()
    val isSetup by viewModel.isSetupMode.collectAsState()

    // Initialize setup mode
    LaunchedEffect(isSetupMode) {
        viewModel.setSetupMode(isSetupMode)
    }

    // Handle success state
    LaunchedEffect(uiState) {
        if (uiState is AppLockUiState.Success) {
            onUnlockSuccess()
        }
    }

    // Shake animation
    val offsetX = remember { Animatable(0f) }
    LaunchedEffect(shouldShake) {
        if (shouldShake) {
            repeat(4) {
                launch {
                    offsetX.animateTo(10f, animationSpec = tween(50))
                    offsetX.animateTo(-10f, animationSpec = tween(50))
                }
            }
            offsetX.animateTo(0f, animationSpec = tween(50))
            viewModel.resetShake()
        }
    }

    val currentPin = if (isConfirming) confirmPin else inputPin
    val isUnlockMode = !isSetup
    val title = when {
        isSetup && !isConfirming -> "Create PIN"
        isSetup && isConfirming -> "Confirm PIN"
        else -> "Welcome Back"
    }
    val subtitle = when {
        isSetup && !isConfirming -> "Create a PIN to secure your account"
        isSetup && isConfirming -> "Confirm your PIN"
        else -> "Enter your PIN to access MoMoney"
    }
    
    // Get context and activity
    val context = LocalContext.current
    
    // Get Activity from context and check if it's FragmentActivity
    val activity = remember {
        context as? Activity
    }
    val fragmentActivity = remember(activity) {
        activity as? FragmentActivity
    }
    
    // Check if biometric is available AND we have FragmentActivity
    val biometricAvailable = remember {
        val biometricManager = BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or 
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS && fragmentActivity != null
    }
    
    // Auto-trigger biometric prompt in unlock mode
    LaunchedEffect(Unit) {
        if (isUnlockMode && biometricAvailable && fragmentActivity != null) {
            // Small delay to ensure UI is ready
            kotlinx.coroutines.delay(300)
            showBiometricPrompt(fragmentActivity) {
                onUnlockSuccess()
            }
        }
    }

    // Layout Container: Surface with 100% opaque background
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section (Top)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(64.dp))
                
                // App Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_mini),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(80.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Title: "Welcome Back" (H5 style)
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Subtitle: "Enter your PIN to access MoMoney" (Body style, gray color)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // PIN Dots (Middle)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(x = offsetX.value.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(6) { index ->
                        PinDot(
                            isFilled = index < currentPin.length,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Error message
                if (uiState is AppLockUiState.Error) {
                    Text(
                        text = (uiState as AppLockUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            // The Numpad (Bottom)
            Keypad(
                onNumberClick = viewModel::onNumberClick,
                onDeleteClick = viewModel::onDeleteClick,
                // Show biometric button if biometric is available, in unlock mode, and we have FragmentActivity
                onBiometricClick = if (isUnlockMode && biometricAvailable && fragmentActivity != null) {
                    { 
                        showBiometricPrompt(fragmentActivity) { 
                            onUnlockSuccess() 
                        }
                    }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PinDot(
    isFilled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .then(
                if (isFilled) {
                    Modifier.background(MaterialTheme.colorScheme.primary)
                } else {
                    Modifier
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = CircleShape
                        )
                }
            )
    )
}

@Composable
private fun Keypad(
    onNumberClick: (Int) -> Unit,
    onDeleteClick: () -> Unit,
    onBiometricClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: 1, 2, 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KeypadButton(
                text = "1",
                onClick = { onNumberClick(1) },
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "2",
                onClick = { onNumberClick(2) },
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "3",
                onClick = { onNumberClick(3) },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Row 2: 4, 5, 6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KeypadButton(
                text = "4",
                onClick = { onNumberClick(4) },
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "5",
                onClick = { onNumberClick(5) },
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "6",
                onClick = { onNumberClick(6) },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Row 3: 7, 8, 9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KeypadButton(
                text = "7",
                onClick = { onNumberClick(7) },
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "8",
                onClick = { onNumberClick(8) },
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "9",
                onClick = { onNumberClick(9) },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 4: Biometric Icon, 0, Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Biometric button (only shown if onBiometricClick is provided)
            if (onBiometricClick != null) {
                KeypadButton(
                    text = "",
                    onClick = onBiometricClick,
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Fingerprint
                )
            } else {
                // Empty space if biometric is not available
                Spacer(modifier = Modifier.weight(1f))
            }
            
            // 0 button
            KeypadButton(
                text = "0",
                onClick = { onNumberClick(0) },
                modifier = Modifier.weight(1f)
            )
            
            // Backspace button
            KeypadButton(
                text = "",
                onClick = onDeleteClick,
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Backspace
            )
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


