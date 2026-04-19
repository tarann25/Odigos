package com.example.odigos.ui.dialogs

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.odigos.domain.model.TimetableEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassEditorDialog(
    entry: TimetableEntry?,
    schedule: List<TimetableEntry>,
    prefilledDay: Int,
    prefilledHour: Int,
    onDismiss: () -> Unit,
    onSave: (TimetableEntry) -> Unit
) {
    val context = LocalContext.current

    // ... (State variables)
    var subjectName by remember(entry) { mutableStateOf(entry?.subjectName ?: "") }
    var roomCode by remember(entry) { mutableStateOf(entry?.roomCode ?: "") }

    var dayOfWeek by remember(entry, prefilledDay) {
        mutableIntStateOf(entry?.dayOfWeek ?: if (prefilledDay > 0) prefilledDay else 1)
    }

    var startHour by remember(entry, prefilledHour) {
        mutableFloatStateOf(entry?.startHour ?: if (prefilledHour in 9..16) prefilledHour.toFloat() else 9f)
    }

    var duration by remember(entry) { mutableFloatStateOf(entry?.duration ?: 1f) }
    // Default color logic: Use entry color OR Dark Cyan (0xFF0a9396)
    var selectedColor by remember(entry) {
        mutableLongStateOf(entry?.colorHex ?: 0xFF0a9396)
    } ; var showColorPicker by remember { mutableStateOf(false) }

    // Suggestion State
    var suggestionsExpanded by remember { mutableStateOf(false) }
    val uniqueSubjects = remember(schedule) {
        schedule.map { it.subjectName to it.colorHex }
            .distinctBy { it.first }
            .filter { it.first.isNotBlank() }
    }
    val filteredSubjects = if (subjectName.isBlank()) uniqueSubjects else uniqueSubjects.filter {
        it.first.contains(subjectName, ignoreCase = true)
    }

    val presetColors = listOf(
        0xFF5A643D, // Olive
        0xFFB89C7A, // Beige
        0xFF865F46, // Brown
        0xFF8E653C, // Tan
        0xFF3A261C, // Dark Brown
        0xFF272A3C, // Dark Blue-Grey
        0xFF485F6D, // Slate Blue
        0xFF141416, // Black/Charcoal
        0xFF370C11, // Dark Red
        0xFF451011  // Red-Brown
    )

    fun vibrateTick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
            vibratorManager.defaultVibrator.vibrate(android.os.VibrationEffect.createPredefined(android.os.VibrationEffect.EFFECT_TICK))
        } else {
            @Suppress("DEPRECATION")
            val v = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
            v.vibrate(10)
        }
    }

    // Custom Dialog with 0 dim amount
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false // Allow custom width
        )
    ) {
        // Mimic AlertDialog styling
        Surface(
            modifier = Modifier
                .width(312.dp) // Standard Dialog width
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp) // Main layout spacing
            ) {
                // HEADER (Keep existing)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (entry == null) "Add Class" else "Edit Class",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                                         // Live Preview Card
                                         Card(
                                             colors = CardDefaults.cardColors(containerColor = Color(selectedColor)),
                                             shape = RoundedCornerShape(8.dp),
                                             modifier = Modifier.size(width = 80.dp, height = 50.dp) // Increased size slightly
                                         ) {
                                             Column(
                                                 modifier = Modifier.fillMaxSize().padding(4.dp),
                                                 horizontalAlignment = Alignment.CenterHorizontally,
                                                 verticalArrangement = Arrangement.Center
                                             ) {
                                                 if (subjectName.isNotEmpty() || roomCode.isNotEmpty()) {
                                                     Text(
                                                         text = subjectName.take(4),
                                                         fontSize = 11.sp,
                                                         fontWeight = FontWeight.Bold,
                                                         color = Color.White,
                                                         maxLines = 1
                                                     )
                                                     if (roomCode.isNotEmpty()) {
                                                         Text(
                                                             text = roomCode.take(4),
                                                             fontSize = 10.sp, // Increased from 8.sp
                                                             fontWeight = FontWeight.SemiBold, // Bolder
                                                             color = Color.White.copy(alpha = 0.95f), // Higher opacity
                                                             maxLines = 1
                                                         )
                                                     }
                                                 }
                                             }
                                         }                }

                // CONTENT
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(weight = 1f, fill = false) // Allow scrolling if needed, but hug content otherwise
                        .verticalScroll(rememberScrollState())
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Inputs: Subject & Room
                    // Wrap Subject in Box for Dropdown
                    Box {
                        OutlinedTextField(
                            value = subjectName,
                            onValueChange = {
                                subjectName = it
                                suggestionsExpanded = true
                            },
                            label = { Text("Subject", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused && filteredSubjects.isNotEmpty()) {
                                        suggestionsExpanded = true
                                    }
                                },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                            singleLine = true
                        )

                        // Suggestions Dropdown
                        DropdownMenu(
                            expanded = suggestionsExpanded && filteredSubjects.isNotEmpty(),
                            onDismissRequest = { suggestionsExpanded = false },
                            properties = androidx.compose.ui.window.PopupProperties(focusable = false)
                        ) {
                            filteredSubjects.take(5).forEach { (name, color) ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(color)))
                                            Spacer(Modifier.width(8.dp))
                                            Text(name, fontSize = 14.sp)
                                        }
                                    },
                                    onClick = {
                                        subjectName = name
                                        selectedColor = color
                                        suggestionsExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = roomCode,
                        onValueChange = { roomCode = it },
                        label = { Text("Room Code", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                        singleLine = true
                    )

                    // Day Selector (Pills)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Day", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT")
                            days.forEachIndexed { index, name ->
                                val isSelected = dayOfWeek == index + 1
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(if (isSelected) Color(0xFF6B9B6B) else Color(0xFF2C2C2E))
                                        .clickable { dayOfWeek = index + 1 },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        name,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    // Time & Duration Sliders
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text("Start: " + if (startHour.toInt() < 12) "${startHour.toInt()}am" else if (startHour.toInt() == 12) "12pm" else "${startHour.toInt() - 12}pm", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            Slider(
                                value = startHour,
                                onValueChange = {
                                    val newFloat = it
                                    if (newFloat != startHour) {
                                        startHour = newFloat
                                        vibrateTick()
                                    }
                                },
                                valueRange = 9f..16f,
                                steps = 6
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Duration: ${duration.toInt()}h", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            Slider(
                                value = duration,
                                onValueChange = {
                                    val newFloat = it
                                    if (newFloat != duration) {
                                        duration = newFloat
                                        vibrateTick()
                                    }
                                },
                                valueRange = 1f..4f,
                                steps = 2
                            )
                        }
                    }

                    // Color Selection
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Preset Colors
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            presetColors.forEach { color ->
                                val isSelected = selectedColor == color
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(color))
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColor = color }
                                )
                            }
                        }

                        // Custom Color Section
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.End
                        ) {
                            // We need to hoist state to share with Preview
                            var isDragging by remember { mutableStateOf(false) }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Preview Circle
                                val isCustom = presetColors.none { it == selectedColor }
                                AnimatedVisibility(
                                    visible = isCustom && !isDragging,
                                    enter = scaleIn(),
                                    exit = scaleOut()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(selectedColor))
                                            .border(1.dp, Color.White, CircleShape)
                                    )
                                }

                                // "Other" Button
                                Box(
                                    modifier = Modifier
                                        .height(28.dp)
                                        .width(70.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(Color.Red, Color.Blue)
                                            )
                                        )
                                        .clickable { showColorPicker = !showColorPicker },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Other", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }

                            // Expandable Color Picker
                            AnimatedVisibility(
                                visible = showColorPicker,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .background(Color(0xFF2C2C2E), RoundedCornerShape(12.dp))
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Rainbow Strip
                                    var rowWidth by remember { mutableIntStateOf(0) }
                                    var dragOffset by remember { mutableFloatStateOf(0f) }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    colors = listOf(
                                                        Color.Red, Color(0xFFFF7F00), Color.Yellow, Color.Green,
                                                        Color.Blue, Color(0xFF4B0082), Color(0xFF8B00FF)
                                                    )
                                                )
                                            )
                                            .onGloballyPositioned { coordinates -> rowWidth = coordinates.size.width }
                                            .pointerInput(Unit) {
                                                detectTapGestures(
                                                    onPress = { offset ->
                                                        isDragging = true
                                                        dragOffset = offset.x.coerceIn(0f, rowWidth.toFloat())
                                                        val hue = (dragOffset / rowWidth.toFloat()) * 360f
                                                        val newColor = android.graphics.Color.HSVToColor(floatArrayOf(hue.coerceIn(0f, 360f), 1f, 1f))
                                                        selectedColor = newColor.toLong()
                                                        tryAwaitRelease()
                                                        isDragging = false
                                                    },
                                                    onTap = { offset ->
                                                        dragOffset = offset.x.coerceIn(0f, rowWidth.toFloat())
                                                        val hue = (dragOffset / rowWidth.toFloat()) * 360f
                                                        val newColor = android.graphics.Color.HSVToColor(floatArrayOf(hue.coerceIn(0f, 360f), 1f, 1f))
                                                        selectedColor = newColor.toLong()
                                                    }
                                                )
                                            }
                                            .pointerInput(Unit) {
                                                detectDragGestures(
                                                    onDragStart = { isDragging = true },
                                                    onDragEnd = { isDragging = false },
                                                    onDragCancel = { isDragging = false }
                                                ) { change, _ ->
                                                    change.consume()
                                                    dragOffset = change.position.x.coerceIn(0f, rowWidth.toFloat())
                                                    val hue = (dragOffset / rowWidth.toFloat()) * 360f
                                                    val newColor = android.graphics.Color.HSVToColor(floatArrayOf(hue.coerceIn(0f, 360f), 1f, 1f))
                                                    selectedColor = newColor.toLong()
                                                }
                                            }
                                    ) {
                                        // Pin
                                        androidx.compose.animation.AnimatedVisibility(
                                            visible = isDragging,
                                            enter = fadeIn() + scaleIn(),
                                            exit = fadeOut() + scaleOut()
                                        ) {
                                             Box(
                                                 modifier = Modifier
                                                     .offset(x = with(LocalDensity.current) { dragOffset.toDp() - 14.dp })
                                                     .size(28.dp)
                                                     .border(2.dp, Color.White, CircleShape)
                                                     .clip(CircleShape)
                                                     .background(Color(selectedColor))
                                                     .shadow(4.dp, CircleShape)
                                             )
                                        }
                                    }

                                    // Hex Code Input
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Hex Code", color = Color.Gray, fontSize = 11.sp)
                                        Row(
                                            modifier = Modifier
                                                .background(Color.Black, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("#", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            var textState by remember(selectedColor) {
                                                mutableStateOf(String.format("%06X", (0xFFFFFF and selectedColor.toInt())))
                                            }
                                            androidx.compose.foundation.text.BasicTextField(
                                                value = textState,
                                                onValueChange = { newValue ->
                                                    if (newValue.length <= 6 && newValue.all { it.isDigit() || it in 'A'..'F' || it in 'a'..'f' }) {
                                                        textState = newValue.uppercase()
                                                        if (newValue.length == 6) {
                                                            try {
                                                                val parsedColor = android.graphics.Color.parseColor("#$newValue")
                                                                selectedColor = parsedColor.toLong()
                                                            } catch (e: Exception) {}
                                                        }
                                                    }
                                                },
                                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                                singleLine = true,
                                                modifier = Modifier.width(50.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ACTIONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = { if (subjectName.isNotBlank()) onSave(TimetableEntry(id = entry?.id ?: java.util.UUID.randomUUID().toString(), subjectName = subjectName, roomCode = roomCode, dayOfWeek = dayOfWeek, startHour = startHour, duration = duration, colorHex = selectedColor)) }) { Text("Save") }
                }
            }
        }
    }
}