package com.cornmuffin.prototype.data.room

import android.content.Context
import androidx.room.Room.databaseBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ) = databaseBuilder(
        context = context,
        Database::class.java,
        Database.DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideProductDao(db: Database) = db.getProductDao()
}
