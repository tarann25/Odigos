package com.example.odigos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.odigos.data.local.dao.TimetableDao
import com.example.odigos.data.local.entity.TagEntity
import com.example.odigos.data.local.entity.TimetableEntity

@Database(
    entities = [TimetableEntity::class, TagEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timetableDao(): TimetableDao
}
