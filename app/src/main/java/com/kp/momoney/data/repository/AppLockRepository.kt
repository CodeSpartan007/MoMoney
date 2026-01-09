package com.kp.momoney.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_FILE_NAME = "auth_prefs"
        private const val MASTER_KEY_ALIAS = "_androidx_security_crypto_encrypted_prefs_key_"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
    }

    private val encryptedPrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            PREFS_FILE_NAME,
            MASTER_KEY_ALIAS,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val _isAppLockEnabled = MutableStateFlow(false)

    init {
        // Initialize the state from encrypted prefs
        _isAppLockEnabled.value = try {
            encryptedPrefs.getBoolean(KEY_APP_LOCK_ENABLED, false)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Flow indicating whether app lock is enabled
     */
    fun isAppLockEnabled(): Flow<Boolean> = _isAppLockEnabled.asStateFlow()

    /**
     * Hash the PIN using SHA-256
     */
    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(pin.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Save the PIN (hashed) and enable app lock
     */
    fun savePin(pin: String) {
        val hashedPin = hashPin(pin)
        encryptedPrefs.edit()
            .putString(KEY_PIN_HASH, hashedPin)
            .putBoolean(KEY_APP_LOCK_ENABLED, true)
            .apply()
        _isAppLockEnabled.value = true
    }

    /**
     * Verify the input PIN against the stored hash
     */
    fun verifyPin(inputPin: String): Boolean {
        val storedHash = encryptedPrefs.getString(KEY_PIN_HASH, null)
            ?: return false
        
        val inputHash = hashPin(inputPin)
        return storedHash == inputHash
    }

    /**
     * Disable app lock by clearing the PIN and flag
     */
    fun disableAppLock() {
        encryptedPrefs.edit()
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_APP_LOCK_ENABLED, false)
            .apply()
        _isAppLockEnabled.value = false
    }
}

