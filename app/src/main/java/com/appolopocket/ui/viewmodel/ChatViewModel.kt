package com.appolopocket.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appolopocket.domain.model.*
import com.appolopocket.domain.repository.LLMRepository
import com.appolopocket.domain.repository.PreferencesRepository
import com.appolopocket.domain.usecase.*
import com.appolopocket.ui.components.ConnectionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isStreaming: Boolean = false,
    val streamedContent: String = "",
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val currentMode: ChatMode = ChatMode.CHAT,
    val error: String? = null
)

enum class ChatMode {
    CHAT, CODE, AUTOMATION, SEARCH
}

sealed class ChatEvent {
    data class Error(val message: String) : ChatEvent()
    data object ScrollToBottom : ChatEvent()
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val createConversationUseCase: CreateConversationUseCase,
    private val getConversationUseCase: GetConversationUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val deleteConversationUseCase: DeleteConversationUseCase,
    private val checkLLMConnectionUseCase: CheckLLMConnectionUseCase,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ChatEvent>()
    val events: SharedFlow<ChatEvent> = _events.asSharedFlow()

    private var currentConversationId: String? = null
    private var streamingJob: Job? = null

    init {
        loadPreferences()
        checkConnection()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesRepository.userPreferences.collect { prefs ->
                _uiState.update {
                    it.copy(
                        connectionStatus = if (prefs.llmConfig.baseUrl.isNotBlank()) {
                            ConnectionStatus.CONNECTING
                        } else {
                            ConnectionStatus.DISCONNECTED
                        }
                    )
                }
            }
        }
    }

    fun checkConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(connectionStatus = ConnectionStatus.CONNECTING) }
            val isConnected = checkLLMConnectionUseCase()
            _uiState.update {
                it.copy(
                    connectionStatus = if (isConnected) {
                        ConnectionStatus.CONNECTED
                    } else {
                        ConnectionStatus.DISCONNECTED
                    }
                )
            }
        }
    }

    fun setInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun setMode(mode: ChatMode) {
        _uiState.update { it.copy(currentMode = mode) }
    }

    fun sendMessage() {
        val inputText = _uiState.value.inputText.trim()
        if (inputText.isBlank() || _uiState.value.isLoading) return

        viewModelScope.launch {
            // Create conversation if needed
            if (currentConversationId == null) {
                val conversation = createConversationUseCase("New Chat")
                currentConversationId = conversation.id
                preferencesRepository.setCurrentConversationId(conversation.id)
            }

            val convId = currentConversationId ?: return@launch

            // Clear input and show loading
            _uiState.update {
                it.copy(
                    inputText = "",
                    isLoading = true,
                    isStreaming = true,
                    streamedContent = "",
                    error = null
                )
            }

            sendMessageUseCase(
                conversationId = convId,
                userMessage = inputText,
                onChunk = { chunk ->
                    _uiState.update {
                        it.copy(streamedContent = it.streamedContent + chunk)
                    }
                    viewModelScope.launch {
                        _events.emit(ChatEvent.ScrollToBottom)
                    }
                },
                onComplete = { message ->
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages + message,
                            isLoading = false,
                            isStreaming = false,
                            streamedContent = ""
                        )
                    }
                    viewModelScope.launch {
                        _events.emit(ChatEvent.ScrollToBottom)
                    }
                },
                onError = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isStreaming = false,
                            error = error
                        )
                    }
                    viewModelScope.launch {
                        _events.emit(ChatEvent.Error(error))
                    }
                }
            )
        }
    }

    fun loadConversation(conversationId: String) {
        viewModelScope.launch {
            currentConversationId = conversationId
            preferencesRepository.setCurrentConversationId(conversationId)
            
            getMessagesUseCase(conversationId).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    fun startNewConversation() {
        viewModelScope.launch {
            currentConversationId = null
            _uiState.update { it.copy(messages = emptyList()) }
            preferencesRepository.setCurrentConversationId(null)
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            deleteConversationUseCase(conversationId)
            if (currentConversationId == conversationId) {
                startNewConversation()
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun showUnavailableFeature(featureName: String) {
        _uiState.update { it.copy(error = "$featureName is not available yet.") }
    }

    override fun onCleared() {
        super.onCleared()
        streamingJob?.cancel()
    }
}
