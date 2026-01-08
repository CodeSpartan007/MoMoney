package com.kp.momoney.domain.repository

import com.kp.momoney.data.local.CurrencyPreference
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {
    fun getCurrencyPreference(): Flow<CurrencyPreference>
    suspend fun getAvailableCurrencies(): List<String>
    suspend fun setCurrency(targetCode: String): Result<Unit>
}


