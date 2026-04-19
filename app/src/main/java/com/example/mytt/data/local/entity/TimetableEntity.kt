package com.example.mytt.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetable")
data class TimetableEntity(
    @PrimaryKey val id: String,
    val subjectName: String,
    val roomCode: String,
    val dayOfWeek: Int,
    val startHour: Float,
    val duration: Float,
    val colorHex: Long
)

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: String,
    val tagName: String,
    val hour: Float
)
