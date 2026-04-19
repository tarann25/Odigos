package com.example.odigos

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class WidgetUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Refresh the widget to update "Current Class" indicators
        val glanceAppWidgetManager = GlanceAppWidgetManager(context)
        val glanceIds = glanceAppWidgetManager.getGlanceIds(TimetableWidget::class.java)

        glanceIds.forEach { glanceId ->
            TimetableWidget().update(context, glanceId)
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "widget_update_work"

        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                15, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Don't replace if already running
                workRequest
            )
        }
    }
}