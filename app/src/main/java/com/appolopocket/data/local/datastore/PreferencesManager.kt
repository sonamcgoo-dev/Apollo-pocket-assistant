package com.appolopocket.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.appolopocket.domain.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "appolo_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    private object PreferencesKeys {
        // LLM Config
        val MODEL_NAME = stringPreferencesKey("llm_model_name")
        val OLLAMA_URL = stringPreferencesKey("ollama_url")
        val MODEL_SOURCE = stringPreferencesKey("llm_model_source")
        val TEMPERATURE = floatPreferencesKey("llm_temperature")
        val MAX_TOKENS = intPreferencesKey("llm_max_tokens")
        val CONTEXT_WINDOW = intPreferencesKey("llm_context_window")
        
        // User Preferences
        val APPROVAL_MODE = stringPreferencesKey("approval_mode")
        val THEME = stringPreferencesKey("app_theme")
        val VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val AUTO_MEMORY = booleanPreferencesKey("auto_memory")
        
        // Control Prompts
        val SYSTEM_PROMPT = stringPreferencesKey("system_prompt")
        val AGENT_PROMPT = stringPreferencesKey("agent_prompt")
        val PERSONALITY_PROMPT = stringPreferencesKey("personality_prompt")
        val FUNCTIONS_SCOPE = stringPreferencesKey("functions_scope")
        
        // App State
        val CURRENT_CONVERSATION_ID = stringPreferencesKey("current_conversation_id")
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        val LAST_MODEL_USED = stringPreferencesKey("last_model_used")
    }
    
    val userPreferences: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                approvalMode = preferences[PreferencesKeys.APPROVAL_MODE]?.let {
                    ApprovalMode.valueOf(it)
                } ?: ApprovalMode.SMART_AUTO,
                theme = preferences[PreferencesKeys.THEME]?.let {
                    AppTheme.valueOf(it)
                } ?: AppTheme.VAPORWAVE,
                voiceEnabled = preferences[PreferencesKeys.VOICE_ENABLED] ?: false,
                notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
                autoMemory = preferences[PreferencesKeys.AUTO_MEMORY] ?: true,
                llmConfig = LLMConfig(
                    modelName = preferences[PreferencesKeys.MODEL_NAME] ?: "llama2",
                    baseUrl = preferences[PreferencesKeys.OLLAMA_URL] ?: "http://localhost:11434",
                    modelSource = preferences[PreferencesKeys.MODEL_SOURCE]?.let {
                        runCatching { ModelSource.valueOf(it) }.getOrDefault(ModelSource.OLLAMA)
                    } ?: ModelSource.OLLAMA,
                    temperature = preferences[PreferencesKeys.TEMPERATURE] ?: 0.7f,
                    maxTokens = preferences[PreferencesKeys.MAX_TOKENS] ?: 4096,
                    contextWindow = preferences[PreferencesKeys.CONTEXT_WINDOW] ?: 4096
                )
            )
        }
    
    val controlPrompts: Flow<ControlPrompts> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            ControlPrompts(
                systemPrompt = preferences[PreferencesKeys.SYSTEM_PROMPT] ?: "",
                agentPrompt = preferences[PreferencesKeys.AGENT_PROMPT] ?: "",
                personalityPrompt = preferences[PreferencesKeys.PERSONALITY_PROMPT] ?: "",
                functionsScope = preferences[PreferencesKeys.FUNCTIONS_SCOPE]?.let {
                    try {
                        json.decodeFromString<FunctionsScope>(it)
                    } catch (e: Exception) {
                        FunctionsScope()
                    }
                } ?: FunctionsScope()
            )
        }
    
    val currentConversationId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CURRENT_CONVERSATION_ID]
        }
    
    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH] ?: true
        }
    
    suspend fun updateLLMConfig(config: LLMConfig) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MODEL_NAME] = config.modelName
            preferences[PreferencesKeys.OLLAMA_URL] = config.baseUrl
            preferences[PreferencesKeys.MODEL_SOURCE] = config.modelSource.name
            preferences[PreferencesKeys.TEMPERATURE] = config.temperature
            preferences[PreferencesKeys.MAX_TOKENS] = config.maxTokens
            preferences[PreferencesKeys.CONTEXT_WINDOW] = config.contextWindow
        }
    }
    
    suspend fun updateApprovalMode(mode: ApprovalMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APPROVAL_MODE] = mode.name
        }
    }
    
    suspend fun updateTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme.name
        }
    }
    
    suspend fun updateVoiceEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VOICE_ENABLED] = enabled
        }
    }
    
    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }
    
    suspend fun updateAutoMemory(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_MEMORY] = enabled
        }
    }
    
    suspend fun updateSystemPrompt(prompt: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SYSTEM_PROMPT] = prompt
        }
    }
    
    suspend fun updateAgentPrompt(prompt: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AGENT_PROMPT] = prompt
        }
    }
    
    suspend fun updatePersonalityPrompt(prompt: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PERSONALITY_PROMPT] = prompt
        }
    }
    
    suspend fun updateFunctionsScope(scope: FunctionsScope) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FUNCTIONS_SCOPE] = json.encodeToString(scope)
        }
    }
    
    suspend fun setCurrentConversationId(id: String?) {
        context.dataStore.edit { preferences ->
            if (id != null) {
                preferences[PreferencesKeys.CURRENT_CONVERSATION_ID] = id
            } else {
                preferences.remove(PreferencesKeys.CURRENT_CONVERSATION_ID)
            }
        }
    }
    
    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH] = false
        }
    }
    
    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
