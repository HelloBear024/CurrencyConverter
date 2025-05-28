package com.currecy.mycurrencyconverter.widgets

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import com.currecy.mycurrencyconverter.database.preferencess.userCurrencyList.UserCurrencyPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetUpdateRegistrar @Inject constructor(
    @ApplicationContext private val appContext: Context,
    prefsRepo: UserCurrencyPreferencesRepository
) : CurrencyPreferencesChangeListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        prefsRepo.registerListener(this)
        Log.d("WidgetUpdateRegistrar", "Registered for prefs changes")

    }

    override fun onPreferencesChanged() {
        Log.d("WidgetUpdateRegistrar", "Prefs changed – scheduling widget update")
        scope.launch {
            ConversionWidget().updateAll(appContext)

            val manager = GlanceAppWidgetManager(context = appContext)
            val widget = ConversionWidget()
            val glanceIds = manager.getGlanceIds(ConversionWidget::class.java)
            glanceIds.forEach { glanceId ->
                widget.update(appContext, glanceId)
            }
            Log.d("WidgetUpdateRegistrar", "Widget.updateAll() complete")
        }
    }
}
