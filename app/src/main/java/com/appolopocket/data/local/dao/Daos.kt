package com.appolopocket.data.local.dao

import androidx.room.*
import com.appolopocket.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>
    
    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversationById(id: String): ConversationEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)
    
    @Update
    suspend fun updateConversation(conversation: ConversationEntity)
    
    @Delete
    suspend fun deleteConversation(conversation: ConversationEntity)
    
    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversationById(id: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesForConversationSync(conversationId: String): List<MessageEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)
    
    @Update
    suspend fun updateMessage(message: MessageEntity)
    
    @Delete
    suspend fun deleteMessage(message: MessageEntity)
    
    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)
    
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(conversationId: String, limit: Int): List<MessageEntity>
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memory_entries ORDER BY updatedAt DESC")
    fun getAllMemories(): Flow<List<MemoryEntryEntity>>
    
    @Query("SELECT * FROM memory_entries WHERE category = :category ORDER BY updatedAt DESC")
    fun getMemoriesByCategory(category: String): Flow<List<MemoryEntryEntity>>
    
    @Query("SELECT * FROM memory_entries WHERE key = :key LIMIT 1")
    suspend fun getMemoryByKey(key: String): MemoryEntryEntity?
    
    @Query("SELECT * FROM memory_entries WHERE key LIKE '%' || :query || '%' OR value LIKE '%' || :query || '%'")
    fun searchMemories(query: String): Flow<List<MemoryEntryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntryEntity)
    
    @Update
    suspend fun updateMemory(memory: MemoryEntryEntity)
    
    @Delete
    suspend fun deleteMemory(memory: MemoryEntryEntity)
    
    @Query("DELETE FROM memory_entries WHERE id = :id")
    suspend fun deleteMemoryById(id: String)
    
    @Query("UPDATE memory_entries SET accessCount = accessCount + 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun incrementAccessCount(id: String, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY createdAt DESC")
    fun getTasksByStatus(status: String): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): TaskEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)
    
    @Update
    suspend fun updateTask(task: TaskEntity)
    
    @Delete
    suspend fun deleteTask(task: TaskEntity)
    
    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)
}

@Dao
interface PromptTemplateDao {
    @Query("SELECT * FROM prompt_templates WHERE type = :type")
    suspend fun getPromptByType(type: String): PromptTemplateEntity?
    
    @Query("SELECT * FROM prompt_templates")
    fun getAllPrompts(): Flow<List<PromptTemplateEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: PromptTemplateEntity)
    
    @Update
    suspend fun updatePrompt(prompt: PromptTemplateEntity)
}
