package com.example.mytt.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mytt.data.local.entity.TagEntity
import com.example.mytt.data.local.entity.TimetableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {

    // --- Timetable Operations ---

    @Query("SELECT * FROM timetable")
    fun getAllClasses(): Flow<List<TimetableEntity>>

    @Query("SELECT * FROM timetable WHERE id = :id")
    suspend fun getClassById(id: String): TimetableEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(entry: TimetableEntity)

    @Update
    suspend fun updateClass(entry: TimetableEntity)

    @Query("DELETE FROM timetable WHERE id = :id")
    suspend fun deleteClass(id: String)

    @Query("DELETE FROM timetable")
    suspend fun clearAllClasses()

    // --- Tag Operations ---

    @Query("SELECT * FROM tags")
    fun getAllTags(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteTag(id: String)
}
