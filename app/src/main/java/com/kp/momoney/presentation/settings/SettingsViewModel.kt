package com.kp.momoney.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.kp.momoney.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SettingsEvent {
    object MapsToLogin : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    val userEmail: String
        get() = firebaseAuth.currentUser?.email ?: ""

    private val _event = MutableStateFlow<SettingsEvent?>(null)
    val event: StateFlow<SettingsEvent?> = _event.asStateFlow()

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Delete the Database File directly
                context.deleteDatabase("momoney_database")
                
                // 2. Clear Shared Preferences (if any libraries generated them)
                val packageName = context.packageName
                val sharedPrefs = context.getSharedPreferences(
                    packageName + "_preferences",
                    Context.MODE_PRIVATE
                )
                sharedPrefs.edit().clear().apply()
                
                // 3. Clear Cache (Optional but good for privacy)
                try {
                    context.cacheDir.deleteRecursively()
                } catch (e: Exception) {
                    // Log but don't fail logout if cache deletion fails
                    e.printStackTrace()
                }
                
                // 4. Sign out of Firebase
                authRepository.logout()
                
                // 5. Navigate
                _event.value = SettingsEvent.MapsToLogin
            } catch (e: Exception) {
                // Handle any exceptions during file deletion safely
                // Still attempt to sign out and navigate even if cleanup fails
                e.printStackTrace()
                try {
                    authRepository.logout()
                    _event.value = SettingsEvent.MapsToLogin
                } catch (logoutException: Exception) {
                    logoutException.printStackTrace()
                    // If logout also fails, still try to navigate
                    _event.value = SettingsEvent.MapsToLogin
                }
            }
        }
    }

    fun clearEvent() {
        _event.value = null
    }
}

