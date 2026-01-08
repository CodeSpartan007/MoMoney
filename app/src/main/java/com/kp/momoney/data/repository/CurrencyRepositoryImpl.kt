package com.kp.momoney.data.repository

import com.kp.momoney.data.local.CurrencyPreference
import com.kp.momoney.data.local.UserPreferences
import com.kp.momoney.data.remote.ExchangeRateApi
import com.kp.momoney.domain.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(
    private val exchangeRateApi: ExchangeRateApi,
    private val userPreferences: UserPreferences
) : CurrencyRepository {

    companion object {
        private val CURRENCY_SYMBOLS = mapOf(
            "USD" to "$",
            "EUR" to "€",
            "GBP" to "£",
            "KES" to "KSh",
            "JPY" to "¥",
            "CNY" to "¥",
            "INR" to "₹",
            "AUD" to "A$",
            "CAD" to "C$",
            "CHF" to "CHF",
            "NZD" to "NZ$",
            "SGD" to "S$",
            "HKD" to "HK$",
            "ZAR" to "R",
            "NGN" to "₦",
            "EGP" to "E£",
            "TZS" to "TSh",
            "UGX" to "USh",
            "ETB" to "Br"
        )

        private val COMMON_CURRENCIES = listOf(
            "USD", "EUR", "GBP", "KES", "JPY", "CNY", "INR",
            "AUD", "CAD", "CHF", "NZD", "SGD", "HKD", "ZAR",
            "NGN", "EGP", "TZS", "UGX", "ETB"
        )
    }

    override fun getCurrencyPreference(): Flow<CurrencyPreference> {
        return userPreferences.currencyPreference
    }

    override suspend fun getAvailableCurrencies(): List<String> {
        return COMMON_CURRENCIES
    }

    override suspend fun setCurrency(targetCode: String): Result<Unit> {
        return try {
            // Get latest rates for KES base
            val response = exchangeRateApi.getLatestRates("KES")
            
            // Extract the rate for target currency
            val rate = response.rates[targetCode]
                ?: return Result.failure(IllegalArgumentException("Currency code $targetCode not found in exchange rates"))
            
            // Get currency symbol
            val symbol = CURRENCY_SYMBOLS[targetCode] ?: targetCode
            
            // Save to DataStore
            userPreferences.updateCurrency(
                currencyCode = targetCode,
                currencySymbol = symbol,
                exchangeRate = rate.toFloat()
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


