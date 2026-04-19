package com.example.mytt.data.repository

import com.example.mytt.data.local.dao.TimetableDao
import com.example.mytt.data.local.entity.TagEntity
import com.example.mytt.data.local.entity.TimetableEntity
import com.example.mytt.domain.model.TagEntry
import com.example.mytt.domain.model.TimetableEntry
import com.example.mytt.domain.repository.ITimetableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TimetableRepositoryImpl @Inject constructor(
    private val dao: TimetableDao
) : ITimetableRepository {

    override fun getSchedule(): Flow<List<TimetableEntry>> {
        return dao.getAllClasses().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTags(): Flow<List<TagEntry>> {
        return dao.getAllTags().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addClass(entry: TimetableEntry) {
        dao.insertClass(entry.toEntity())
    }

    override suspend fun updateClass(entry: TimetableEntry) {
        dao.updateClass(entry.toEntity())
    }

    override suspend fun deleteClass(id: String) {
        dao.deleteClass(id)
    }

    override suspend fun addTag(tag: TagEntry) {
        dao.insertTag(tag.toEntity())
    }

    override suspend fun deleteTag(id: String) {
        dao.deleteTag(id)
    }
    
    override suspend fun clearSchedule() {
        dao.clearAllClasses()
    }
    
    override suspend fun insertBatch(entries: List<TimetableEntry>) {
        // Room doesn't have insertAll for list directly in basic impl unless defined,
        // but we can loop or add insertAll to DAO. 
        // For efficiency, let's add insertAll to DAO later or just loop here.
        // Looping is fine for small timetables.
        entries.forEach { dao.insertClass(it.toEntity()) }
    }

    // --- Mappers ---

    private fun TimetableEntity.toDomain() = TimetableEntry(
        id = id,
        subjectName = subjectName,
        roomCode = roomCode,
        dayOfWeek = dayOfWeek,
        startHour = startHour,
        duration = duration,
        colorHex = colorHex
    )

    private fun TimetableEntry.toEntity() = TimetableEntity(
        id = id,
        subjectName = subjectName,
        roomCode = roomCode,
        dayOfWeek = dayOfWeek,
        startHour = startHour,
        duration = duration,
        colorHex = colorHex
    )

    private fun TagEntity.toDomain() = TagEntry(
        id = id,
        tagName = tagName,
        hour = hour
    )

    private fun TagEntry.toEntity() = TagEntity(
        id = id,
        tagName = tagName,
        hour = hour
    )
}
