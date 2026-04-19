package com.example.mytt.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mytt.data.local.dao.TimetableDao
import com.example.mytt.data.local.entity.TagEntity
import com.example.mytt.data.local.entity.TimetableEntity

@Database(
    entities = [TimetableEntity::class, TagEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timetableDao(): TimetableDao
}
