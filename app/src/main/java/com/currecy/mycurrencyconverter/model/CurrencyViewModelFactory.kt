package com.currecy.mycurrencyconverter.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.currecy.mycurrencyconverter.database.CurrencyRateDao

//class CurrencyViewModelFactory(
//    private val currencyDao: CurrencyRateDao? = null,
////    private val userPreferenceCurrencyDao: UserCurrencyPreferenceDao? = null
//) : ViewModelProvider.Factory {
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        return when {
//            modelClass.isAssignableFrom(CameraViewModel::class.java) -> {
//                currencyDao?.let {
//                    CameraViewModel(it) as T
//                } ?: throw IllegalArgumentException("CurrencyRateDao is required for CameraViewModel")
//            }
//
//            modelClass.isAssignableFrom(CurrencyViewModel::class.java) -> {
//                currencyDao?.let {
//                    CurrencyViewModel(it) as T
//                } ?: throw IllegalArgumentException("CurrencyRateDao is required for CurrencyViewModel")
//            }
//
//            modelClass.isAssignableFrom(CardCurrencyViewModel::class.java) -> {
//                userPreferenceCurrencyDao?.let {
//                    CardCurrencyViewModel() as T
//                } ?: throw IllegalArgumentException("UserCurrencyPreferenceDao is required for CardCurrencyViewModel")
//            }
//
//            else ->
//                throw IllegalArgumentException("Unknown ViewModel class")
//        }
//    }
//}



