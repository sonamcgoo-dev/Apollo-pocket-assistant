package com.appolopocket.data.repository

import com.appolopocket.data.local.dao.*
import com.appolopocket.data.local.datastore.PreferencesManager
import com.appolopocket.data.local.entity.*
import com.appolopocket.data.remote.llm.*
import com.appolopocket.domain.model.*
import com.appolopocket.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepositoryImpl @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) : ConversationRepository {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    override fun getAllConversations(): Flow<List<Conversation>> {
        return conversationDao.getAllConversations().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getConversationById(id: String): Conversation? {
        val entity = conversationDao.getConversationById(id) ?: return null
        val messages = messageDao.getMessagesForConversationSync(id)
        return entity.toDomain(messages.map { it.toDomain() })
    }
    
    override suspend fun createConversation(title: String): Conversation {
        val now = System.currentTimeMillis()
        val conversation = Conversation(
            id = UUID.randomUUID().toString(),
            title = title,
            createdAt = now,
            updatedAt = now
        )
        conversationDao.insertConversation(conversation.toEntity())
        return conversation
    }
    
    override suspend fun updateConversation(conversation: Conversation) {
        conversationDao.updateConversation(conversation.toEntity())
    }
    
    override suspend fun deleteConversation(id: String) {
        conversationDao.deleteConversationById(id)
    }
    
    override suspend fun addMessage(conversationId: String, message: Message) {
        messageDao.insertMessage(message.toEntity(conversationId))
        // Update conversation timestamp
        conversationDao.getConversationById(conversationId)?.let { conv ->
            conversationDao.updateConversation(conv.copy(updatedAt = System.currentTimeMillis()))
        }
    }
    
    override suspend fun getMessages(conversationId: String): List<Message> {
        return messageDao.getMessagesForConversationSync(conversationId).map { it.toDomain() }
    }
    
    override fun getMessagesFlow(conversationId: String): Flow<List<Message>> {
        return messageDao.getMessagesForConversation(conversationId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    private fun ConversationEntity.toDomain(messages: List<Message> = emptyList()) = Conversation(
        id = id,
        title = title,
        messages = messages,
        createdAt = createdAt,
        updatedAt = updatedAt,
        metadata = ConversationMetadata(
            model = model,
            totalTokens = totalTokens,
            messageCount = messageCount
        )
    )
    
    private fun Conversation.toEntity() = ConversationEntity(
        id = id,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt,
        model = metadata.model,
        totalTokens = metadata.totalTokens,
        messageCount = metadata.messageCount
    )
    
    private fun MessageEntity.toDomain(): Message {
        val toolCalls = toolCallsJson?.let {
            try {
                json.decodeFromString<List<ToolCall>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()
        
        val toolResults = toolResultsJson?.let {
            try {
                json.decodeFromString<List<ToolResult>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()
        
        return Message(
            id = id,
            role = MessageRole.valueOf(role.uppercase()),
            content = content,
            timestamp = timestamp,
            toolCalls = toolCalls,
            toolResults = toolResults,
            metadata = if (model != null || tokensUsed != null || executionTime != null) {
                MessageMetadata(model = model, tokensUsed = tokensUsed, executionTime = executionTime)
            } else null
        )
    }
    
    private fun Message.toEntity(conversationId: String) = MessageEntity(
        id = id,
        conversationId = conversationId,
        role = role.name.lowercase(),
        content = content,
        timestamp = timestamp,
        toolCallsJson = if (toolCalls.isNotEmpty()) json.encodeToString(toolCalls) else null,
        toolResultsJson = if (toolResults.isNotEmpty()) json.encodeToString(toolResults) else null,
        model = metadata?.model,
        tokensUsed = metadata?.tokensUsed,
        executionTime = metadata?.executionTime
    )
}

@Singleton
class MemoryRepositoryImpl @Inject constructor(
    private val memoryDao: MemoryDao
) : MemoryRepository {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    override fun getAllMemories(): Flow<List<MemoryEntry>> {
        return memoryDao.getAllMemories().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getMemoriesByCategory(category: MemoryCategory): Flow<List<MemoryEntry>> {
        return memoryDao.getMemoriesByCategory(category.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun searchMemories(query: String): Flow<List<MemoryEntry>> {
        return memoryDao.searchMemories(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getMemoryByKey(key: String): MemoryEntry? {
        return memoryDao.getMemoryByKey(key)?.toDomain()
    }
    
    override suspend fun saveMemory(memory: MemoryEntry) {
        memoryDao.insertMemory(memory.toEntity())
    }
    
    override suspend fun deleteMemory(id: String) {
        memoryDao.deleteMemoryById(id)
    }
    
    override suspend fun incrementAccessCount(id: String) {
        memoryDao.incrementAccessCount(id)
    }
    
    private fun MemoryEntryEntity.toDomain() = MemoryEntry(
        id = id,
        key = key,
        value = value,
        category = MemoryCategory.valueOf(category.uppercase()),
        createdAt = createdAt,
        updatedAt = updatedAt,
        accessCount = accessCount,
        tags = try {
            json.decodeFromString<List<String>>(tagsJson)
        } catch (e: Exception) {
            emptyList()
        }
    )
    
    private fun MemoryEntry.toEntity() = MemoryEntryEntity(
        id = id,
        key = key,
        value = value,
        category = category.name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        accessCount = accessCount,
        tagsJson = json.encodeToString(tags)
    )
}

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getTasksByStatus(status: TaskStatus): Flow<List<Task>> {
        return taskDao.getTasksByStatus(status.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getTaskById(id: String): Task? {
        return taskDao.getTaskById(id)?.toDomain()
    }
    
    override suspend fun createTask(task: Task) {
        taskDao.insertTask(task.toEntity())
    }
    
    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.toEntity())
    }
    
    override suspend fun deleteTask(id: String) {
        taskDao.deleteTaskById(id)
    }
    
    private fun TaskEntity.toDomain() = Task(
        id = id,
        title = title,
        description = description,
        status = TaskStatus.valueOf(status.uppercase()),
        createdAt = createdAt,
        completedAt = completedAt,
        steps = try {
            json.decodeFromString<List<TaskStep>>(stepsJson)
        } catch (e: Exception) {
            emptyList()
        },
        currentStepIndex = currentStepIndex,
        metadata = try {
            json.decodeFromString<Map<String, String>>(metadataJson)
        } catch (e: Exception) {
            emptyMap()
        }
    )
    
    private fun Task.toEntity() = TaskEntity(
        id = id,
        title = title,
        description = description,
        status = status.name,
        createdAt = createdAt,
        completedAt = completedAt,
        stepsJson = json.encodeToString(steps),
        currentStepIndex = currentStepIndex,
        metadataJson = json.encodeToString(metadata)
    )
}

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager
) : PreferencesRepository {
    
    override val userPreferences: Flow<UserPreferences> = preferencesManager.userPreferences
    override val controlPrompts: Flow<ControlPrompts> = preferencesManager.controlPrompts
    override val currentConversationId: Flow<String?> = preferencesManager.currentConversationId
    override val isFirstLaunch: Flow<Boolean> = preferencesManager.isFirstLaunch
    
    override suspend fun updateLLMConfig(config: LLMConfig) = preferencesManager.updateLLMConfig(config)
    override suspend fun updateApprovalMode(mode: ApprovalMode) = preferencesManager.updateApprovalMode(mode)
    override suspend fun updateTheme(theme: AppTheme) = preferencesManager.updateTheme(theme)
    override suspend fun updateVoiceEnabled(enabled: Boolean) = preferencesManager.updateVoiceEnabled(enabled)
    override suspend fun updateNotificationsEnabled(enabled: Boolean) = preferencesManager.updateNotificationsEnabled(enabled)
    override suspend fun updateAutoMemory(enabled: Boolean) = preferencesManager.updateAutoMemory(enabled)
    override suspend fun updateAsciiAnimations(enabled: Boolean) = preferencesManager.updateAsciiAnimations(enabled)
    override suspend fun updateSystemPrompt(prompt: String) = preferencesManager.updateSystemPrompt(prompt)
    override suspend fun updateAgentPrompt(prompt: String) = preferencesManager.updateAgentPrompt(prompt)
    override suspend fun updatePersonalityPrompt(prompt: String) = preferencesManager.updatePersonalityPrompt(prompt)
    override suspend fun updateFunctionsScope(scope: FunctionsScope) = preferencesManager.updateFunctionsScope(scope)
    override suspend fun setCurrentConversationId(id: String?) = preferencesManager.setCurrentConversationId(id)
    override suspend fun setFirstLaunchComplete() = preferencesManager.setFirstLaunchComplete()
    override suspend fun resetToDefaults() = preferencesManager.resetToDefaults()
}

@Singleton
class LLMRepositoryImpl @Inject constructor(
    private val ollamaClient: OllamaClient
) : LLMRepository {
    
    override suspend fun chat(messages: List<Message>, tools: List<OllamaTool>): Flow<LLMResponse> {
        return ollamaClient.chat(messages, tools)
    }
    
    override suspend fun generate(prompt: String, systemPrompt: String?): Flow<LLMResponse> {
        return ollamaClient.generate(prompt, systemPrompt)
    }
    
    override suspend fun listModels(): Result<List<OllamaModel>> {
        return ollamaClient.listModels()
    }
    
    override suspend fun pullModel(modelName: String): Flow<ModelPullProgress> {
        return ollamaClient.pullModel(modelName)
    }
    
    override suspend fun checkConnection(): Boolean {
        return ollamaClient.checkConnection()
    }
    
    override fun updateConfig(config: LLMConfig) {
        ollamaClient.updateConfig(config)
    }
    
    override fun getCurrentConfig(): LLMConfig {
        return ollamaClient.getCurrentConfig()
    }
}
