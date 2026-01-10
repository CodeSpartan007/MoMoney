package com.kp.momoney.presentation.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kp.momoney.presentation.common.AppLoadingAnimation
import kotlinx.coroutines.delay

/**
 * Splash Screen with a fixed 5-second delay.
 * Displays the app loading animation and app name.
 * 
 * @param onSplashFinished Callback invoked after the 5-second delay
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Trigger the delay logic when the composable is first composed
    LaunchedEffect(Unit) {
        delay(5000) // 5 seconds delay
        onSplashFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display the Lottie loading animation (App Icon)
            AppLoadingAnimation()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Display the app name with custom layout
            Column {
                // Display "MoMoney" large
                Text(
                    text = "MoMoney",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Display "MoBetter" smaller, aligned to start under "ey" of "MoMoney"
                Text(
                    text = "MoBetter",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 120.dp)
                )
            }
        }
    }
}

