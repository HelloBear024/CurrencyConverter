package com.currecy.mycurrencyconverter.NewsAPI

data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<Article>
)
