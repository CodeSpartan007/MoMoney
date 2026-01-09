package com.kp.momoney.util

import android.app.Activity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Helper function to show biometric authentication prompt.
 * 
 * @param activity The Activity context (must be FragmentActivity or AppCompatActivity)
 * @param onAuthSuccess Callback invoked when authentication succeeds
 */
fun showBiometricPrompt(
    activity: Activity,
    onAuthSuccess: () -> Unit
) {
    // BiometricPrompt requires FragmentActivity
    val fragmentActivity = activity as? FragmentActivity
        ?: return // Silently fail if not FragmentActivity - user can use PIN instead
    
    val executor = ContextCompat.getMainExecutor(fragmentActivity)
    
    val biometricPrompt = BiometricPrompt(
        fragmentActivity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onAuthSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // User cancelled or error occurred - do nothing, let them use PIN
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Authentication failed - do nothing, let them try again or use PIN
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock MoMoney")
        .setNegativeButtonText("Use PIN")
        .build()

    biometricPrompt.authenticate(promptInfo)
}

