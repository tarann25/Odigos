package com.example.mytt.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mytt.TimetableWidgetHelper
import com.example.mytt.domain.model.TagEntry
import com.example.mytt.domain.model.TimetableEntry
import com.example.mytt.domain.repository.ITimetableRepository
import com.example.mytt.utils.NotificationHelper
import com.example.mytt.utils.NotificationScheduler
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.isActive
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.ai.client.generativeai.GenerativeModel
import com.example.mytt.BuildConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val repository: ITimetableRepository,
    private val application: Application
) : ViewModel() {

    // Using stateIn to convert the Repository Flow to a StateFlow for Compose
    val schedule: StateFlow<List<TimetableEntry>> = repository.getSchedule()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val tags: StateFlow<List<TagEntry>> = repository.getTags()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Undo History (InMemory for session)
    private val _lastDeletedEntry = MutableStateFlow<TimetableEntry?>(null)
    val canUndo: StateFlow<Boolean> = MutableStateFlow(false) // Simplified for this refactor

    val importPreviewState = MutableStateFlow<List<TimetableEntry>?>(null)

    // Color Palette
    private val colorPalette = listOf(
        0xFF5A643D, 0xFFB89C7A, 0xFF865F46, 0xFF8E653C, 0xFF3A261C,
        0xFF272A3C, 0xFF485F6D, 0xFF141416, 0xFF370C11, 0xFF451011
    )

    init {
        NotificationHelper.createNotificationChannel(application)
        
        // Observe schedule changes to update notifications and widget automatically
        viewModelScope.launch {
            schedule.collect { entries ->
                NotificationScheduler.scheduleNotifications(application, entries)
                TimetableWidgetHelper.updateAll(application)
            }
        }
    }

    fun addClass(entry: TimetableEntry) {
        viewModelScope.launch {
            repository.addClass(entry)
        }
    }

    fun updateClass(entry: TimetableEntry) {
        viewModelScope.launch {
            repository.updateClass(entry)
        }
    }

    fun deleteClass(id: String) {
        viewModelScope.launch {
            // Find entry to enable undo
            val entryToDelete = schedule.value.find { it.id == id }
            if (entryToDelete != null) {
                _lastDeletedEntry.value = entryToDelete
                // In a real app, expose canUndo via a derived state or a snackbar action
                // For now, we perform the delete
                repository.deleteClass(id)
            }
        }
    }

    // Simplified Undo: Restore last deleted
    fun undo() {
        val entry = _lastDeletedEntry.value
        if (entry != null) {
            viewModelScope.launch {
                repository.addClass(entry)
                _lastDeletedEntry.value = null
            }
        }
    }

    fun addTag(tag: TagEntry) {
        viewModelScope.launch {
            repository.addTag(tag)
        }
    }

    fun deleteTag(id: String) {
        viewModelScope.launch {
            repository.deleteTag(id)
        }
    }

    fun updateWidget() {
        TimetableWidgetHelper.updateAll(application)
    }

    fun sendTestNotification() {
        NotificationHelper.showNotification(application, "Test Subject", "Test Room")
    }

    fun importTimetableFromImage(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val image = InputImage.fromFilePath(application, uri)
                val rawText = suspendCancellableCoroutine<String> { cont ->
                    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    recognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            if (cont.isActive) cont.resume(visionText.text)
                        }
                        .addOnFailureListener { e ->
                            if (cont.isActive) cont.resumeWithException(e)
                        }
                }
                
                val generativeModel = GenerativeModel(
                    modelName = "gemini-1.5-pro",
                    apiKey = BuildConfig.GEMINI_API_KEY
                )
                
                val prompt = """
                    Extract the timetable from the following text and return it as a JSON array of objects.
                    Each object MUST have the following fields:
                    - subjectName (string)
                    - roomCode (string, use "" if not found)
                    - dayOfWeek (integer, 1 for Monday, 2 for Tuesday, ..., 7 for Sunday)
                    - startHour (float, e.g., 9.0 for 9 AM, 13.5 for 1:30 PM)
                    - duration (float, in hours, e.g., 1.5 for 1 hour 30 mins)
                    
                    Respond ONLY with the raw JSON array, no markdown formatting or backticks.
                    
                    Text:
                    ${rawText}
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val jsonResponse = response.text?.replace("```json", "")?.replace("```", "")?.trim() ?: "[]"
                
                val gson = Gson()
                val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
                val rawList: List<Map<String, Any>> = gson.fromJson(jsonResponse, listType)

                val subjectColorMap = mutableMapOf<String, Long>()
                var colorIndex = 0
                val batchList = mutableListOf<TimetableEntry>()

                for (item in rawList) {
                    val subject = (item["subjectName"] as? String) ?: "Unknown"
                    val room = (item["roomCode"] as? String) ?: ""
                    val day = (item["dayOfWeek"] as? Number)?.toInt() ?: 1
                    val start = (item["startHour"] as? Number)?.toFloat() ?: 9f
                    val dur = (item["duration"] as? Number)?.toFloat() ?: 1f

                    val color = subjectColorMap.getOrPut(subject) {
                        val c = colorPalette[colorIndex % colorPalette.size]
                        colorIndex++
                        c
                    }

                    batchList.add(
                        TimetableEntry(
                            id = UUID.randomUUID().toString(),
                            subjectName = subject,
                            roomCode = room,
                            dayOfWeek = day,
                            startHour = start,
                            duration = dur,
                            colorHex = color
                        )
                    )
                }

                importPreviewState.value = batchList
            } catch (e: Exception) {
                e.printStackTrace()
                // In a real app we would communicate error via state
            }
        }
    }

    fun confirmImport(list: List<TimetableEntry>) {
        viewModelScope.launch {
            repository.clearSchedule()
            repository.insertBatch(list)
            importPreviewState.value = null
        }
    }
    
    fun cancelImport() {
        importPreviewState.value = null
    }
}
