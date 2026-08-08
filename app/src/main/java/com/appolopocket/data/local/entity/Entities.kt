package com.appolopocket.data.local.entity

import androidx.room.*
import com.appolopocket.domain.model.Message
import com.appolopocket.domain.model.MessageRole

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val model: String?,
    val totalTokens: Int,
    val messageCount: Int
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val role: String,
    val content: String,
    val timestamp: Long,
    val toolCallsJson: String?, // JSON serialized tool calls
    val toolResultsJson: String?, // JSON serialized tool results
    val model: String?,
    val tokensUsed: Int?,
    val executionTime: Long?
)

@Entity(tableName = "memory_entries")
data class MemoryEntryEntity(
    @PrimaryKey
    val id: String,
    val key: String,
    val value: String,
    val category: String,
    val createdAt: Long,
    val updatedAt: Long,
    val accessCount: Int,
    val tagsJson: String // JSON serialized tags
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val status: String,
    val createdAt: Long,
    val completedAt: Long?,
    val stepsJson: String, // JSON serialized steps
    val currentStepIndex: Int,
    val metadataJson: String // JSON serialized metadata
)

@Entity(tableName = "prompt_templates")
data class PromptTemplateEntity(
    @PrimaryKey
    val type: String, // system, agent, personality, functions
    val content: String,
    val updatedAt: Long
)
