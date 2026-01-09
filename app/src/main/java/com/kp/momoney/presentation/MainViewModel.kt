package com.kp.momoney.presentation

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kp.momoney.data.local.AppTheme
import com.kp.momoney.data.local.UserPreferencesRepository
import com.kp.momoney.data.repository.AppLockRepository
import com.kp.momoney.data.util.ConnectivityObserver
import com.kp.momoney.ui.theme.SunYellow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
    connectivityObserver: ConnectivityObserver,
    appLockRepository: AppLockRepository
) : ViewModel() {

    // Initialize to true for security: assume locked until proven otherwise
    // This ensures the lock screen appears immediately on cold start if app lock is enabled
    private val _isAppLocked = MutableStateFlow<Boolean>(true)

    init {
        // Cold Start Logic: Read appLockRepository.isAppLockEnabled() immediately
        // If true, isAppLocked remains true (locked). If false, set to false (unlocked).
        viewModelScope.launch {
            val isLockEnabled = appLockRepository.isAppLockEnabled().first()
            _isAppLocked.value = isLockEnabled
        }
    }

    /**
     * Theme state flow with initial value of LIGHT
     * Uses SharingStarted.Lazily to start sharing when first collector subscribes
     */
    val theme: StateFlow<AppTheme> = userPreferencesRepository.theme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = AppTheme.LIGHT
        )

    /**
     * Seed color state flow with initial value of SunYellow
     * Uses SharingStarted.Lazily to start sharing when first collector subscribes
     */
    val seedColor: StateFlow<Color> = userPreferencesRepository.seedColor
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = SunYellow
        )

    /**
     * Connectivity status flow
     */
    val connectivityStatus: StateFlow<ConnectivityObserver.Status> = connectivityObserver.observe()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = ConnectivityObserver.Status.Available
        )

    /**
     * Boolean state indicating if the device is offline
     * True when status is Lost or Unavailable
     */
    val isOffline: StateFlow<Boolean> = connectivityStatus
        .map { status ->
            status == ConnectivityObserver.Status.Lost || 
            status == ConnectivityObserver.Status.Unavailable
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = false
        )

    /**
     * State indicating if the app is currently locked
     * Defaults to true if app lock is enabled
     */
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    /**
     * Unlock the app by setting isAppLocked to false
     */
    fun unlockApp() {
        _isAppLocked.value = false
    }

    /**
     * Lock the app by setting isAppLocked to true
     */
    fun lockApp() {
        _isAppLocked.value = true
    }
}

