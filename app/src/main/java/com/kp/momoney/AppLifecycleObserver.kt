package com.kp.momoney

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.kp.momoney.presentation.MainViewModel

/**
 * Lifecycle observer that locks the app when it goes to background.
 * Uses ProcessLifecycleOwner to only trigger when the UI is completely hidden,
 * not when rotating screens or other activity lifecycle events occur.
 */
class AppLifecycleObserver(
    private val viewModel: MainViewModel
) : DefaultLifecycleObserver {
    
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        // Lock the app when it goes to background
        // This ensures that as soon as the app is backgrounded (Home pressed or Recents switched),
        // the flag flips to TRUE.
        viewModel.lockApp()
    }
}

