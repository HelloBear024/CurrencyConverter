package com.currecy.mycurrencyconverter

import retrofit2.http.GET
import retrofit2.http.Path

interface CurrencyApiService {

    @GET("v1/currencies/{currencyCode}.json")
    suspend fun getCurrencyRates(@Path("currencyCode") currencyCode: String): CurrencyResponse

}