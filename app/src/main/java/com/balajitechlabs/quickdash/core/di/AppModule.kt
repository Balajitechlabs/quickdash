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
