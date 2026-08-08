package com.appolopocket.di

import android.content.Context
import androidx.room.Room
import com.appolopocket.data.local.AppoloDatabase
import com.appolopocket.data.local.dao.*
import com.appolopocket.data.local.datastore.PreferencesManager
import com.appolopocket.data.remote.llm.OllamaClient
import com.appolopocket.data.repository.*
import com.appolopocket.domain.repository.*
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
    fun provideAppoloDatabase(
        @ApplicationContext context: Context
    ): AppoloDatabase {
        return Room.databaseBuilder(
            context,
            AppoloDatabase::class.java,
            AppoloDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideConversationDao(database: AppoloDatabase): ConversationDao {
        return database.conversationDao()
    }

    @Provides
    @Singleton
    fun provideMessageDao(database: AppoloDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    @Singleton
    fun provideMemoryDao(database: AppoloDatabase): MemoryDao {
        return database.memoryDao()
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: AppoloDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun providePromptTemplateDao(database: AppoloDatabase): PromptTemplateDao {
        return database.promptTemplateDao()
    }

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideOllamaClient(): OllamaClient {
        return OllamaClient()
    }

    @Provides
    @Singleton
    fun provideConversationRepository(
        conversationDao: ConversationDao,
        messageDao: MessageDao
    ): ConversationRepository {
        return ConversationRepositoryImpl(conversationDao, messageDao)
    }

    @Provides
    @Singleton
    fun provideMemoryRepository(
        memoryDao: MemoryDao
    ): MemoryRepository {
        return MemoryRepositoryImpl(memoryDao)
    }

    @Provides
    @Singleton
    fun provideTaskRepository(
        taskDao: TaskDao
    ): TaskRepository {
        return TaskRepositoryImpl(taskDao)
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(
        preferencesManager: PreferencesManager
    ): PreferencesRepository {
        return PreferencesRepositoryImpl(preferencesManager)
    }

    @Provides
    @Singleton
    fun provideLLMRepository(
        ollamaClient: OllamaClient
    ): LLMRepository {
        return LLMRepositoryImpl(ollamaClient)
    }
}
