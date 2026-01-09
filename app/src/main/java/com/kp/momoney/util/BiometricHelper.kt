package com.kp.momoney.util

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Helper function to show biometric authentication prompt.
 * 
 * @param activity The FragmentActivity context (must be FragmentActivity or AppCompatActivity)
 * @param onAuthSuccess Callback invoked when authentication succeeds
 */
fun showBiometricPrompt(
    activity: FragmentActivity,
    onAuthSuccess: () -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    
    val biometricPrompt = BiometricPrompt(
        activity,
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

