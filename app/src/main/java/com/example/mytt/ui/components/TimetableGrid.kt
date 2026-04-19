package com.example.mytt.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.mytt.domain.model.TagEntry
import com.example.mytt.domain.model.TimetableEntry
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimetableGrid(
    schedule: List<TimetableEntry>,
    tags: List<TagEntry> = emptyList(),
    onClassClick: (TimetableEntry) -> Unit,
    onEmptySlotLongClick: (Int, Int) -> Unit,
    onUpdateEntry: (TimetableEntry) -> Unit
) {
    // Dynamic Time Calculation (Tight)
    val startHour = remember(schedule) {
        if (schedule.isEmpty()) 9 else schedule.minOf { it.startHour.toInt() }
    }
    
    val endHour = remember(schedule) {
        if (schedule.isEmpty()) 17 else schedule.maxOf { (it.startHour + it.duration).toInt() }
    }

    // Helper for Time Format
    fun formatHour(h: Int): String {
        return when {
            h < 12 -> "${h}AM"
            h == 12 -> "12PM"
            else -> "${h - 12}PM"
        }
    }

    // User requested M, T, W...
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    // Filter days to show only relevant ones (or default to Mon-Fri/Sat if empty)
    val maxDay = remember(schedule) { schedule.maxOfOrNull { it.dayOfWeek } ?: 6 }
    val visibleDays = days.take(maxDay.coerceIn(5, 7)) // Show at least Mon-Fri (5), max Sun (7)

    val cellHeight = 32.dp
    // Reduced header height to tighten the look
    val headerHeight = 40.dp 
    // Reduced time width for single letters
    val timeWidth = 24.dp
    
    val density = LocalDensity.current
    // Fix: Remove ambiguity by explicit multiplication
    val cellHeightPx = with(density) { cellHeight.toPx() } 
    
    val textMeasurer = rememberTextMeasurer()

    var draggingEntry by remember { mutableStateOf<TimetableEntry?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) } 

    // PERFORMANCE FIX: Use BoxWithConstraints for width calculation
    BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        // Calculate cell width once based on available space
        val totalCols = (endHour - startHour).coerceAtLeast(1)
        val availableWidth = maxWidth - timeWidth
        val cellWidthDp = availableWidth / totalCols
        val cellWidthPx = with(density) { cellWidthDp.toPx() }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            // Time Header
            Row(modifier = Modifier.fillMaxWidth().height(headerHeight)) {
                Spacer(modifier = Modifier.width(timeWidth))
                for (hour in startHour until endHour) {
                    Box(
                        modifier = Modifier
                            .width(cellWidthDp) // Use calculated width directly
                            .weight(1f), // Keep weight for safety
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // Stacked Format:
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy((-4).dp),
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Text(text = formatHour(hour), fontSize = 8.sp, color = Color.Gray, maxLines = 1, lineHeight = 8.sp)
                            Text(text = "-", fontSize = 8.sp, color = Color.DarkGray, lineHeight = 8.sp)
                            Text(text = formatHour(hour + 1), fontSize = 8.sp, color = Color.Gray, maxLines = 1, lineHeight = 8.sp)
                        }
                    }
                }
            }

            // Grid Rows
            visibleDays.forEachIndexed { dayIndex, dayName ->
                val dayNum = dayIndex + 1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cellHeight)
                        .padding(vertical = 1.dp) // Reduced padding
                ) {
                    Box(modifier = Modifier.width(timeWidth).fillMaxHeight(), contentAlignment = Alignment.CenterStart) {
                        Text(text = dayName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                    }
                    var hour = startHour
                    while (hour < endHour) {
                        val classEntry = schedule.find { it.dayOfWeek == dayNum && it.startHour == hour.toFloat() }
                        if (classEntry != null) {
                            val isDraggingThis = draggingEntry?.id == classEntry.id
                            // Calculate weight based on duration
                            val itemWeight = classEntry.duration.toFloat()
                            
                            Box(
                                modifier = Modifier
                                    .weight(itemWeight)
                                    .fillMaxHeight()
                                    .padding(horizontal = 1.dp)
                                    .zIndex(if (isDraggingThis) 10f else 0f)
                                    .offset { if (isDraggingThis) IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt()) else IntOffset.Zero }
                                    .pointerInput(classEntry) {
                                        detectDragGestures(
                                            onDragStart = { draggingEntry = classEntry; dragOffset = Offset.Zero },
                                            onDragEnd = {
                                                if (cellWidthPx > 0 && cellHeightPx > 0) {
                                                    val deltaCols = (dragOffset.x / cellWidthPx).roundToInt()
                                                    val deltaRows = (dragOffset.y / cellHeightPx).roundToInt()
                                                    val newDay = (classEntry.dayOfWeek + deltaRows).coerceIn(1, visibleDays.size)
                                                    // Ensure we don't drag out of calculated bounds
                                                    val newStartHour = (classEntry.startHour + deltaCols).coerceIn(startHour.toFloat(), endHour.toFloat() - classEntry.duration)
                                                    if (newDay != classEntry.dayOfWeek || newStartHour != classEntry.startHour) {
                                                        onUpdateEntry(classEntry.copy(dayOfWeek = newDay, startHour = newStartHour))
                                                    }
                                                }
                                                draggingEntry = null; dragOffset = Offset.Zero
                                            },
                                            onDragCancel = { draggingEntry = null; dragOffset = Offset.Zero }
                                        ) { change, dragAmount -> change.consume(); dragOffset += dragAmount }
                                    }
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                                        .combinedClickable(onClick = {}, onLongClick = { if (draggingEntry == null) onClassClick(classEntry) }),
                                    colors = CardDefaults.cardColors(containerColor = Color(classEntry.colorHex)),
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = if (isDraggingThis) 8.dp else 0.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxSize().padding(2.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Text(classEntry.subjectName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                                        if (classEntry.roomCode.isNotEmpty()) Text(classEntry.roomCode, fontSize = 8.sp, color = Color.White.copy(alpha = 0.9f), maxLines = 1, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                            hour += classEntry.duration.toInt()
                        } else {
                            val currentSlotHour = hour
                            Box(
                                modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 1.dp)
                                    .clip(RoundedCornerShape(4.dp)).background(Color(0xFF1E1E1E))
                                    .combinedClickable(onClick = {}, onLongClick = { onEmptySlotLongClick(dayNum, currentSlotHour) })
                            )
                            hour++
                        }
                    }
                }
            }
        }
        
        // TAG OVERLAY (Canvas)
        if (cellWidthPx > 0f) {
            val timeWidthPx = with(density) { timeWidth.toPx() }
            
            Canvas(modifier = Modifier.matchParentSize()) {
                tags.forEach { tag ->
                    val colIndex = tag.hour - startHour
                    // Only draw if tag is within current view bounds
                    if (colIndex >= 0) {
                        val xPos = timeWidthPx + (colIndex * cellWidthPx)
                        
                        // 1. Red Line (Top to Bottom)
                        drawLine(
                            color = Color.Red,
                            start = Offset(xPos, 0f),
                            end = Offset(xPos, size.height),
                            strokeWidth = 2.dp.toPx()
                        )
                        
                        // 3. Name (Bottom)
                        val textResult = textMeasurer.measure(
                            text = androidx.compose.ui.text.AnnotatedString(tag.tagName),
                            style = androidx.compose.ui.text.TextStyle(color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        )
                        drawText(
                            textResult,
                            topLeft = Offset(xPos - (textResult.size.width / 2), size.height - 15.dp.toPx())
                        )
                    }
                }
            }
            
            // Overlay Icons separately to use Vector
            tags.forEach { tag ->
                val colIndex = tag.hour - startHour
                if (colIndex >= 0) {
                    val xOffsetPx = timeWidthPx + (colIndex * cellWidthPx)
                    val xOffsetDpCorrect = with(density) { xOffsetPx.toDp() }
                    
                    Box(modifier = Modifier.offset(x = xOffsetDpCorrect - 6.dp, y = 0.dp)) {
                         Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Tag",
                            tint = Color.Red,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}