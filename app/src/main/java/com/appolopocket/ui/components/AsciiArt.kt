package com.appolopocket.ui.components

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
import kotlin.random.Random

object AsciiArt {
    private val kanjiSet = listOf("愛", "夢", "月", "光", "波", "夜", "神", "空", "海", "星", "幻", "電", "風")
    private val skylineSet = listOf("▁▂▃▄▅▆▇█", "█▇▆▅▄▃▂▁", "▂▄▆█▆▄▂", "▁▃▅▇█▇▅▃")
    private val checkerSet = listOf("▓░", "░▓", "▞▚", "▚▞", "◢◤", "◥◣")

    fun generateVaporwaveStartupArt(randomSeed: Long = System.currentTimeMillis()): String {
        val random = Random(randomSeed)
        val headerKanji = (1..8).joinToString(" ") { kanjiSet.random(random) }
        val footerKanji = (1..8).joinToString(" ") { kanjiSet.random(random) }
        val skyline = skylineSet.random(random)

        val checkerRows = (0..5).joinToString("\n") { row ->
            val pair = checkerSet[(row + random.nextInt(checkerSet.size)) % checkerSet.size]
            buildString {
                append(if (row % 2 == 0) "  " else "")
                repeat(24) { append(pair[it % pair.length]) }
            }
        }

        val statue = """
            |            .-====-.
            |          .'  _  _  '.
            |         /   (o)(o)   \
            |        |      /\      |
            |        |   .-====-.   |
            |         \  \______/  /
            |       .-'.________.'-.
            |      /  /|  APOLLO |\  \
            |     /__/ |   BUST  | \__\
        """.trimMargin()

        return """
            |┌──────────────────────────────────────────────┐
            |│  $headerKanji  │
            |├──────────────────────────────────────────────┤
            |$checkerRows
            |$statue
            |      ${skyline.repeat(4).take(36)}
            |├──────────────────────────────────────────────┤
            |│  $footerKanji  │
            |└──────────────────────────────────────────────┘
        """.trimMargin()
    }

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
    animate: Boolean = false
) {
    Text(
        text = text,
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
    Text(
        text = "[SYSTEM BUSY]",
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
    Text(
        text = text,
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
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    VaporwaveColors.DeepNight,
                    VaporwaveColors.SoftLavender,
                    VaporwaveColors.DeepNight
                )
            )
        )

        val tileSize = 28.dp.toPx()
        var row = 0
        var y = 0f
        while (y < size.height + tileSize) {
            val rowOffset = ((row % 4) - 2) * (tileSize / 3f)
            var col = 0
            var x = rowOffset
            while (x < size.width + tileSize) {
                val checkerColor = if ((row + col) % 2 == 0) {
                    VaporwaveColors.NeonMagenta.copy(alpha = 0.07f)
                } else {
                    VaporwaveColors.ElectricCyan.copy(alpha = 0.05f)
                }
                drawRect(
                    color = checkerColor,
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(tileSize, tileSize)
                )
                x += tileSize
                col++
            }
            y += tileSize
            row++
        }
    }
}

@Composable
fun GlowingBorder(
    modifier: Modifier = Modifier,
    color: Color = VaporwaveColors.NeonMagenta,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = VaporwaveColors.GlassBackground,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRoundRect(
                color = color.copy(alpha = 0.55f),
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
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium,
        color = color.copy(alpha = 0.95f)
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
