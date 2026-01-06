package com.kp.momoney.presentation

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kp.momoney.data.local.AppTheme
import com.kp.momoney.data.local.UserPreferencesRepository
import com.kp.momoney.data.util.ConnectivityObserver
import com.kp.momoney.ui.theme.SunYellow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
    connectivityObserver: ConnectivityObserver
) : ViewModel() {

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
}

