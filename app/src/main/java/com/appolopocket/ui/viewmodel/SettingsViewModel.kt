package com.appolopocket.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appolopocket.data.remote.llm.OllamaModel
import com.appolopocket.domain.model.*
import com.appolopocket.domain.repository.LLMRepository
import com.appolopocket.domain.repository.PreferencesRepository
import com.appolopocket.domain.usecase.GetAvailableModelsUseCase
import com.appolopocket.domain.usecase.PullModelUseCase
import com.appolopocket.ui.components.ConnectionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userPreferences: UserPreferences = UserPreferences(),
    val controlPrompts: ControlPrompts = ControlPrompts(),
    val availableModels: List<OllamaModel> = emptyList(),
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val isLoadingModels: Boolean = false,
    val isPullingModel: Boolean = false,
    val pullProgress: String = "",
    val editingPromptType: PromptType? = null,
    val error: String? = null
)

enum class PromptType {
    SYSTEM, AGENT, PERSONALITY, FUNCTIONS
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val llmRepository: LLMRepository,
    private val getAvailableModelsUseCase: GetAvailableModelsUseCase,
    private val pullModelUseCase: PullModelUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
        checkConnection()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesRepository.userPreferences.collect { prefs ->
                _uiState.update { it.copy(userPreferences = prefs) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.controlPrompts.collect { prompts ->
                _uiState.update { it.copy(controlPrompts = prompts) }
            }
        }
    }

    fun checkConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(connectionStatus = ConnectionStatus.CONNECTING) }
            val source = _uiState.value.userPreferences.llmConfig.modelSource
            val isConnected = if (source == ModelSource.OLLAMA) {
                llmRepository.checkConnection()
            } else {
                true
            }
            _uiState.update {
                it.copy(
                    connectionStatus = if (isConnected) {
                        ConnectionStatus.CONNECTED
                    } else {
                        ConnectionStatus.DISCONNECTED
                    }
                )
            }
            if (isConnected) {
                loadAvailableModels()
            }
        }
    }

    fun loadAvailableModels() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingModels = true) }
            getAvailableModelsUseCase().fold(
                onSuccess = { models ->
                    _uiState.update {
                        it.copy(
                            availableModels = models,
                            isLoadingModels = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingModels = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    fun updateLLMConfig(config: LLMConfig) {
        viewModelScope.launch {
            preferencesRepository.updateLLMConfig(config)
            llmRepository.updateConfig(config)
            checkConnection()
        }
    }

    fun updateModel(modelName: String) {
        val currentConfig = _uiState.value.userPreferences.llmConfig
        updateLLMConfig(currentConfig.copy(modelName = modelName))
    }

    fun updateBaseUrl(url: String) {
        val currentConfig = _uiState.value.userPreferences.llmConfig
        updateLLMConfig(currentConfig.copy(baseUrl = url))
    }

    fun updateModelSource(source: ModelSource) {
        val currentConfig = _uiState.value.userPreferences.llmConfig
        updateLLMConfig(currentConfig.copy(modelSource = source))
    }

    fun updateTemperature(temperature: Float) {
        val currentConfig = _uiState.value.userPreferences.llmConfig
        updateLLMConfig(currentConfig.copy(temperature = temperature))
    }

    fun updateMaxTokens(maxTokens: Int) {
        val currentConfig = _uiState.value.userPreferences.llmConfig
        updateLLMConfig(currentConfig.copy(maxTokens = maxTokens))
    }

    fun updateApprovalMode(mode: ApprovalMode) {
        viewModelScope.launch {
            preferencesRepository.updateApprovalMode(mode)
        }
    }

    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferencesRepository.updateTheme(theme)
        }
    }

    fun updateVoiceEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateVoiceEnabled(enabled)
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateNotificationsEnabled(enabled)
        }
    }

    fun updateAutoMemory(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAutoMemory(enabled)
        }
    }

    fun startEditingPrompt(type: PromptType) {
        _uiState.update { it.copy(editingPromptType = type) }
    }

    fun stopEditingPrompt() {
        _uiState.update { it.copy(editingPromptType = null) }
    }

    fun updateSystemPrompt(prompt: String) {
        viewModelScope.launch {
            preferencesRepository.updateSystemPrompt(prompt)
        }
    }

    fun updateAgentPrompt(prompt: String) {
        viewModelScope.launch {
            preferencesRepository.updateAgentPrompt(prompt)
        }
    }

    fun updatePersonalityPrompt(prompt: String) {
        viewModelScope.launch {
            preferencesRepository.updatePersonalityPrompt(prompt)
        }
    }

    fun updateFunctionsScope(scope: FunctionsScope) {
        viewModelScope.launch {
            preferencesRepository.updateFunctionsScope(scope)
        }
    }

    fun pullModel(modelName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPullingModel = true, pullProgress = "Starting...") }
            pullModelUseCase(modelName).collect { progress ->
                when (progress) {
                    is com.appolopocket.data.remote.llm.ModelPullProgress.Update -> {
                        _uiState.update {
                            it.copy(pullProgress = "${progress.status} ${progress.completed}/${progress.total}")
                        }
                    }
                    is com.appolopocket.data.remote.llm.ModelPullProgress.Complete -> {
                        _uiState.update {
                            it.copy(isPullingModel = false, pullProgress = "")
                        }
                        loadAvailableModels()
                    }
                    is com.appolopocket.data.remote.llm.ModelPullProgress.Error -> {
                        _uiState.update {
                            it.copy(isPullingModel = false, error = progress.message)
                        }
                    }
                }
            }
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            preferencesRepository.resetToDefaults()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
