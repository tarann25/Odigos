package com.example.odigos.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.odigos.utils.NotificationHelper
import com.example.odigos.utils.NotificationScheduler

class ClassNotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val id = inputData.getString("id") ?: return Result.failure()
        val subject = inputData.getString("subject") ?: "Class"
        val room = inputData.getString("room") ?: "Campus"
        val dayOfWeek = inputData.getInt("dayOfWeek", -1)
        val startHour = inputData.getFloat("startHour", -1f)

        // 1. Show the notification
        NotificationHelper.showNotification(applicationContext, subject, room)

        // 2. Reschedule for next week if data is valid
        if (dayOfWeek != -1 && startHour != -1f) {
            NotificationScheduler.scheduleNotification(
                context = applicationContext,
                id = id,
                subject = subject,
                room = room,
                dayOfWeek = dayOfWeek,
                startHour = startHour
            )
        }

        return Result.success()
    }
}