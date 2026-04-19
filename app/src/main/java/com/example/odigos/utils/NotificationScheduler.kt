package com.example.odigos.utils

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.odigos.domain.model.TimetableEntry
import com.example.odigos.workers.ClassNotificationWorker
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun scheduleNotifications(context: Context, schedule: List<TimetableEntry>) {
        val workManager = WorkManager.getInstance(context)
        
        // Cancel all previous class alerts to avoid duplicates
        // Note: This cancels everything, so we must reschedule everything.
        workManager.cancelAllWorkByTag("class_alert")

        schedule.forEach { entry ->
            scheduleNotification(
                context = context,
                id = entry.id,
                subject = entry.subjectName,
                room = entry.roomCode,
                dayOfWeek = entry.dayOfWeek,
                startHour = entry.startHour
            )
        }
    }

    fun scheduleNotification(
        context: Context,
        id: String,
        subject: String,
        room: String,
        dayOfWeek: Int,
        startHour: Float
    ) {
        val startHourInt = startHour.toInt()
        val delay = calculateDelay(dayOfWeek, startHourInt)
        
        // Only schedule if we have a valid delay (logic always returns +ve delay for future)
        val data = Data.Builder()
            .putString("id", id)
            .putString("subject", subject)
            .putString("room", room)
            .putInt("dayOfWeek", dayOfWeek)
            .putInt("startHour", startHourInt)
            .build()

        val request = OneTimeWorkRequestBuilder<ClassNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("class_alert")
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "alert_$id",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun calculateDelay(dayOfWeek: Int, startHour: Int): Long {
        val now = LocalDateTime.now()
        val targetDay = DayOfWeek.of(dayOfWeek) // 1=Mon
        val targetTime = LocalTime.of(startHour, 0).minusMinutes(10) // 10 mins before

        var nextOccurrence = now.with(TemporalAdjusters.nextOrSame(targetDay)).with(targetTime)

        // If today is the day but time has passed (or is strictly now), move to next week
        // We add a small buffer (e.g. 1 minute) to ensure we don't schedule for "now" if running exactly at time
        if (!nextOccurrence.isAfter(now.plusSeconds(30))) {
            nextOccurrence = nextOccurrence.plusWeeks(1)
        }

        return Duration.between(now, nextOccurrence).toMillis()
    }
}