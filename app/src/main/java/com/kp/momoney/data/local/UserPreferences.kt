package com.kp.momoney.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.currencyDataStore: DataStore<Preferences> by preferencesDataStore("currency_prefs")

data class CurrencyPreference(
    val currencyCode: String,
    val currencySymbol: String,
    val exchangeRate: Float
)

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val CURRENCY_CODE_KEY = stringPreferencesKey("currency_code")
        private val CURRENCY_SYMBOL_KEY = stringPreferencesKey("currency_symbol")
        private val EXCHANGE_RATE_KEY = floatPreferencesKey("exchange_rate")
        
        private const val DEFAULT_CURRENCY_CODE = "KES"
        private const val DEFAULT_CURRENCY_SYMBOL = "KSh"
        private const val DEFAULT_EXCHANGE_RATE = 1.0f
    }

    val currencyPreference: Flow<CurrencyPreference> = context.currencyDataStore.data.map { preferences ->
        CurrencyPreference(
            currencyCode = preferences[CURRENCY_CODE_KEY] ?: DEFAULT_CURRENCY_CODE,
            currencySymbol = preferences[CURRENCY_SYMBOL_KEY] ?: DEFAULT_CURRENCY_SYMBOL,
            exchangeRate = preferences[EXCHANGE_RATE_KEY] ?: DEFAULT_EXCHANGE_RATE
        )
    }

    suspend fun updateCurrency(
        currencyCode: String,
        currencySymbol: String,
        exchangeRate: Float
    ) {
        context.currencyDataStore.edit { preferences ->
            preferences[CURRENCY_CODE_KEY] = currencyCode
            preferences[CURRENCY_SYMBOL_KEY] = currencySymbol
            preferences[EXCHANGE_RATE_KEY] = exchangeRate
        }
    }
}


