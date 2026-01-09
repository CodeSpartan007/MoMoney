package com.kp.momoney.presentation.settings

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.graphics.Color
import com.kp.momoney.data.local.AppTheme
import com.kp.momoney.data.local.CurrencyPreference
import com.kp.momoney.data.local.UserPreferencesRepository
import com.kp.momoney.data.repository.AppLockRepository
import com.kp.momoney.domain.repository.CurrencyRepository
import com.kp.momoney.domain.repository.TransactionRepository
import com.kp.momoney.util.CsvUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val transactionRepository: TransactionRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val currencyRepository: CurrencyRepository,
    private val appLockRepository: AppLockRepository
) : ViewModel() {

    val userEmail: String
        get() = firebaseAuth.currentUser?.email ?: "Guest"

    /**
     * Current theme preference as a Flow
     */
    val currentTheme = userPreferencesRepository.theme

    /**
     * Computed boolean indicating if dark theme is enabled
     * Maps AppTheme to boolean (DARK -> true, LIGHT/SYSTEM -> false)
     */
    val isDarkTheme: Flow<Boolean> = 
        userPreferencesRepository.theme.map { theme ->
            theme == AppTheme.DARK
        }

    /**
     * Current currency preference as a Flow
     */
    val currentCurrency: Flow<CurrencyPreference> = currencyRepository.getCurrencyPreference()

    /**
     * Loading state for currency update operation
     */
    private val _isUpdatingCurrency = MutableStateFlow(false)
    val isUpdatingCurrency: StateFlow<Boolean> = _isUpdatingCurrency.asStateFlow()

    /**
     * App lock enabled state as a Flow
     */
    val isAppLockEnabled: Flow<Boolean> = appLockRepository.isAppLockEnabled()

    /**
     * State for showing PIN setup screen
     */
    private val _showPinSetup = MutableStateFlow(false)
    val showPinSetup: StateFlow<Boolean> = _showPinSetup.asStateFlow()

    /**
     * State for showing PIN verification dialog
     */
    private val _showPinVerification = MutableStateFlow(false)
    val showPinVerification: StateFlow<Boolean> = _showPinVerification.asStateFlow()

    /**
     * Error message for PIN verification
     */
    private val _pinVerificationError = MutableStateFlow<String?>(null)
    val pinVerificationError: StateFlow<String?> = _pinVerificationError.asStateFlow()

    /**
     * Set the theme preference
     */
    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            userPreferencesRepository.setTheme(theme)
        }
    }

    /**
     * Toggle dark mode on/off
     * Maps boolean to AppTheme (true -> DARK, false -> LIGHT)
     */
    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            val newTheme = if (isDark) AppTheme.DARK else AppTheme.LIGHT
            userPreferencesRepository.setTheme(newTheme)
        }
    }

    /**
     * Set the seed color preference
     */
    fun setSeedColor(color: Color) {
        viewModelScope.launch {
            userPreferencesRepository.setSeedColor(color)
        }
    }

    /**
     * Update currency preference by fetching latest exchange rate
     */
    fun updateCurrency(newCode: String) {
        viewModelScope.launch {
            _isUpdatingCurrency.value = true
            try {
                val result = currencyRepository.setCurrency(newCode)
                result.fold(
                    onSuccess = {
                        // Success - currency updated
                    },
                    onFailure = { error ->
                        // Error handling could be added here (e.g., show snackbar)
                        error.printStackTrace()
                    }
                )
            } finally {
                _isUpdatingCurrency.value = false
            }
        }
    }

    fun exportData(context: Context) {
        viewModelScope.launch {
            try {
                // Fetch all transactions
                val transactions = transactionRepository.getAllTransactions().first()
                
                // Generate CSV string
                val csvContent = CsvUtils.generateCsv(transactions)
                
                // Write to file
                val file = File(context.cacheDir, "finance_export.csv")
                file.writeText(csvContent)
                
                // Get URI using FileProvider
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                // Create intent to share
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                // Launch share chooser
                context.startActivity(Intent.createChooser(intent, "Export via"))
            } catch (e: Exception) {
                // Error handling - could show a toast or snackbar
                e.printStackTrace()
            }
        }
    }

    /**
     * Request to enable app lock - shows PIN setup screen
     */
    fun requestEnableAppLock() {
        _showPinSetup.value = true
    }

    /**
     * Called when PIN setup is successful
     */
    fun onPinSetupSuccess() {
        _showPinSetup.value = false
    }

    /**
     * Called when PIN setup is cancelled
     */
    fun onPinSetupCancelled() {
        _showPinSetup.value = false
    }

    /**
     * Request to disable app lock - shows PIN verification dialog
     */
    fun requestDisableAppLock() {
        _pinVerificationError.value = null
        _showPinVerification.value = true
    }

    /**
     * Verify PIN and disable app lock if correct
     */
    fun verifyAndDisableAppLock(pin: String) {
        viewModelScope.launch {
            val isValid = appLockRepository.verifyPin(pin)
            if (isValid) {
                appLockRepository.disableAppLock()
                _showPinVerification.value = false
                _pinVerificationError.value = null
            } else {
                _pinVerificationError.value = "Incorrect PIN"
            }
        }
    }

    /**
     * Cancel PIN verification
     */
    fun cancelPinVerification() {
        _showPinVerification.value = false
        _pinVerificationError.value = null
    }
}

