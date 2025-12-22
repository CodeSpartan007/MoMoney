package com.kp.momoney.presentation.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.kp.momoney.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isPasswordVisible = MutableStateFlow(false)
    val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun onEmailChanged(value: String) {
        _email.value = value
    }

    fun onPasswordChanged(value: String) {
        _password.value = value
    }

    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }

    fun login() {
        val email = _email.value.trim()
        val password = _password.value

        val validationError = validate(email, password)
        if (validationError != null) {
            _authState.value = AuthState.Error(validationError)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.login(email, password).collect { result ->
                result.fold(
                    onSuccess = { user -> _authState.value = AuthState.Success(user) },
                    onFailure = { error ->
                        _authState.value = AuthState.Error(error.message ?: "Login failed")
                    }
                )
            }
        }
    }

    fun register() {
        val email = _email.value.trim()
        val password = _password.value

        val validationError = validate(email, password)
        if (validationError != null) {
            _authState.value = AuthState.Error(validationError)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.register(email, password).collect { result ->
                result.fold(
                    onSuccess = { user -> _authState.value = AuthState.Success(user) },
                    onFailure = { error ->
                        _authState.value = AuthState.Error(error.message ?: "Registration failed")
                    }
                )
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    private fun validate(email: String, password: String): String? {
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Please enter a valid email"
        }
        if (password.length < 6) {
            return "Password must be at least 6 characters"
        }
        return null
    }
}


