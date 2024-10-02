package com.currecy.mycurrencyconverter.api.NewsAPI

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NewsRetrofitInstance {
    private const val BASE_URL = "https://newsapi.org"

    val api: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApiService::class.java)
    }
}