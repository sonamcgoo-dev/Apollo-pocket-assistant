package com.appolopocket.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appolopocket.ui.theme.VaporwaveColors
import kotlinx.coroutines.delay
import kotlin.random.Random

object AsciiArt {
    
    val apolloBust = """
        |        _____
        |       /     \
        |      |  o o  |
        |      |   >   |
        |      |  ___  |
        |       \_____/
        |      /|     |\
        |     / |     | \
        |    /__|     |__\
        |       |   |
        |       |   |
        |      /|   |\
        |     / |   | \
        |    /__|   |__\
    """.trimMargin()

    val loadingFrames = listOf(
        listOf("[■□□□□□□□□□]", "[■■□□□□□□□□]", "[■■■□□□□□□□]", "[■■■■□□□□□□]", 
                "[■■■■■□□□□□]", "[■■■■■■□□□□]", "[■■■■■■■□□□]", "[■■■■■■■■□□]",
                "[■■■■■■■■■□]", "[■■■■■■■■■■]"),
        listOf("◐", "◑", "◒", "◓"),
        listOf("⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏")
    )

    val welcomeMessages = listOf(
        """
        |╔═══════════════════════════════════════════════════════════╗
        |║                                                           ║
        |║   ██████╗ ███████╗██████╗ ████████╗██╗  ██╗███████╗      ║
        |║   ██╔══██╗██╔════╝██╔══██╗╚══██╔══╝██║  ██║██╔════╝      ║
        |║   ██║  ██║█████╗  ██████╔╝   ██║   ███████║█████╗        ║
        |║   ██║  ██║██╔══╝  ██╔══██╗   ██║   ██╔══██║██╔══╝        ║
        |║   ██████╔╝███████╗██║  ██║   ██║   ██║  ██║███████╗      ║
        |║   ╚═════╝ ╚══════╝╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝╚══════╝      ║
        |║                                                           ║
        |║   ██████╗  ██████╗  ██████╗ ████████╗ ██████╗  ██████╗ ██████╗ ██╗
        |║   ██╔══██╗██╔════╝ ██╔═══██╗╚══██╔══╝██╔═══██╗██╔════╝██╔═══██╗██║
        |║   ██████╔╝██║  ███╗██║   ██║   ██║   ██║   ██║██║     ██║   ██║██║
        |║   ██╔═══╝ ██║   ██║██║   ██║   ██║   ██║   ██║██║     ██║   ██║██║
        |║   ██║     ╚██████╔╝╚██████╔╝   ██║   ╚██████╔╝╚██████╗╚██████╔╝███████╗
        |║   ╚═╝      ╚═════╝  ╚═════╝    ╚═╝    ╚═════╝  ╚═════╝ ╚═════╝ ╚══════╝
        |║                                                           ║
        |║   ★ · . ✦ · ★  · . ✦ · ★ · . ✦  · ★ · . ✦ · ★  · . ✦ · ★  · . ✦   ║
        |║                                                           ║
        |╚═══════════════════════════════════════════════════════════╝
        """.trimMargin(),
        """
        |    _____  ____   ___  __  __  ___  _  _  ___ 
        |   / ____)(  _ \\ / __)(  )(  )/ __)( )( )(  _ \
        |  ( (___   )   /( (__  )(__)( \\__ \\)( )(  ) _ (
        |   \\___ \\ (__)  \\___)(______)(___/(__)_)(_)   (_)
        |       _)_(_  _  ___  _  _  ___  ___  ___  ___ 
        |      (  )( )(  _)(  )( )( )(  _)(  _)/ __)/ __)
        |       )(  )(  )(   )(  )(  \\ \\(  _)( (_ \\__ \\
        |      (__)(__)(_)   (__)(__)  \\_\\__)(\\___)(___/
        """.trimMargin(),
        """
        |    ▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄
        |    █                                           █
        |    █   ╔═══════════════════════════════════╗   █
        |    █   ║   Welcome to the DIGITAL FRONTIER   ║   █
        |    █   ╚═══════════════════════════════════╝   █
        |    █                                           █
        |    █         ╭──────────────────────╮          █
        |    █         │  ◢██████████████◣   │          █
        |    █         │  █ A P P O L O █     │          █
        |    █         │  █   P O C K E T   █ │          █
        |    █         │  ◥██████████████◤   │          █
        |    █         ╰──────────────────────╯          █
        |    █                                           █
        |    ▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀
        """.trimMargin()
    )

    val modeTransitions = mapOf(
        "code" to """
            |╔════════════════════════════════════════╗
            |║  ⚡ ENTERING CODE EXECUTION MODE ⚡      ║
            |╚════════════════════════════════════════╝
        """.trimMargin(),
        "chat" to """
            |╔════════════════════════════════════════╗
            |║  💬 ENTERING CONVERSATION MODE 💬       ║
            |╚════════════════════════════════════════╝
        """.trimMargin(),
        "automation" to """
            |╔════════════════════════════════════════╗
            |║  🤖 ENTERING AUTOMATION MODE 🤖         ║
            |╚════════════════════════════════════════╝
        """.trimMargin(),
        "search" to """
            |╔════════════════════════════════════════╗
            |║  🔍 ENTERING SEARCH MODE 🔍             ║
            |╚════════════════════════════════════════╝
        """.trimMargin()
    )

    val successArt = """
        |   ╔════════════════════════════╗
        |   ║     ✓ TASK COMPLETE! ✓      ║
        |   ╚════════════════════════════╝
        |         \\   |   |   |   |   /
        |          \\  ★   ★   ★  /
        |           \\   \\ | /   /
        |            \\   ★   /
        |             \\ /|\\ /
        |              ★  ★
    """.trimMargin()

    val errorArt = """
        |   ╔════════════════════════════╗
        |   ║     ✗ ERROR ENCOUNTERED ✗   ║
        |   ╚════════════════════════════╝
        |              /\\
        |             /  \\
        |            /    \\
        |           /  !!  \\
        |          /________\\
    """.trimMargin()

    val divider = "─".repeat(50)

    val sunDecorations = listOf(
        "  \\  |  /  ",
        " ─── ─── ",
        "  /  |  \\  ",
        "  ___  ___  ",
        " /   \\/   \\ "
    )
}

@Composable
fun AnimatedAsciiText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = VaporwaveColors.NeonMagenta,
    animate: Boolean = true
) {
    var displayedText by remember { mutableStateOf("") }
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(text) {
        displayedText = ""
        currentIndex = 0
        while (currentIndex < text.length) {
            displayedText = text.substring(0, currentIndex + 1)
            currentIndex++
            if (animate) delay(15)
        }
    }

    Text(
        text = displayedText,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            lineHeight = 12.sp
        ),
        color = color
    )
}

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    style: Int = 0
) {
    var frame by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            frame = (frame + 1) % AsciiArt.loadingFrames[style].size
        }
    }

    Text(
        text = AsciiArt.loadingFrames[style][frame],
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace
        ),
        color = VaporwaveColors.ElectricCyan
    )
}

@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    delayMs: Long = 30
) {
    var displayedText by remember { mutableStateOf("") }
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(text) {
        displayedText = ""
        currentIndex = 0
        while (currentIndex < text.length) {
            displayedText = text.substring(0, currentIndex + 1)
            currentIndex++
            delay(delayMs)
        }
    }

    Text(
        text = displayedText,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace
        ),
        color = VaporwaveColors.TextPrimary
    )
}

@Composable
fun VaporwaveBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scroll"
    )

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        // Base gradient background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    VaporwaveColors.DeepNight,
                    VaporwaveColors.SoftLavender,
                    VaporwaveColors.DeepNight
                )
            )
        )

        // Grid lines
        val gridSize = 50.dp.toPx()
        val gridColor = VaporwaveColors.NeonMagenta.copy(alpha = 0.1f)

        // Vertical lines
        var x = (animatedOffset % gridSize)
        while (x < size.width) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += gridSize
        }

        // Horizontal lines
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += gridSize
        }
    }
}

@Composable
fun GlowingBorder(
    modifier: Modifier = Modifier,
    color: Color = VaporwaveColors.NeonMagenta,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier
            .background(
                color = VaporwaveColors.GlassBackground,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRoundRect(
                color = color.copy(alpha = glowAlpha),
                style = Stroke(width = 2f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
            )
        }
        content()
    }
}

@Composable
fun NeonText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = VaporwaveColors.NeonMagenta
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neon")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neonGlow"
    )

    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium,
        color = color.copy(alpha = glowAlpha)
    )
}

@Composable
fun AsciiDivider(
    modifier: Modifier = Modifier,
    char: String = "─",
    length: Int = 50
) {
    Text(
        text = char.repeat(length),
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace
        ),
        color = VaporwaveColors.NeonMagenta.copy(alpha = 0.5f),
        textAlign = TextAlign.Center
    )
}
