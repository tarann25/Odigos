package com.example.odigos.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.odigos.domain.model.TimetableEntry

@Composable
fun ClassItem(
    entry: TimetableEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val startHourInt = entry.startHour.toInt()
    val startTime = if (startHourInt < 12) "${startHourInt}am" else if (startHourInt == 12) "12pm" else "${startHourInt - 12}pm"
    val endHour = (entry.startHour + entry.duration).toInt()
    val endTime = if (endHour < 12) "${endHour}am" else if (endHour == 12) "12pm" else "${endHour - 12}pm"

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(entry.colorHex)), // Full color background
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp), // Increased padding for cleaner look
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.subjectName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    "${dayNames[entry.dayOfWeek - 1]} • $startTime - $endTime • ${entry.roomCode}", 
                    fontSize = 12.sp, 
                    color = Color.White.copy(alpha = 0.8f) // Slightly transparent white for readability
                )
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit", tint = Color.White) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete", tint = Color.White.copy(alpha = 0.8f)) }
        }
    }
}