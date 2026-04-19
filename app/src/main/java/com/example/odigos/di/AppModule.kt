package com.example.odigos.di

import android.content.Context
import androidx.room.Room
import com.example.odigos.data.local.AppDatabase
import com.example.odigos.data.local.dao.TimetableDao
import com.example.odigos.data.repository.TimetableRepositoryImpl
import com.example.odigos.domain.repository.ITimetableRepository
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
            "odigos_database"
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
