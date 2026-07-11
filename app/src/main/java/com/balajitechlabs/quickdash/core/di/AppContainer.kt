package com.balajitechlabs.quickdash.core.di

import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.features.notes.domain.repository.NotesRepository
import com.balajitechlabs.quickdash.features.settings.domain.repository.SettingsRepository

interface AppContainer {
    val userStore: UserStore
    val notesRepository: NotesRepository
    val settingsRepository: SettingsRepository
}
