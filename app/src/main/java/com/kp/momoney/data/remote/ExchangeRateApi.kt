package com.kp.momoney.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

data class ExchangeRateResponse(
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)

interface ExchangeRateApi {
    @GET("v4/latest/{base}")
    suspend fun getLatestRates(@Path("base") base: String): ExchangeRateResponse
}


