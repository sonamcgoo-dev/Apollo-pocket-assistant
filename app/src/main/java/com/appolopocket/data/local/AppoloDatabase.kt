package com.appolopocket.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.appolopocket.data.local.dao.*
import com.appolopocket.data.local.entity.*

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        MemoryEntryEntity::class,
        TaskEntity::class,
        PromptTemplateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppoloDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun memoryDao(): MemoryDao
    abstract fun taskDao(): TaskDao
    abstract fun promptTemplateDao(): PromptTemplateDao
    
    companion object {
        const val DATABASE_NAME = "appolo_memory.db"
    }
}
