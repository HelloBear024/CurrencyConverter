package com.currecy.mycurrencyconverter.model.chartModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.currecy.mycurrencyconverter.api.NewsAPI.Article
import com.currecy.mycurrencyconverter.api.NewsAPI.NewsApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NewsViewModel @Inject constructor(
    private val api: NewsApiService
): ViewModel() {

    private val _newsArticles = MutableStateFlow<List<Article>>(emptyList())
    val newsArticles: StateFlow<List<Article>> = _newsArticles

    fun fetchNews(sourceCurrency: String, targetCurrency: String) {
        viewModelScope.launch {
            try {

                val sourceCurrencyQuery = "$sourceCurrency=X"

                val targetCurrencyQuery = "$targetCurrency=X"


                val sourceNewsDeferred = async { api.getNews(query = sourceCurrencyQuery , apiKey = "8b268bfa35e34dd79303e8d2c29a20a2") }
                val targetNewsDeferred = async { api.getNews(query = targetCurrencyQuery, apiKey = "8b268bfa35e34dd79303e8d2c29a20a2") }

                // Await both responses
                val sourceNews = sourceNewsDeferred.await()
                val targetNews = targetNewsDeferred.await()

                // Check if both responses are successful
                if (sourceNews.status == "ok" && targetNews.status == "ok") {
                    // Interleave the articles alternately
                    val combinedNews = interleaveArticles(sourceNews.articles, targetNews.articles)
                    _newsArticles.value = combinedNews
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    private fun interleaveArticles(
        sourceArticles: List<Article>,
        targetArticles: List<Article>
    ): List<Article> {
        val combinedList = mutableListOf<Article>()
        val maxLength = maxOf(sourceArticles.size, targetArticles.size)

        // Interleave the articles
        for (i in 0 until maxLength) {
            if (i < sourceArticles.size) {
                combinedList.add(sourceArticles[i])
            }
            if (i < targetArticles.size) {
                combinedList.add(targetArticles[i])
            }
        }

        return combinedList
    }

}