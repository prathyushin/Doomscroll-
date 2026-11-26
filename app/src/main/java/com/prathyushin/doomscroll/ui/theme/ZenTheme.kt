package com.prathyushin.doomscroll.ui.theme

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object ZenColors {
    val Charcoal = Color(0xFF101114)
    val Panel = Color(0xFF1B1A1F)
    val PanelElevated = Color(0xFF232229)
    val GlassSurface = Color(0xD91B1A1F)
    val GlassBorder = Color(0x1AFFFFFF)
    val GlassSheen = Color(0x12FFFFFF)
    val Mint = Color(0xFFB8F2D0)
    val MintDeep = Color(0xFF183A2A)
    val TextPrimary = Color(0xFFF6F6F9)
    val TextMuted = Color(0xFFAAA6B0)
    val Danger = Color(0xFFFF8F8F)
}

val LocalReducedMotion = compositionLocalOf { false }

private val ZenDarkColorScheme = darkColorScheme(
    background = ZenColors.Charcoal,
    surface = ZenColors.Panel,
    surfaceVariant = ZenColors.PanelElevated,
    primary = ZenColors.Mint,
    onPrimary = ZenColors.Charcoal,
    secondary = ZenColors.TextMuted,
    onSecondary = ZenColors.Charcoal,
    onBackground = ZenColors.TextPrimary,
    onSurface = ZenColors.TextPrimary,
    error = ZenColors.Danger
)

@Composable
fun rememberReducedMotion(context: Context = LocalContext.current): Boolean {
    return remember(context) {
        val scale = runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
        }.getOrDefault(1f)
        scale == 0f
    }
}

@Composable
fun ZenTheme(content: @Composable () -> Unit) {
    val reducedMotion = rememberReducedMotion()
    CompositionLocalProvider(LocalReducedMotion provides reducedMotion) {
        MaterialTheme(
            colorScheme = ZenDarkColorScheme,
            content = content
        )
    }
}

@Composable
fun ZenGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    padding: Dp = 18.dp,
    highlighted: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val borderColor = if (highlighted) ZenColors.Mint.copy(alpha = 0.35f) else ZenColors.GlassBorder
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            ZenColors.PanelElevated.copy(alpha = 0.92f),
            ZenColors.Panel.copy(alpha = 0.96f)
        )
    )
    Column(
        modifier = modifier
            .clip(shape)
            .background(bgBrush)
            .border(BorderStroke(1.dp, borderColor), shape)
            .padding(padding),
        content = content
    )
}
