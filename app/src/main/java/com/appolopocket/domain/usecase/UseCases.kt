package com.appolopocket.domain.usecase

import com.appolopocket.data.remote.llm.LLMResponse
import com.appolopocket.data.remote.llm.OllamaTool
import com.appolopocket.data.remote.llm.OllamaModel
import com.appolopocket.data.remote.llm.ModelPullProgress
import com.appolopocket.data.remote.llm.OllamaFunction
import com.appolopocket.data.remote.llm.OllamaParameters
import com.appolopocket.data.remote.llm.OllamaProperty
import com.appolopocket.domain.model.*
import com.appolopocket.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val llmRepository: LLMRepository,
    private val preferencesRepository: PreferencesRepository,
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(
        conversationId: String,
        userMessage: String,
        onChunk: (String) -> Unit,
        onComplete: (Message) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val config = preferencesRepository.userPreferences.first().llmConfig
            val prompts = preferencesRepository.controlPrompts.first()
            
            llmRepository.updateConfig(config)
            
            // Get existing messages
            val existingMessages = conversationRepository.getMessages(conversationId)
            
            // Build system prompt from control prompts
            val systemPrompt = buildSystemPrompt(prompts)
            
            // Add system message if not present
            val allMessages = mutableListOf<Message>()
            if (existingMessages.isEmpty() || existingMessages.first().role != MessageRole.SYSTEM) {
                allMessages.add(Message(role = MessageRole.SYSTEM, content = systemPrompt))
            } else {
                allMessages.add(existingMessages.first())
            }
            
            // Add user message
            val userMsg = Message(role = MessageRole.USER, content = userMessage)
            allMessages.add(userMsg)
            
            // Add existing conversation messages
            allMessages.addAll(existingMessages.drop(1))
            
            // Convert functions to Ollama tools
            val tools = prompts.functionsScope.toOllamaTools()
            
            // Send to LLM
            val responseContent = StringBuilder()
            
            llmRepository.chat(allMessages, tools).collect { response ->
                when (response) {
                    is LLMResponse.Content -> {
                        responseContent.append(response.content)
                        onChunk(response.content)
                        
                        if (response.done) {
                            // Create assistant message
                            val assistantMessage = Message(
                                role = MessageRole.ASSISTANT,
                                content = responseContent.toString(),
                                metadata = MessageMetadata(
                                    model = config.modelName,
                                    tokensUsed = response.evalCount
                                )
                            )
                            
                            // Save both messages
                            conversationRepository.addMessage(conversationId, userMsg)
                            conversationRepository.addMessage(conversationId, assistantMessage)
                            
                            // Auto-memory if enabled
                            if (preferencesRepository.userPreferences.first().autoMemory) {
                                rememberConversation(userMessage, responseContent.toString())
                            }
                            
                            onComplete(assistantMessage)
                        }
                    }
                    is LLMResponse.Error -> {
                        onError(response.message)
                    }
                }
            }
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error occurred")
        }
    }
    
    private fun buildSystemPrompt(prompts: ControlPrompts): String {
        return buildString {
            appendLine("# APPOLO POCKET - AI SUPER ASSISTANT")
            appendLine()
            
            if (prompts.systemPrompt.isNotBlank()) {
                appendLine("## SYSTEM CONFIGURATION")
                appendLine(prompts.systemPrompt)
                appendLine()
            }
            
            if (prompts.personalityPrompt.isNotBlank()) {
                appendLine("## PERSONALITY")
                appendLine(prompts.personalityPrompt)
                appendLine()
            }
            
            if (prompts.agentPrompt.isNotBlank()) {
                appendLine("## AGENT BEHAVIOR")
                appendLine(prompts.agentPrompt)
                appendLine()
            }
            
            appendLine("""
                |## OPERATING MODE
                |- You are Appolo Pocket, a local-first AI assistant
                |- Prioritize user privacy and data security
                |- Use tools when appropriate for complex tasks
                |- Be helpful, concise, and creative
                |- Use ASCII art for visual flair when appropriate
            """.trimMargin())
        }
    }
    
    private suspend fun rememberConversation(userInput: String, response: String) {
        // Extract key facts and save to memory
        val memoryEntry = MemoryEntry(
            key = "conversation_${System.currentTimeMillis()}",
            value = "User: $userInput\nAppolo: $response",
            category = MemoryCategory.CONTEXT,
            tags = listOf("conversation", "auto记忆")
        )
        memoryRepository.saveMemory(memoryEntry)
    }
    
    private fun FunctionsScope.toOllamaTools(): List<OllamaTool> {
        return mcpTools.map { tool ->
            OllamaTool(
                function = OllamaFunction(
                    name = tool.name,
                    description = tool.description,
                    parameters = OllamaParameters(
                        properties = tool.inputSchema.mapValues { (key, value) ->
                            OllamaProperty(
                                type = (value as? String) ?: "string",
                                description = null
                            )
                        }
                    )
                )
            )
        }
    }
}

class CreateConversationUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    suspend operator fun invoke(title: String): Conversation {
        return conversationRepository.createConversation(title)
    }
}

class GetConversationUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    suspend operator fun invoke(id: String): Conversation? {
        return conversationRepository.getConversationById(id)
    }
}

class GetConversationsUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    operator fun invoke(): Flow<List<Conversation>> {
        return conversationRepository.getAllConversations()
    }
}

class DeleteConversationUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    suspend operator fun invoke(id: String) {
        conversationRepository.deleteConversation(id)
    }
}

class GetMessagesUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    operator fun invoke(conversationId: String): Flow<List<Message>> {
        return conversationRepository.getMessagesFlow(conversationId)
    }
}

class SaveMemoryUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(memory: MemoryEntry) {
        memoryRepository.saveMemory(memory)
    }
}

class GetMemoriesUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    operator fun invoke(): Flow<List<MemoryEntry>> {
        return memoryRepository.getAllMemories()
    }
    
    fun byCategory(category: MemoryCategory): Flow<List<MemoryEntry>> {
        return memoryRepository.getMemoriesByCategory(category)
    }
    
    fun search(query: String): Flow<List<MemoryEntry>> {
        return memoryRepository.searchMemories(query)
    }
}

class CheckLLMConnectionUseCase @Inject constructor(
    private val llmRepository: LLMRepository
) {
    suspend operator fun invoke(): Boolean {
        return llmRepository.checkConnection()
    }
}

class GetAvailableModelsUseCase @Inject constructor(
    private val llmRepository: LLMRepository
) {
    suspend operator fun invoke(): Result<List<OllamaModel>> {
        return llmRepository.listModels()
    }
}

class PullModelUseCase @Inject constructor(
    private val llmRepository: LLMRepository
) {
    suspend operator fun invoke(modelName: String): Flow<ModelPullProgress> {
        return llmRepository.pullModel(modelName)
    }
}
