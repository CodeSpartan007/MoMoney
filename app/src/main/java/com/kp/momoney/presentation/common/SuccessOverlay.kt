package com.kp.momoney.presentation.common

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.kp.momoney.R
import kotlinx.coroutines.delay

/**
 * Fullscreen success overlay with Lottie animation. Shows a success animation and automatically
 * calls onAnimationFinished after 2.5 seconds. Blocks back button navigation while animation is
 * playing.
 *
 * @param onAnimationFinished Callback invoked after animation completes (2.5 seconds)
 */
@Composable
fun SuccessOverlay(onAnimationFinished: () -> Unit) {
    // Prevent back button navigation during animation
    BackHandler(enabled = true) {
        // Do nothing - block back navigation
    }

    // Load the Lottie composition
    val composition by
            rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.success_anim))

    // Animate the composition with 1 iteration (play once)
    val progress by animateLottieCompositionAsState(composition = composition, iterations = 1)

    // Fullscreen Box with theme background
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Lottie Animation
        if (composition != null) {
            LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.fillMaxSize()
            )
        }

        // Success Text (centered below animation)

    }

    // Timing Logic: Wait 2.5 seconds then call callback
    LaunchedEffect(Unit) {
        delay(2500)
        onAnimationFinished()
    }
}
