package com.appolopocket.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a message in the conversation with the AI
 */
@Serializable
data class Message(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toolCalls: List<ToolCall> = emptyList(),
    val toolResults: List<ToolResult> = emptyList(),
    val metadata: MessageMetadata? = null
)

@Serializable
enum class MessageRole {
    SYSTEM,
    USER,
    ASSISTANT,
    TOOL
}

@Serializable
data class MessageMetadata(
    val model: String? = null,
    val tokensUsed: Int? = null,
    val executionTime: Long? = null
)

/**
 * Tool call request from the AI
 */
@Serializable
data class ToolCall(
    val id: String,
    val name: String,
    val arguments: Map<String, @Serializable(with = AnySerializer::class) Any>
)

@Serializable
data class ToolResult(
    val callId: String,
    val name: String,
    val success: Boolean,
    val result: @Serializable(with = AnySerializer::class) Any?,
    val error: String? = null
)

/**
 * AI Model configuration
 */
@Serializable
data class LLMConfig(
    val modelName: String = "llama2",
    val baseUrl: String = "http://localhost:11434",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 4096,
    val contextWindow: Int = 4096,
    val stream: Boolean = true
)

/**
 * Control prompts for AI behavior
 */
@Serializable
data class ControlPrompts(
    val systemPrompt: String = "",
    val agentPrompt: String = "",
    val personalityPrompt: String = "",
    val functionsScope: FunctionsScope = FunctionsScope()
)

@Serializable
data class FunctionsScope(
    val capabilities: List<String> = emptyList(),
    val permissions: List<String> = emptyList(),
    val autoApproveRules: List<AutoApproveRule> = emptyList(),
    val mcpTools: List<MCPTool> = emptyList()
)

@Serializable
data class AutoApproveRule(
    val pattern: String,
    val action: String,
    val enabled: Boolean = true
)

@Serializable
data class MCPTool(
    val name: String,
    val description: String,
    val inputSchema: Map<String, @Serializable(with = AnySerializer::class) Any>
)

/**
 * Task for automation and scheduling
 */
@Serializable
data class Task(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val status: TaskStatus = TaskStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val steps: List<TaskStep> = emptyList(),
    val currentStepIndex: Int = 0,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED
}

@Serializable
data class TaskStep(
    val id: String = java.util.UUID.randomUUID().toString(),
    val description: String,
    val toolName: String,
    val arguments: Map<String, @Serializable(with = AnySerializer::class) Any> = emptyMap(),
    val status: StepStatus = StepStatus.PENDING,
    val result: String? = null,
    val error: String? = null
)

@Serializable
enum class StepStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED
}

/**
 * Memory entry for persistent storage
 */
@Serializable
data class MemoryEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val key: String,
    val value: String,
    val category: MemoryCategory = MemoryCategory.GENERAL,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val accessCount: Int = 0,
    val tags: List<String> = emptyList()
)

@Serializable
enum class MemoryCategory {
    GENERAL,
    USER_PREFERENCE,
    TASK_HISTORY,
    FACTS,
    SKILLS,
    CONTEXT
}

/**
 * Conversation session
 */
@Serializable
data class Conversation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val messages: List<Message> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val metadata: ConversationMetadata = ConversationMetadata()
)

@Serializable
data class ConversationMetadata(
    val model: String? = null,
    val totalTokens: Int = 0,
    val messageCount: Int = 0
)

/**
 * User preferences
 */
@Serializable
data class UserPreferences(
    val approvalMode: ApprovalMode = ApprovalMode.SMART_AUTO,
    val theme: AppTheme = AppTheme.VAPORWAVE,
    val voiceEnabled: Boolean = false,
        val notificationsEnabled: Boolean = true,
    val autoMemory: Boolean = true,
    val asciiAnimations: Boolean = true,
    val llmConfig: LLMConfig = LLMConfig()
)

@Serializable
enum class ApprovalMode {
    FULL_AUTO,
    SMART_AUTO,
    MANUAL
}

@Serializable
enum class AppTheme {
    VAPORWAVE,
    NEON,
    MINIMAL,
    CLASSIC
}
