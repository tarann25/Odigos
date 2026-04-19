package com.example.mytt.di

import com.example.mytt.domain.repository.ITimetableRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun getTimetableRepository(): ITimetableRepository
}
