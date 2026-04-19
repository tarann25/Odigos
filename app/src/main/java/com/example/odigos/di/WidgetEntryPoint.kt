package com.example.odigos.di

import com.example.odigos.domain.repository.ITimetableRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun getTimetableRepository(): ITimetableRepository
}
