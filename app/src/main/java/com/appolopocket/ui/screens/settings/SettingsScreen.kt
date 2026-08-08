package com.appolopocket.ui.screens.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appolopocket.data.remote.llm.OllamaModel
import com.appolopocket.domain.model.ApprovalMode
import com.appolopocket.domain.model.AppTheme
import com.appolopocket.ui.components.*
import com.appolopocket.ui.theme.VaporwaveColors
import com.appolopocket.ui.viewmodel.PromptType
import com.appolopocket.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showPromptEditor by remember { mutableStateOf<PromptType?>(null) }
    var showModelSelector by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VaporwaveColors.DeepNight)
    ) {
        VaporwaveBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top bar
            SettingsTopBar(onNavigateBack = onNavigateBack)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Connection Status
                item {
                    ConnectionSection(
                        status = uiState.connectionStatus,
                        onRefresh = { viewModel.checkConnection() }
                    )
                }
                
                // LLM Settings
                item {
                    LLMSettingsSection(
                        config = uiState.userPreferences.llmConfig,
                        availableModels = uiState.availableModels,
                        isLoadingModels = uiState.isLoadingModels,
                        onModelSelect = { showModelSelector = true },
                        onOllamaUrlChange = { viewModel.updateOllamaUrl(it) },
                        onTemperatureChange = { viewModel.updateTemperature(it) },
                        onMaxTokensChange = { viewModel.updateMaxTokens(it) },
                        onPullModel = { viewModel.pullModel(it) }
                    )
                }
                
                // Approval Mode
                item {
                    ApprovalSection(
                        currentMode = uiState.userPreferences.approvalMode,
                        onModeChange = { viewModel.updateApprovalMode(it) }
                    )
                }
                
                // Appearance
                item {
                    AppearanceSection(
                        currentTheme = uiState.userPreferences.theme,
                        asciiAnimations = uiState.userPreferences.asciiAnimations,
                        onThemeChange = { viewModel.updateTheme(it) },
                        onAsciiAnimationsChange = { viewModel.updateAsciiAnimations(it) }
                    )
                }
                
                // Prompts
                item {
                    PromptsSection(
                        onEditSystemPrompt = { showPromptEditor = PromptType.SYSTEM },
                        onEditAgentPrompt = { showPromptEditor = PromptType.AGENT },
                        onEditPersonalityPrompt = { showPromptEditor = PromptType.PERSONALITY },
                        onEditFunctionsScope = { showPromptEditor = PromptType.FUNCTIONS }
                    )
                }
                
                // General Settings
                item {
                    GeneralSection(
                        voiceEnabled = uiState.userPreferences.voiceEnabled,
                        notificationsEnabled = uiState.userPreferences.notificationsEnabled,
                        autoMemory = uiState.userPreferences.autoMemory,
                        onVoiceEnabledChange = { viewModel.updateVoiceEnabled(it) },
                        onNotificationsEnabledChange = { viewModel.updateNotificationsEnabled(it) },
                        onAutoMemoryChange = { viewModel.updateAutoMemory(it) }
                    )
                }
                
                // About
                item {
                    AboutSection()
                }
                
                // Reset
                item {
                    ResetSection(onReset = { viewModel.resetToDefaults() })
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
        
        // Prompt Editor Dialog
        showPromptEditor?.let { promptType ->
            PromptEditorDialog(
                promptType = promptType,
                currentPrompt = when (promptType) {
                    PromptType.SYSTEM -> uiState.controlPrompts.systemPrompt
                    PromptType.AGENT -> uiState.controlPrompts.agentPrompt
                    PromptType.PERSONALITY -> uiState.controlPrompts.personalityPrompt
                    PromptType.FUNCTIONS -> uiState.controlPrompts.functionsScope.toString()
                },
                onDismiss = { showPromptEditor = null },
                onSave = { newPrompt ->
                    when (promptType) {
                        PromptType.SYSTEM -> viewModel.updateSystemPrompt(newPrompt)
                        PromptType.AGENT -> viewModel.updateAgentPrompt(newPrompt)
                        PromptType.PERSONALITY -> viewModel.updatePersonalityPrompt(newPrompt)
                        PromptType.FUNCTIONS -> { /* Parse and save */ }
                    }
                    showPromptEditor = null
                }
            )
        }
        
        // Model Selector Dialog
        if (showModelSelector) {
            ModelSelectorDialog(
                models = uiState.availableModels,
                currentModel = uiState.userPreferences.llmConfig.modelName,
                onDismiss = { showModelSelector = false },
                onSelect = {
                    viewModel.updateModel(it)
                    showModelSelector = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Settings",
                color = VaporwaveColors.NeonMagenta
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = VaporwaveColors.TextPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = VaporwaveColors.DeepNight
        )
    )
}

@Composable
private fun ConnectionSection(
    status: ConnectionStatus,
    onRefresh: () -> Unit
) {
    SettingsSection(title = "Connection") {
        SettingsRow(
            label = "Ollama Server",
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusIndicator(status = status)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = VaporwaveColors.ElectricCyan
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun LLMSettingsSection(
    config: com.appolopocket.domain.model.LLMConfig,
    availableModels: List<com.appolopocket.data.remote.llm.OllamaModel>,
    isLoadingModels: Boolean,
    onModelSelect: () -> Unit,
    onOllamaUrlChange: (String) -> Unit,
    onTemperatureChange: (Float) -> Unit,
    onMaxTokensChange: (Int) -> Unit,
    onPullModel: (String) -> Unit
) {
    SettingsSection(title = "LLM Settings") {
        // Model selection
        SettingsRow(
            label = "Model",
            description = config.modelName,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = VaporwaveColors.VaporwavePurple
                )
            },
            trailing = {
                TextButton(onClick = onModelSelect) {
                    Text(
                        text = if (isLoadingModels) "Loading..." else "Change",
                        color = VaporwaveColors.ElectricCyan
                    )
                }
            }
        )
        
        HorizontalDivider(color = VaporwaveColors.GlassyPurple)
        
        // Ollama URL
        SettingsRow(
            label = "Server URL",
            description = config.baseUrl,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = null,
                    tint = VaporwaveColors.VaporwavePurple
                )
            }
        )
        
        HorizontalDivider(color = VaporwaveColors.GlassyPurple)
        
        // Temperature slider
        SettingsRow(
            label = "Temperature",
            description = String.format("%.2f", config.temperature),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Thermostat,
                    contentDescription = null,
                    tint = VaporwaveColors.VaporwavePurple
                )
            },
            trailing = {
                Slider(
                    value = config.temperature,
                    onValueChange = onTemperatureChange,
                    valueRange = 0f..2f,
                    modifier = Modifier.width(150.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = VaporwaveColors.NeonMagenta,
                        activeTrackColor = VaporwaveColors.NeonMagenta
                    )
                )
            }
        )
        
        HorizontalDivider(color = VaporwaveColors.GlassyPurple)
        
        // Max tokens
        SettingsRow(
            label = "Max Tokens",
            description = config.maxTokens.toString(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.TextFields,
                    contentDescription = null,
                    tint = VaporwaveColors.VaporwavePurple
                )
            },
            trailing = {
                Slider(
                    value = config.maxTokens.toFloat(),
                    onValueChange = { onMaxTokensChange(it.toInt()) },
                    valueRange = 256f..8192f,
                    steps = 6,
                    modifier = Modifier.width(150.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = VaporwaveColors.NeonMagenta,
                        activeTrackColor = VaporwaveColors.NeonMagenta
                    )
                )
            }
        )
    }
}

@Composable
private fun ApprovalSection(
    currentMode: ApprovalMode,
    onModeChange: (ApprovalMode) -> Unit
) {
    SettingsSection(title = "Auto-Approval") {
        ApprovalMode.entries.forEach { mode ->
            SettingsRow(
                label = mode.name.replace("_", " "),
                description = when (mode) {
                    ApprovalMode.FULL_AUTO -> "Approve all non-destructive tasks"
                    ApprovalMode.SMART_AUTO -> "Approve based on learned patterns"
                    ApprovalMode.MANUAL -> "Require confirmation for all tasks"
                },
                leadingIcon = {
                    RadioButton(
                        selected = currentMode == mode,
                        onClick = { onModeChange(mode) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = VaporwaveColors.NeonMagenta
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun AppearanceSection(
    currentTheme: AppTheme,
    asciiAnimations: Boolean,
    onThemeChange: (AppTheme) -> Unit,
    onAsciiAnimationsChange: (Boolean) -> Unit
) {
    SettingsSection(title = "Appearance") {
        // Theme selection
        Text(
            text = "Theme",
            style = MaterialTheme.typography.bodyMedium,
            color = VaporwaveColors.TextPrimary
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppTheme.entries.forEach { theme ->
                FilterChip(
                    selected = currentTheme == theme,
                    onClick = { onThemeChange(theme) },
                    label = { Text(theme.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VaporwaveColors.NeonMagenta.copy(alpha = 0.2f),
                        selectedLabelColor = VaporwaveColors.NeonMagenta
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // ASCII animations toggle
        SettingsRow(
            label = "ASCII Animations",
            description = "Show ASCII art transitions and decorations",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = VaporwaveColors.GoldenBronze
                )
            },
            trailing = {
                Switch(
                    checked = asciiAnimations,
                    onCheckedChange = onAsciiAnimationsChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = VaporwaveColors.NeonMagenta,
                        checkedTrackColor = VaporwaveColors.NeonMagenta.copy(alpha = 0.3f)
                    )
                )
            }
        )
    }
}

@Composable
private fun PromptsSection(
    onEditSystemPrompt: () -> Unit,
    onEditAgentPrompt: () -> Unit,
    onEditPersonalityPrompt: () -> Unit,
    onEditFunctionsScope: () -> Unit
) {
    SettingsSection(title = "Control Prompts") {
        val promptItems = listOf(
            "System Prompt" to onEditSystemPrompt,
            "Agent Prompt" to onEditAgentPrompt,
            "Personality Prompt" to onEditPersonalityPrompt,
            "Functions & Scope" to onEditFunctionsScope
        )
        
        promptItems.forEachIndexed { index, (label, onClick) ->
            if (index > 0) {
                HorizontalDivider(color = VaporwaveColors.GlassyPurple)
            }
            SettingsRow(
                label = label,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = VaporwaveColors.VaporwavePurple
                    )
                },
                trailing = {
                    IconButton(onClick = onClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = VaporwaveColors.ElectricCyan
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun GeneralSection(
    voiceEnabled: Boolean,
    notificationsEnabled: Boolean,
    autoMemory: Boolean,
    onVoiceEnabledChange: (Boolean) -> Unit,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    onAutoMemoryChange: (Boolean) -> Unit
) {
    SettingsSection(title = "General") {
        SettingsRow(
            label = "Voice Input",
            description = "Enable voice commands",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = VaporwaveColors.VaporwavePurple
                )
            },
            trailing = {
                Switch(
                    checked = voiceEnabled,
                    onCheckedChange = onVoiceEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = VaporwaveColors.NeonMagenta,
                        checkedTrackColor = VaporwaveColors.NeonMagenta.copy(alpha = 0.3f)
                    )
                )
            }
        )
        
        HorizontalDivider(color = VaporwaveColors.GlassyPurple)
        
        SettingsRow(
            label = "Notifications",
            description = "Show notification responses",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = VaporwaveColors.VaporwavePurple
                )
            },
            trailing = {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = onNotificationsEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = VaporwaveColors.NeonMagenta,
                        checkedTrackColor = VaporwaveColors.NeonMagenta.copy(alpha = 0.3f)
                    )
                )
            }
        )
        
        HorizontalDivider(color = VaporwaveColors.GlassyPurple)
        
        SettingsRow(
            label = "Auto Memory",
            description = "Automatically remember conversations",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = VaporwaveColors.VaporwavePurple
                )
            },
            trailing = {
                Switch(
                    checked = autoMemory,
                    onCheckedChange = onAutoMemoryChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = VaporwaveColors.NeonMagenta,
                        checkedTrackColor = VaporwaveColors.NeonMagenta.copy(alpha = 0.3f)
                    )
                )
            }
        )
    }
}

@Composable
private fun AboutSection() {
    SettingsSection(title = "About") {
        SettingsRow(
            label = "Appolo Pocket",
            description = "Version 1.0.0"
        )
        SettingsRow(
            label = "Built with",
            description = "Kotlin, Jetpack Compose, Ollama"
        )
        SettingsRow(
            label = "License",
            description = "MIT License"
        )
    }
}

@Composable
private fun ResetSection(onReset: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }
    
    SettingsSection(title = "Reset") {
        AppoloButton(
            text = "Reset to Defaults",
            onClick = { showConfirm = true },
            variant = ButtonVariant.Outline,
            modifier = Modifier.fillMaxWidth()
        )
    }
    
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Reset Settings?") },
            text = { Text("This will reset all settings to their default values. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReset()
                        showConfirm = false
                    }
                ) {
                    Text("Reset", color = VaporwaveColors.HotPink)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancel")
                }
            },
            containerColor = VaporwaveColors.SoftLavender
        )
    }
}

@Composable
private fun PromptEditorDialog(
    promptType: PromptType,
    currentPrompt: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var editedText by remember { mutableStateOf(currentPrompt) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (promptType) {
                    PromptType.SYSTEM -> "System Prompt"
                    PromptType.AGENT -> "Agent Prompt"
                    PromptType.PERSONALITY -> "Personality Prompt"
                    PromptType.FUNCTIONS -> "Functions & Scope"
                },
                color = VaporwaveColors.NeonMagenta
            )
        },
        text = {
            Column {
                Text(
                    text = "Edit the prompt in Markdown format",
                    style = MaterialTheme.typography.bodySmall,
                    color = VaporwaveColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                AppoloTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    singleLine = false
                )
            }
        },
        confirmButton = {
            AppoloButton(
                text = "Save",
                onClick = { onSave(editedText) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = VaporwaveColors.SoftLavender
    )
}

@Composable
private fun ModelSelectorDialog(
    models: List<com.appolopocket.data.remote.llm.OllamaModel>,
    currentModel: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Model",
                color = VaporwaveColors.NeonMagenta
            )
        },
        text = {
            LazyColumn {
                items(models) { model ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(model.name) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = model.name == currentModel,
                            onClick = { onSelect(model.name) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = VaporwaveColors.NeonMagenta
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = model.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = VaporwaveColors.TextPrimary
                            )
                            Text(
                                text = formatModelSize(model.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = VaporwaveColors.TextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = VaporwaveColors.SoftLavender
    )
}

private fun formatModelSize(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    return when {
        mb >= 1024 -> String.format("%.1f GB", mb / 1024)
        else -> String.format("%.1f MB", mb)
    }
}
