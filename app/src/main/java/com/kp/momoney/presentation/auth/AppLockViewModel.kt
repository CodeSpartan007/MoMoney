package com.kp.momoney.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kp.momoney.data.repository.AppLockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AppLockMode {
    object Unlock : AppLockMode()
    object Setup : AppLockMode()
}

sealed class AppLockUiState {
    object Idle : AppLockUiState()
    object Verifying : AppLockUiState()
    data class Error(val message: String) : AppLockUiState()
    object Success : AppLockUiState()
}

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val appLockRepository: AppLockRepository
) : ViewModel() {

    private val _inputPin = MutableStateFlow("")
    val inputPin: StateFlow<String> = _inputPin.asStateFlow()

    private val _confirmPin = MutableStateFlow("")
    val confirmPin: StateFlow<String> = _confirmPin.asStateFlow()

    private val _uiState = MutableStateFlow<AppLockUiState>(AppLockUiState.Idle)
    val uiState: StateFlow<AppLockUiState> = _uiState.asStateFlow()

    private val _isSetupMode = MutableStateFlow(false)
    val isSetupMode: StateFlow<Boolean> = _isSetupMode.asStateFlow()

    private val _isConfirming = MutableStateFlow(false)
    val isConfirming: StateFlow<Boolean> = _isConfirming.asStateFlow()

    private val _shouldShake = MutableStateFlow(false)
    val shouldShake: StateFlow<Boolean> = _shouldShake.asStateFlow()

    fun setSetupMode(isSetup: Boolean) {
        _isSetupMode.value = isSetup
        _isConfirming.value = false
        _inputPin.value = ""
        _confirmPin.value = ""
        _uiState.value = AppLockUiState.Idle
    }

    fun onNumberClick(number: Int) {
        if (_uiState.value is AppLockUiState.Verifying) return

        val currentPin = if (_isConfirming.value) _confirmPin.value else _inputPin.value
        if (currentPin.length < 6) {
            val newPin = currentPin + number.toString()
            if (_isConfirming.value) {
                _confirmPin.value = newPin
            } else {
                _inputPin.value = newPin
            }

            // Auto-verify when PIN is complete
            if (newPin.length == 6) {
                if (_isSetupMode.value && !_isConfirming.value) {
                    // First PIN entered in setup mode, switch to confirmation
                    _isConfirming.value = true
                } else {
                    // Verify or confirm PIN
                    verifyOrConfirmPin()
                }
            }
        }
    }

    fun onDeleteClick() {
        if (_uiState.value is AppLockUiState.Verifying) return

        if (_isConfirming.value) {
            _confirmPin.value = _confirmPin.value.dropLast(1)
        } else {
            _inputPin.value = _inputPin.value.dropLast(1)
        }
        _uiState.value = AppLockUiState.Idle
    }

    private fun verifyOrConfirmPin() {
        viewModelScope.launch {
            _uiState.value = AppLockUiState.Verifying

            if (_isSetupMode.value) {
                // Setup mode: confirm PIN
                if (_isConfirming.value) {
                    if (_inputPin.value == _confirmPin.value) {
                        // PINs match, save to repository
                        appLockRepository.savePin(_inputPin.value)
                        _uiState.value = AppLockUiState.Success
                    } else {
                        // PINs don't match, show error
                        _uiState.value = AppLockUiState.Error("PINs do not match")
                        _shouldShake.value = true
                        _inputPin.value = ""
                        _confirmPin.value = ""
                        _isConfirming.value = false
                    }
                }
            } else {
                // Unlock mode: verify PIN
                val isValid = appLockRepository.verifyPin(_inputPin.value)
                if (isValid) {
                    _uiState.value = AppLockUiState.Success
                } else {
                    _uiState.value = AppLockUiState.Error("Incorrect PIN")
                    _shouldShake.value = true
                    _inputPin.value = ""
                }
            }
        }
    }

    fun resetShake() {
        _shouldShake.value = false
    }

    fun clearError() {
        if (_uiState.value is AppLockUiState.Error) {
            _uiState.value = AppLockUiState.Idle
        }
    }
}

