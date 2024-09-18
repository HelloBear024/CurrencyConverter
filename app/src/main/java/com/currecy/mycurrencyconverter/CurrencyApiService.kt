package com.currecy.mycurrencyconverter

import retrofit2.http.GET
import retrofit2.http.Url

interface CurrencyApiService {
    @GET
    suspend fun getCurrencyRates(@Url url: String): CurrencyResponse

}