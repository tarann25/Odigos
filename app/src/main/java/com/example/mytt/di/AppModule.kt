package com.example.mytt.di

import android.content.Context
import androidx.room.Room
import com.example.mytt.data.local.AppDatabase
import com.example.mytt.data.local.dao.TimetableDao
import com.example.mytt.data.repository.TimetableRepositoryImpl
import com.example.mytt.domain.repository.ITimetableRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mytt_database"
        ).fallbackToDestructiveMigration() // Commercial apps should handle migration, but for dev speed now -> destructive
         .build()
    }

    @Provides
    @Singleton
    fun provideTimetableDao(database: AppDatabase): TimetableDao {
        return database.timetableDao()
    }

    @Provides
    @Singleton
    fun provideTimetableRepository(dao: TimetableDao): ITimetableRepository {
        return TimetableRepositoryImpl(dao)
    }
}
