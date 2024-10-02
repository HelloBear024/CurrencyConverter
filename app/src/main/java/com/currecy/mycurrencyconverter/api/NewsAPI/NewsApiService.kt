package com.currecy.mycurrencyconverter.api.NewsAPI

import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("v2/everything")
    suspend fun getNews(
        @Query("q") query: String,
        @Query("apiKey") apiKey: String    // Your API key for authentication
    ): NewsResponse
}