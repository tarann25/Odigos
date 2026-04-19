package com.example.odigos.domain.model

import java.util.UUID

data class TimetableEntry(
    val id: String = UUID.randomUUID().toString(),
    val subjectName: String,
    val roomCode: String,
    val dayOfWeek: Int,
    val startHour: Float,
    val duration: Float,
    val colorHex: Long
)

data class TagEntry(
    val id: String = UUID.randomUUID().toString(),
    val tagName: String,
    val hour: Float
)
