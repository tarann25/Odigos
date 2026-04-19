@file:android.annotation.SuppressLint("RestrictedApi")

package com.example.odigos

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.odigos.di.WidgetEntryPoint
import com.example.odigos.domain.model.TimetableEntry
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

class TimetableWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Hilt Injection via EntryPoint
        val repository = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        ).getTimetableRepository()

        // Fetch data from Room (IO safe due to suspend)
        val schedule = try {
            repository.getSchedule().first()
        } catch (e: Exception) {
            emptyList()
        }

        val startHour = if (schedule.isEmpty()) 9 else schedule.minOf { it.startHour.toInt() }
        val endHour = if (schedule.isEmpty()) 17 else schedule.maxOf { (it.startHour + it.duration).toInt() }

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF1C1C1E))
            ) {
                Column(modifier = GlanceModifier.fillMaxSize()) {
                    TimeHeaderRow(startHour, endHour)
                    LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        val maxDay = schedule.maxOfOrNull { it.dayOfWeek } ?: 6
                        val visibleDaysCount = maxDay.coerceAtLeast(5)

                        items(visibleDaysCount) { index ->
                            DayRow(
                                dayIndex = index + 1,
                                dayName = days.getOrElse(index) { "Day ${index + 1}" },
                                schedule = schedule,
                                startHour = startHour,
                                endHour = endHour
                            )
                            Spacer(modifier = GlanceModifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun TimeHeaderRow(startHour: Int, endHour: Int) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Spacer(modifier = GlanceModifier.width(24.dp))
            for (hour in startHour until endHour) {
                Box(
                    modifier = GlanceModifier.defaultWeight(),
                    contentAlignment = Alignment.Center
                ) {
                    val startLabel = if (hour < 12) "${hour}AM" else if (hour == 12) "12PM" else "${hour - 12}PM"
                    val nextHour = hour + 1
                    val endLabel = if (nextHour < 12) "${nextHour}AM" else if (nextHour == 12) "12PM" else "${nextHour - 12}PM"
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = startLabel, style = TextStyle(color = ColorProvider(Color(0xFF999999)), fontSize = 8.sp))
                        Text(text = "-", style = TextStyle(color = ColorProvider(Color(0xFF999999)), fontSize = 8.sp))
                        Text(text = endLabel, style = TextStyle(color = ColorProvider(Color(0xFF999999)), fontSize = 8.sp))
                    }
                }
            }
        }
    }

    @Composable
    fun DayRow(
        dayIndex: Int,
        dayName: String,
        schedule: List<TimetableEntry>,
        startHour: Int,
        endHour: Int
    ) {
        val now = LocalDateTime.now()
        val currentDay = now.dayOfWeek.value
        val currentHour = now.hour
        val isToday = dayIndex == currentDay

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(42.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .width(24.dp)
                    .fillMaxHeight()
                    .padding(end = 4.dp)
                    .background(if (isToday) Color(0xFF2D4A2D) else Color.Transparent)
                    .cornerRadius(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayName.take(1),
                    style = TextStyle(
                        color = ColorProvider(if (isToday) Color(0xFF66BB6A) else Color(0xFFDDDDDD)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            var hour = startHour
            while (hour < endHour) {
                val classEntry = schedule.find { it.dayOfWeek == dayIndex && it.startHour == hour.toFloat() }

                if (classEntry != null) {
                    val duration = classEntry.duration.toInt()
                    val isCurrentClass = isToday && (currentHour >= classEntry.startHour && currentHour < classEntry.startHour + duration)

                    Box(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight()
                            .padding(1.dp)
                    ) {
                        Box(
                             modifier = GlanceModifier
                                 .fillMaxSize()
                                 .background(if (isCurrentClass) Color.Black else Color.Transparent)
                                 .cornerRadius(8.dp)
                        ) {
                             Box(
                                 modifier = GlanceModifier
                                     .fillMaxSize()
                                     .padding(if (isCurrentClass) 2.dp else 0.dp)
                                     .background(Color(classEntry.colorHex))
                                     .cornerRadius(8.dp),
                                 contentAlignment = Alignment.Center
                             ) {
                                 Column(
                                     horizontalAlignment = Alignment.CenterHorizontally,
                                     verticalAlignment = Alignment.CenterVertically,
                                     modifier = GlanceModifier.padding(2.dp)
                                 ) {
                                     Text(
                                         text = classEntry.subjectName,
                                         style = TextStyle(
                                             color = ColorProvider(Color.White),
                                             fontSize = 8.sp,
                                             fontWeight = FontWeight.Bold,
                                             textAlign = TextAlign.Center
                                         ),
                                         maxLines = 1
                                     )
                                     if (classEntry.roomCode.isNotEmpty()) {
                                         Text(
                                             text = classEntry.roomCode,
                                             style = TextStyle(
                                                 color = ColorProvider(Color(0xFFEEEEEE)),
                                                 fontSize = 6.sp,
                                                 textAlign = TextAlign.Center
                                             ),
                                             maxLines = 1
                                         )
                                     }
                                 }
                             }
                        }
                    }
                    hour += duration
                } else {
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight()
                            .padding(0.5.dp)
                            .background(Color(0xFF252525))
                            .cornerRadius(4.dp)
                    ) {}
                    hour++
                }
            }
        }
    }
}
