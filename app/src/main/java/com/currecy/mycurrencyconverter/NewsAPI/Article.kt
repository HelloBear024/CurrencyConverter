package com.currecy.mycurrencyconverter.NewsAPI

data class Article(
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String
)
