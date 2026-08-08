package com.appolopocket.domain.repository

import com.appolopocket.domain.model.*
import com.appolopocket.data.remote.llm.LLMResponse
import com.appolopocket.data.remote.llm.OllamaModel
import com.appolopocket.data.remote.llm.ModelPullProgress
import com.appolopocket.data.remote.llm.OllamaTool
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun getAllConversations(): Flow<List<Conversation>>
    suspend fun getConversationById(id: String): Conversation?
    suspend fun createConversation(title: String): Conversation
    suspend fun updateConversation(conversation: Conversation)
    suspend fun deleteConversation(id: String)
    suspend fun addMessage(conversationId: String, message: Message)
    suspend fun getMessages(conversationId: String): List<Message>
    fun getMessagesFlow(conversationId: String): Flow<List<Message>>
}

interface MemoryRepository {
    fun getAllMemories(): Flow<List<MemoryEntry>>
    fun getMemoriesByCategory(category: MemoryCategory): Flow<List<MemoryEntry>>
    fun searchMemories(query: String): Flow<List<MemoryEntry>>
    suspend fun getMemoryByKey(key: String): MemoryEntry?
    suspend fun saveMemory(memory: MemoryEntry)
    suspend fun deleteMemory(id: String)
    suspend fun incrementAccessCount(id: String)
}

interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    fun getTasksByStatus(status: TaskStatus): Flow<List<Task>>
    suspend fun getTaskById(id: String): Task?
    suspend fun createTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(id: String)
}

interface PreferencesRepository {
    val userPreferences: Flow<UserPreferences>
    val controlPrompts: Flow<ControlPrompts>
    val currentConversationId: Flow<String?>
    val isFirstLaunch: Flow<Boolean>
    
    suspend fun updateLLMConfig(config: LLMConfig)
    suspend fun updateApprovalMode(mode: ApprovalMode)
    suspend fun updateTheme(theme: AppTheme)
    suspend fun updateVoiceEnabled(enabled: Boolean)
    suspend fun updateNotificationsEnabled(enabled: Boolean)
    suspend fun updateAutoMemory(enabled: Boolean)
    suspend fun updateSystemPrompt(prompt: String)
    suspend fun updateAgentPrompt(prompt: String)
    suspend fun updatePersonalityPrompt(prompt: String)
    suspend fun updateFunctionsScope(scope: FunctionsScope)
    suspend fun setCurrentConversationId(id: String?)
    suspend fun setFirstLaunchComplete()
    suspend fun resetToDefaults()
}

interface LLMRepository {
    suspend fun chat(messages: List<Message>, tools: List<OllamaTool> = emptyList()): Flow<LLMResponse>
    suspend fun generate(prompt: String, systemPrompt: String? = null): Flow<LLMResponse>
    suspend fun listModels(): Result<List<OllamaModel>>
    suspend fun pullModel(modelName: String): Flow<ModelPullProgress>
    suspend fun checkConnection(): Boolean
    fun updateConfig(config: LLMConfig)
    fun getCurrentConfig(): LLMConfig
}
