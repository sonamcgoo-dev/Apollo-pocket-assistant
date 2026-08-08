package com.appolopocket.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appolopocket.domain.model.Message
import com.appolopocket.ui.components.*
import com.appolopocket.ui.theme.VaporwaveColors
import com.appolopocket.ui.viewmodel.ChatEvent
import com.appolopocket.ui.viewmodel.ChatMode
import com.appolopocket.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ChatEvent.ShowAscii -> {
                    // Handle ASCII animation display
                }
                is ChatEvent.ModeChange -> {
                    // Handle mode change animation
                }
                is ChatEvent.Error -> {
                    // Show error snackbar
                }
                ChatEvent.ScrollToBottom -> {
                    if (uiState.messages.isNotEmpty()) {
                        coroutineScope.launch {
                            listState.scrollToItem(uiState.messages.size - 1)
                        }
                    }
                }
            }
        }
    }

    // Auto-scroll when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.scrollToItem(uiState.messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VaporwaveColors.DeepNight)
    ) {
        // Background grid effect
        VaporwaveBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top bar with status and settings
            TopBar(
                connectionStatus = uiState.connectionStatus,
                currentMode = uiState.currentMode,
                onSettingsClick = onNavigateToSettings,
                onNewChatClick = { viewModel.startNewConversation() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            VaporwaveAccentBanner()

            Spacer(modifier = Modifier.height(12.dp))

            // Mode tabs
            ModeTabs(
                currentMode = uiState.currentMode,
                onModeSelected = { viewModel.setMode(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Messages list
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (uiState.messages.isEmpty() && !uiState.isLoading) {
                    // Empty state
                    EmptyStateView()
                } else {
                    MessagesList(
                        messages = uiState.messages,
                        listState = listState,
                        isStreaming = uiState.isStreaming,
                        streamedContent = uiState.streamedContent
                    )
                }
            }

            // ASCII status overlay
            if (uiState.showAsciiAnimation) {
                AsciiAnimationOverlay(message = uiState.asciiMessage)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input area
            InputArea(
                inputText = uiState.inputText,
                onInputChange = { viewModel.setInputText(it) },
                onSend = { viewModel.sendMessage() },
                isLoading = uiState.isLoading
            )
        }

        // Quick tiles panel
        QuickTilePanel(
            onVoiceClick = { /* TODO: Implement voice input */ },
            onCameraClick = { /* TODO: Implement camera */ },
            onFilesClick = { /* TODO: Implement file browser */ },
            onSearchClick = { viewModel.setMode(ChatMode.SEARCH) },
            onSettingsClick = onNavigateToSettings,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 100.dp)
        )

        // Error snackbar
        uiState.error?.let { error ->
            ErrorSnackbar(
                message = error,
                onDismiss = { viewModel.clearError() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    connectionStatus: ConnectionStatus,
    currentMode: ChatMode,
    onSettingsClick: () -> Unit,
    onNewChatClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Apollo logo/title
        Column {
            Text(
                text = "APPOLO POCKET",
                style = MaterialTheme.typography.titleLarge,
                color = VaporwaveColors.NeonMagenta
            )
            Text(
                text = "AI Super Assistant",
                style = MaterialTheme.typography.labelSmall,
                color = VaporwaveColors.TextSecondary
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Connection status
        StatusIndicator(status = connectionStatus)

        Spacer(modifier = Modifier.width(16.dp))

        // New chat button
        IconButton(onClick = onNewChatClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New chat",
                tint = VaporwaveColors.ElectricCyan
            )
        }

        // Settings button
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = VaporwaveColors.TextSecondary
            )
        }
    }
}

@Composable
private fun ModeTabs(
    currentMode: ChatMode,
    onModeSelected: (ChatMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = VaporwaveColors.GlassyPurple.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ModeTab(
            label = "Chat",
            icon = Icons.Default.Chat,
            isSelected = currentMode == ChatMode.CHAT,
            onClick = { onModeSelected(ChatMode.CHAT) }
        )
        ModeTab(
            label = "Code",
            icon = Icons.Default.Code,
            isSelected = currentMode == ChatMode.CODE,
            onClick = { onModeSelected(ChatMode.CODE) }
        )
        ModeTab(
            label = "Auto",
            icon = Icons.Default.AutoAwesome,
            isSelected = currentMode == ChatMode.AUTOMATION,
            onClick = { onModeSelected(ChatMode.AUTOMATION) }
        )
        ModeTab(
            label = "Search",
            icon = Icons.Default.Search,
            isSelected = currentMode == ChatMode.SEARCH,
            onClick = { onModeSelected(ChatMode.SEARCH) }
        )
    }
}

@Composable
private fun ModeTab(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        VaporwaveColors.NeonMagenta.copy(alpha = 0.2f)
    } else {
        VaporwaveColors.GlassyPurple
    }
    val contentColor = if (isSelected) {
        VaporwaveColors.NeonMagenta
    } else {
        VaporwaveColors.TextSecondary
    }

    Surface(
        onClick = onClick,
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun MessagesList(
    messages: List<Message>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    isStreaming: Boolean,
    streamedContent: String
) {
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(messages, key = { it.id }) { message ->
            MessageBubble(message = message)
        }

        // Streaming content
        if (isStreaming && streamedContent.isNotEmpty()) {
            item {
                StreamingMessage(content = streamedContent)
            }
        }
    }
}

@Composable
private fun StreamingMessage(content: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .align(Alignment.CenterStart)
                .background(
                    color = VaporwaveColors.GlassyPurple,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(12.dp)
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                ),
                color = VaporwaveColors.TextPrimary
            )
            Text(
                text = "▌",
                color = VaporwaveColors.NeonMagenta,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                )
            )
        }
    }
}

@Composable
private fun EmptyStateView() {
    val startupArt = remember { AsciiArt.generateVaporwaveStartupArt() }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedAsciiText(
            text = startupArt,
            color = VaporwaveColors.NeonMagenta
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "WELCOME TO APOLLO POCKET",
            style = MaterialTheme.typography.headlineSmall,
            color = VaporwaveColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "VAPORWAVE AI SUPER ASSISTANT",
            style = MaterialTheme.typography.bodyMedium,
            color = VaporwaveColors.TextSecondary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        AsciiDivider(length = 40)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Type a message to begin...",
            style = MaterialTheme.typography.bodySmall,
            color = VaporwaveColors.TextTertiary
        )
    }
}

@Composable
private fun VaporwaveAccentBanner() {
    val kanjiLine = remember {
        (1..6).joinToString("  ") {
            listOf("愛", "夢", "神", "電", "夜", "空", "幻", "海").random()
        }
    }
    val checker = "▓░".repeat(24)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = VaporwaveColors.GlassBackground,
                shape = MaterialTheme.shapes.medium
            )
            .border(
                width = 1.dp,
                color = VaporwaveColors.ElectricCyan.copy(alpha = 0.4f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = checker,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = VaporwaveColors.NeonMagenta.copy(alpha = 0.7f)
        )
        Text(
            text = kanjiLine,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = VaporwaveColors.ElectricCyan
        )
        Text(
            text = checker.reversed(),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = VaporwaveColors.NeonMagenta.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun InputArea(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = VaporwaveColors.SoftLavender,
                shape = MaterialTheme.shapes.large
            )
            .border(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        VaporwaveColors.NeonMagenta.copy(alpha = 0.3f),
                        VaporwaveColors.ElectricCyan.copy(alpha = 0.3f)
                    )
                ),
                shape = MaterialTheme.shapes.large
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppoloTextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            placeholder = "Ask Appolo anything...",
            singleLine = false,
            maxLines = 4,
            enabled = !isLoading,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = androidx.compose.ui.text.input.ImeAction.Send
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onSend = { onSend() }
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        FloatingActionButton(
            onClick = onSend,
            containerColor = VaporwaveColors.NeonMagenta,
            contentColor = VaporwaveColors.DeepNight,
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = VaporwaveColors.DeepNight,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send"
                )
            }
        }
    }
}

@Composable
private fun AsciiAnimationOverlay(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = VaporwaveColors.GlassBackground,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            LoadingIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = VaporwaveColors.TextSecondary
            )
        }
    }
}

@Composable
private fun ErrorSnackbar(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Snackbar(
        modifier = modifier,
        action = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = VaporwaveColors.NeonMagenta)
            }
        },
        containerColor = VaporwaveColors.GlassyPurple,
        contentColor = VaporwaveColors.HotPink
    ) {
        Text(message)
    }
}
