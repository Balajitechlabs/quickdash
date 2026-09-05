/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/di
 * File: AppModule.kt
 * Description: Hilt dependency injection module binding application context, repositories, database, and client instances.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.di

import android.content.Context
import com.balajitechlabs.quickdash.core.data.UserStore
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
    fun provideUserStore(@ApplicationContext context: Context): UserStore {
        return UserStore(context)
    }
}
