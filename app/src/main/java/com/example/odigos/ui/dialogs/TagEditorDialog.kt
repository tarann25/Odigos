package com.example.odigos.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.odigos.domain.model.TagEntry

@Composable
fun TagEditorDialog(
    tags: List<TagEntry>,
    onDismiss: () -> Unit,
    onSave: (TagEntry) -> Unit,
    onDelete: (String) -> Unit
) {
    var tagName by remember { mutableStateOf("") }
    var hour by remember { mutableIntStateOf(12) } // Default 12PM

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier.width(300.dp).wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Add Tag", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    label = { Text("Tag Name") },
                    singleLine = true
                )
                
                Column {
                    Text("Time: ${if(hour<12) "$hour AM" else if(hour==12) "12 PM" else "${hour-12} PM"}", fontWeight = FontWeight.Bold)
                    Slider(
                        value = hour.toFloat(),
                        onValueChange = { hour = it.toInt() },
                        valueRange = 9f..16f,
                        steps = 6
                    )
                }
                
                if (tags.isNotEmpty()) {
                    HorizontalDivider()
                    Text("Existing Tags", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 150.dp).verticalScroll(rememberScrollState())
                    ) {
                        tags.forEach { tag ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(tag.tagName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    val h = tag.hour.toInt()
                                    Text("at ${if(h<12) "${h}am" else if(h==12) "12pm" else "${h-12}pm"}", fontSize = 10.sp, color = Color.Gray)
                                }
                                IconButton(onClick = { onDelete(tag.id) }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = { 
                        if (tagName.isNotBlank()) onSave(TagEntry(tagName = tagName, hour = hour.toFloat())) 
                    }) { Text("Save") }
                }
            }
        }
    }
}
