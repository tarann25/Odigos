package com.example.odigos.domain.repository

import com.example.odigos.domain.model.TagEntry
import com.example.odigos.domain.model.TimetableEntry
import kotlinx.coroutines.flow.Flow

interface ITimetableRepository {
    fun getSchedule(): Flow<List<TimetableEntry>>
    fun getTags(): Flow<List<TagEntry>>
    suspend fun addClass(entry: TimetableEntry)
    suspend fun updateClass(entry: TimetableEntry)
    suspend fun deleteClass(id: String)
    suspend fun addTag(tag: TagEntry)
    suspend fun deleteTag(id: String)
    suspend fun clearSchedule()
    suspend fun insertBatch(entries: List<TimetableEntry>)
}
