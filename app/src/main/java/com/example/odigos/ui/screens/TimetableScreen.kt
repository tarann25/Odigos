package com.example.odigos.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.odigos.TimetableWidgetReceiver
import com.example.odigos.domain.model.TimetableEntry
import com.example.odigos.ui.TimetableViewModel
import com.example.odigos.ui.components.ClassItem
import com.example.odigos.ui.components.TimetableGrid
import com.example.odigos.ui.dialogs.ClassEditorDialog
import com.example.odigos.ui.dialogs.TagEditorDialog
import com.example.odigos.ui.dialogs.ReviewImportDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    viewModel: TimetableViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    val schedule by viewModel.schedule.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val importPreviewState by viewModel.importPreviewState.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    
    var showClassDialog by remember { mutableStateOf(false) }
    var editingClassEntry by remember { mutableStateOf<TimetableEntry?>(null) }
    var showTagDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }

    var prefilledDay by remember { mutableIntStateOf(1) }
    var prefilledHour by remember { mutableIntStateOf(9) }

    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.sendTestNotification()
            Toast.makeText(context, "Notification Sent!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    fun refreshWidget() {
        viewModel.updateWidget()
        Toast.makeText(context, "Widget update requested", Toast.LENGTH_SHORT).show()
    }
    
    fun onImportSuccess() {
        isImporting = false
        showImportDialog = false
        Toast.makeText(context, "Schedule Imported Successfully!", Toast.LENGTH_LONG).show()
    }
    
    fun onImportError(msg: String) {
        isImporting = false
        Toast.makeText(context, "Import Failed: $msg", Toast.LENGTH_LONG).show()
    }

    fun requestPinWidget() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
            val myProvider = android.content.ComponentName(context, TimetableWidgetReceiver::class.java)
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                try {
                    appWidgetManager.requestPinAppWidget(myProvider, null, null)
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not pin widget: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Pinning widgets not supported on this device", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Pinning widgets requires Android 8.0+", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Odigos", color = Color.White, fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = { 
                            if (Build.VERSION.SDK_INT >= 33) {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                                    viewModel.sendTestNotification()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                viewModel.sendTestNotification()
                            }
                        }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Test Notification", tint = Color.Gray)
                        }
                        IconButton(onClick = { showImportDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Import JSON", tint = Color.Cyan) 
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
                )
            },
            containerColor = Color(0xFF121212),
            modifier = Modifier.blur(if (showClassDialog || showTagDialog) 10.dp else 0.dp)
        ) { padding ->
            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    TimetableGrid(
                        schedule = schedule,
                        tags = tags,
                        onClassClick = { entry ->
                            editingClassEntry = entry
                            prefilledDay = 0
                            prefilledHour = 0
                            showClassDialog = true
                        },
                        onEmptySlotLongClick = { day, hour ->
                            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
                                vibratorManager.defaultVibrator
                            } else {
                                @Suppress("DEPRECATION")
                                context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
                            }
                            vibrator.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))

                            editingClassEntry = null
                            prefilledDay = day
                            prefilledHour = hour
                            showClassDialog = true
                        },
                        onUpdateEntry = { updatedEntry ->
                            viewModel.updateClass(updatedEntry)
                        }
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Removed Undo if not strictly needed or handle logic differently
                        // Keeping tag button
                        Surface(
                            onClick = { showTagDialog = true },
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF2C2C2E),
                            border = BorderStroke(1.dp, Color.Gray),
                            modifier = Modifier.height(32.dp).width(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Tag", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                item {
                    Text(
                        "All Classes",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 16.dp)
                    )
                }

                items(schedule.sortedBy { it.dayOfWeek * 100 + it.startHour }) { entry ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ClassItem(
                            entry = entry,
                            onEdit = {
                                editingClassEntry = entry
                                prefilledDay = 0
                                prefilledHour = 0
                                showClassDialog = true
                            },
                            onDelete = {
                                viewModel.deleteClass(entry.id)
                            }
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .height(64.dp)
                .width(280.dp),
            shape = RoundedCornerShape(50),
            color = Color(0xFF2C2C2E).copy(alpha = 0.9f),
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, Color(0xFF3A3A3C))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { requestPinWidget() }) {
                     Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(30.dp)
                                .border(1.5.dp, Color.LightGray, RoundedCornerShape(4.dp))
                        )
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Pin Widget",
                            tint = Color.LightGray,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF42749D))
                        .clickable {
                            editingClassEntry = null
                            prefilledDay = 1
                            prefilledHour = 9
                            showClassDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Class",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                IconButton(onClick = { refreshWidget() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Update Widget",
                        tint = Color.LightGray
                    )
                }
            }
        }
    }

    if (showClassDialog) {
        ClassEditorDialog(
            entry = editingClassEntry,
            schedule = schedule, 
            prefilledDay = prefilledDay,
            prefilledHour = prefilledHour,
            onDismiss = { showClassDialog = false },
            onSave = { entry ->
                if (editingClassEntry == null) {
                    viewModel.addClass(entry)
                } else {
                    viewModel.updateClass(entry)
                }
                showClassDialog = false
            }
        )
    }
    
    if (showTagDialog) {
        TagEditorDialog(
            tags = tags,
            onDismiss = { showTagDialog = false },
            onSave = { tag ->
                viewModel.addTag(tag)
                showTagDialog = false
            },
            onDelete = { tagId ->
                viewModel.deleteTag(tagId)
            }
        )
    }

    androidx.compose.runtime.LaunchedEffect(importPreviewState) {
        if (importPreviewState != null) {
            isImporting = false
            showImportDialog = false
        }
    }

    if (showImportDialog) {
        com.example.odigos.ui.dialogs.ImportDialog(
            onDismiss = { showImportDialog = false },
            isLoading = isImporting,
            onImport = { uri ->
                isImporting = true
                viewModel.importTimetableFromImage(uri)
            }
        )
    }
    
    importPreviewState?.let { previewList ->
        ReviewImportDialog(
            entries = previewList,
            onConfirm = { list ->
                viewModel.confirmImport(list)
                Toast.makeText(context, "Schedule Imported Successfully!", Toast.LENGTH_LONG).show()
            },
            onDismiss = {
                viewModel.cancelImport()
            }
        )
    }
}
