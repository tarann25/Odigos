package com.example.mytt.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.mytt.R
import kotlin.random.Random

object NotificationHelper {

    const val CHANNEL_ID = "class_alerts"
    private const val CHANNEL_NAME = "Class Alerts"

    private val messages = listOf(
        "Hey! It's time for {subject} in {room} 😊",
        "Hurry up! {subject} is starting soon at {room} 🏃‍♂️",
        "Don't be late! {subject} awaits in {room} ⏰",
        "Ready for some learning? {subject} in {room} 📚",
        "Time to shine! {subject} class in {room} ✨",
        "Grab your books! {subject} is starting in {room} 🎒",
        "Next up: {subject} at {room}. See you there! 👋",
        "Class alert: {subject} in {room} 🔔",
        "Get to {room} for {subject}! Don't miss out 🚀",
        "{subject} is calling! Head to {room} now 📞",
        "Time to focus! {subject} in {room} 🧠",
        "A new lesson awaits! {subject} in {room} 📖",
        "On your way? {subject} starts soon in {room} 🚶‍♀️",
        "Check your schedule! It's {subject} time in {room} 🗓️",
        "Knowledge power! {subject} in {room} 💡",
        "Let's go! {subject} session in {room} 🎓",
        "Your next class: {subject} in {room} 📍",
        "Don't forget: {subject} in {room} right now! ⚡",
        "Time to level up! {subject} in {room} 🆙",
        "Stay sharp! {subject} in {room} ✏️",
        "Almost time! Head over to {room} for {subject} ⌚",
        "Time to learn! {subject} in {room} 🤓",
        "Reminder: {subject} class in {room} 📝",
        "Make your way to {room} for {subject} 🗺️",
        "It's {subject} time! See you in {room} 🤩"
    )

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming classes"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, subject: String, room: String) {
        val template = messages.random()
        val content = template
            .replace("{subject}", subject)
            .replace("{room}", room)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round) // Fallback icon
            .setContentTitle(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Random.nextInt(), builder.build())
    }
}