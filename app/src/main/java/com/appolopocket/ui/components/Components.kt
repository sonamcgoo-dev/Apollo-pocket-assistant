package com.appolopocket.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appolopocket.domain.model.Message
import com.appolopocket.domain.model.MessageRole
import com.appolopocket.ui.theme.VaporwaveColors

@Composable
fun AppoloTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 5,
    enabled: Boolean = true,
    readOnly: Boolean = false
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .background(
                color = VaporwaveColors.GlassyPurple,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        VaporwaveColors.NeonMagenta.copy(alpha = 0.5f),
                        VaporwaveColors.ElectricCyan.copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        textStyle = LocalTextStyle.current.copy(
            color = VaporwaveColors.TextPrimary,
            textAlign = TextAlign.Start
        ),
        cursorBrush = Brush.linearGradient(
            colors = listOf(VaporwaveColors.NeonMagenta, VaporwaveColors.ElectricCyan)
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        enabled = enabled,
        readOnly = readOnly,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIcon?.invoke()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = LocalTextStyle.current.copy(
                                color = VaporwaveColors.TextTertiary
                            )
                        )
                    }
                    innerTextField()
                }
                trailingIcon?.invoke()
            }
        }
    )
}

@Composable
fun AppoloButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    isLoading: Boolean = false,
    variant: ButtonVariant = ButtonVariant.Primary
) {
    val colors = when (variant) {
        ButtonVariant.Primary -> ButtonDefaults.buttonColors(
            containerColor = VaporwaveColors.NeonMagenta,
            contentColor = VaporwaveColors.DeepNight,
            disabledContainerColor = VaporwaveColors.GlassyPurple,
            disabledContentColor = VaporwaveColors.TextTertiary
        )
        ButtonVariant.Secondary -> ButtonDefaults.buttonColors(
            containerColor = VaporwaveColors.ElectricCyan,
            contentColor = VaporwaveColors.DeepNight,
            disabledContainerColor = VaporwaveColors.GlassyPurple,
            disabledContentColor = VaporwaveColors.TextTertiary
        )
        ButtonVariant.Outline -> ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = VaporwaveColors.NeonMagenta,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = VaporwaveColors.TextTertiary
        )
        ButtonVariant.Ghost -> ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = VaporwaveColors.TextPrimary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = VaporwaveColors.TextTertiary
        )
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
        colors = colors,
        border = if (variant == ButtonVariant.Outline) {
            BorderStroke(1.dp, VaporwaveColors.NeonMagenta)
        } else null,
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = VaporwaveColors.TextPrimary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

enum class ButtonVariant {
    Primary, Secondary, Outline, Ghost
}

@Composable
fun MessageBubble(
    message: Message,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == MessageRole.USER
    val bubbleColor = if (isUser) {
        VaporwaveColors.NeonMagenta.copy(alpha = 0.2f)
    } else {
        VaporwaveColors.GlassyPurple
    }
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val textColor = VaporwaveColors.TextPrimary
    val prefix = if (isUser) "▶ " else "◀ "

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .background(
                    color = bubbleColor,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (isUser) {
                        VaporwaveColors.NeonMagenta.copy(alpha = 0.3f)
                    } else {
                        VaporwaveColors.ElectricCyan.copy(alpha = 0.3f)
                    },
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = prefix + message.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    color = textColor
                )

                // Tool calls indicator
                if (message.toolCalls.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = VaporwaveColors.NeonGreen
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${message.toolCalls.size} tool(s) called",
                            style = MaterialTheme.typography.labelSmall,
                            color = VaporwaveColors.NeonGreen
                        )
                    }
                }
            }
        }

        // Timestamp
        Text(
            text = formatTimestamp(message.timestamp),
            style = MaterialTheme.typography.labelSmall,
            color = VaporwaveColors.TextTertiary,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

@Composable
fun QuickTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    val backgroundColor = if (isActive) {
        VaporwaveColors.NeonMagenta.copy(alpha = 0.2f)
    } else {
        VaporwaveColors.GlassyPurple
    }
    val iconColor = if (isActive) {
        VaporwaveColors.NeonMagenta
    } else {
        VaporwaveColors.TextSecondary
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = if (isActive) VaporwaveColors.NeonMagenta else VaporwaveColors.GlassyPurple,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = iconColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = VaporwaveColors.TextSecondary
        )
    }
}

@Composable
fun QuickTilePanel(
    onVoiceClick: () -> Unit,
    onCameraClick: () -> Unit,
    onFilesClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(expanded) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        if (isExpanded) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickTile(
                    icon = Icons.Default.Mic,
                    label = "Voice",
                    onClick = {
                        onVoiceClick()
                        isExpanded = false
                    }
                )
                QuickTile(
                    icon = Icons.Default.CameraAlt,
                    label = "Camera",
                    onClick = {
                        onCameraClick()
                        isExpanded = false
                    }
                )
                QuickTile(
                    icon = Icons.Default.Folder,
                    label = "Files",
                    onClick = {
                        onFilesClick()
                        isExpanded = false
                    }
                )
                QuickTile(
                    icon = Icons.Default.Search,
                    label = "Search",
                    onClick = {
                        onSearchClick()
                        isExpanded = false
                    }
                )
                QuickTile(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    onClick = {
                        onSettingsClick()
                        isExpanded = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            containerColor = VaporwaveColors.NeonMagenta,
            contentColor = VaporwaveColors.DeepNight,
            shape = CircleShape
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = if (isExpanded) "Close menu" else "Open menu"
            )
        }
    }
}

@Composable
fun StatusIndicator(
    status: ConnectionStatus,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    when (status) {
                        ConnectionStatus.CONNECTED -> VaporwaveColors.NeonGreen
                        ConnectionStatus.CONNECTING -> VaporwaveColors.SunsetOrange
                        ConnectionStatus.DISCONNECTED -> VaporwaveColors.HotPink
                    }
                )
        )
        Text(
            text = status.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = VaporwaveColors.TextSecondary
        )
    }
}

enum class ConnectionStatus(val displayName: String) {
    CONNECTED("Connected"),
    CONNECTING("Connecting..."),
    DISCONNECTED("Disconnected")
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = VaporwaveColors.GlassBackground,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        VaporwaveColors.NeonMagenta.copy(alpha = 0.3f),
                        VaporwaveColors.ElectricCyan.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = VaporwaveColors.NeonMagenta
        )
        Spacer(modifier = Modifier.height(8.dp))
        GlassCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsRow(
    label: String,
    description: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingIcon?.invoke()
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = VaporwaveColors.TextPrimary
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = VaporwaveColors.TextSecondary
                )
            }
        }
        trailing?.invoke()
    }
}
