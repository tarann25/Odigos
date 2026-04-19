package com.example.odigos

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object TimetableWidgetHelper {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun updateAll(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            val manager = GlanceAppWidgetManager(appContext)
            // We reference the class only for ID lookup
            val glanceIds = manager.getGlanceIds(TimetableWidget::class.java)
            glanceIds.forEach { glanceId ->
                TimetableWidget().update(appContext, glanceId)
            }
        }
    }
}