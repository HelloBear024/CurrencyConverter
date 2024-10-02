package com.currecy.mycurrencyconverter.widgets

//
//class ConversionWidget: GlanceAppWidget() {
//
//    override suspend fun provideGlance(context: Context, id: GlanceId) {
//        // Load data from SharedPreferences or a local database
//        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
//        val amount = prefs.getString("amount", "1.00") ?: "1.00"
//        val fromCurrency = prefs.getString("from_currency", "USD") ?: "USD"
//        val toCurrency = prefs.getString("to_currency", "EUR") ?: "EUR"
////        val convertedAmount = performConversion(amount, fromCurrency, toCurrency)
//
//        provideContent {
//            CurrencyConverterContent(
//                amount = amount,
//                fromCurrency = fromCurrency,
//                toCurrency = toCurrency,
//                convertedAmount = convertedAmount
//            )
//        }
//    }
//
//    @Composable
//    fun CurrencyConverterContent(
//        amount: String,
//        fromCurrency: String,
//        toCurrency: String,
//        convertedAmount: String
//    ) {
//        Column(
//            modifier = GlanceModifier
//                .fillMaxSize()
//                .padding(16.dp)
//                .clickable(actionStartActivity<MainActivity>())
//        ) {
//            Text(
//                text = "$amount $fromCurrency equals",
//                style = TextStyle(fontSize = 16.sp)
//            )
//            Text(
//                text = "$convertedAmount $toCurrency",
//                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
//            )
//            Spacer(modifier = GlanceModifier.height(8.dp))
//            Button(
//                text = "Refresh",
//                onClick = actionRunCallback<RefreshRatesAction>()
//            )
//        }
//    }
//}
