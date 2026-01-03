package com.kp.momoney.presentation.common

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.kp.momoney.R

/**
 * Reusable Lottie loading animation component.
 * Falls back to CircularProgressIndicator if the Lottie file is not found.
 *
 * @param modifier Modifier to be applied to the animation
 * @param animRes Resource ID of the Lottie animation file (default: R.raw.loading_anim)
 */
@Composable
fun AppLoadingAnimation(
    modifier: Modifier = Modifier,
    animRes: Int = R.raw.loading_anim
) {
    // Try to load the Lottie composition
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(animRes)
    )
    
    // Animate the composition with infinite loop
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    
    // Display the animation, or fallback to CircularProgressIndicator if composition is null
    if (composition != null) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = modifier.size(200.dp)
        )
    } else {
        // Fallback to CircularProgressIndicator if Lottie file is not found
        CircularProgressIndicator(
            modifier = modifier.size(200.dp)
        )
    }
}

