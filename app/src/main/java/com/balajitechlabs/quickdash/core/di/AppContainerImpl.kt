package com.balajitechlabs.quickdash.core.di

import android.content.Context
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.core.data.database.AppDatabase
import com.balajitechlabs.quickdash.features.notes.data.NotesRepositoryImpl
import com.balajitechlabs.quickdash.features.notes.domain.repository.NotesRepository
import com.balajitechlabs.quickdash.features.settings.data.SettingsRepositoryImpl
import com.balajitechlabs.quickdash.features.settings.domain.repository.SettingsRepository

class AppContainerImpl(private val context: Context) : AppContainer {
    
    override val userStore: UserStore by lazy {
        UserStore(context)
    }
    
    override val notesRepository: NotesRepository by lazy {
        NotesRepositoryImpl(AppDatabase.getDatabase(context).noteDao())
    }
    
    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(userStore)
    }
}
