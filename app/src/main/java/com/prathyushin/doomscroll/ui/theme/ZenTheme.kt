package com.prathyushin.doomscroll.ui.theme

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object ZenColors {
    val Charcoal = Color(0xFF101114)
    val Obsidian = Color(0xFF0A0B0E)
    val Panel = Color(0xFF1B1A1F)
    val PanelElevated = Color(0xFF23242B)
    val GlassFillTop = Color(0x28FFFFFF)
    val GlassFillBottom = Color(0x0DFFFFFF)
    val GlassSurface = Color(0xCC16181D)
    val GlassBorderBright = Color(0x52FFFFFF)
    val GlassBorder = Color(0x22FFFFFF)
    val GlassBorderSoft = Color(0x12FFFFFF)
    val Mint = Color(0xFFB8F2D0)
    val MintDeep = Color(0xFF183A2A)
    val MintMist = Color(0xFF2A4D3E)
    val TextPrimary = Color(0xFFF6F7FA)
    val TextMuted = Color(0xFFA7ACB5)
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

/**
 * Calm dark charcoal canvas with subtle deep-sage ambient light pools
 * so frosted liquid-glass surfaces catch specular depth cleanly.
 */
@Composable
fun ZenAmbientBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ZenColors.Charcoal)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Subtle top-right misty mint luminance
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ZenColors.MintMist.copy(alpha = 0.42f),
                        ZenColors.MintDeep.copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.85f, size.height * 0.12f),
                    radius = size.maxDimension * 0.52f
                ),
                radius = size.maxDimension * 0.52f,
                center = Offset(size.width * 0.85f, size.height * 0.12f)
            )
            // Soft bottom-left cool slate depth
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF233238).copy(alpha = 0.34f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.15f, size.height * 0.78f),
                    radius = size.maxDimension * 0.48f
                ),
                radius = size.maxDimension * 0.48f,
                center = Offset(size.width * 0.15f, size.height * 0.78f)
            )
        }
        content()
    }
}

/**
 * Liquid-glass frosted card with diagonal specular rim refraction.
 */
@Composable
fun ZenGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    padding: Dp = 18.dp,
    highlighted: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val rimBrush = Brush.linearGradient(
        colors = if (highlighted) {
            listOf(
                ZenColors.Mint.copy(alpha = 0.50f),
                ZenColors.GlassBorderSoft,
                ZenColors.GlassBorderBright.copy(alpha = 0.32f)
            )
        } else {
            listOf(
                ZenColors.GlassBorderBright.copy(alpha = 0.38f),
                ZenColors.GlassBorderSoft,
                ZenColors.GlassBorder.copy(alpha = 0.28f)
            )
        }
    )
    val surfaceBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF282D32).copy(alpha = 0.62f),
            ZenColors.Panel.copy(alpha = 0.72f),
            Color(0xFF15181C).copy(alpha = 0.78f)
        )
    )

    Column(
        modifier = modifier
            .clip(shape)
            .background(surfaceBrush)
            .border(BorderStroke(1.dp, rimBrush), shape)
            .padding(padding),
        content = content
    )
}

/**
 * Individual frosted capsule settings row with circular frosted icon badge and chevron.
 */
@Composable
fun ZenGlassPillRow(
    title: String,
    subtitle: String? = null,
    badgeGlyph: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    val rimBrush = Brush.linearGradient(
        colors = listOf(
            ZenColors.GlassBorderBright.copy(alpha = 0.34f),
            ZenColors.GlassBorderSoft,
            ZenColors.GlassBorder.copy(alpha = 0.24f)
        )
    )
    val fillBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF262B30).copy(alpha = 0.64f),
            Color(0xFF1A1D22).copy(alpha = 0.72f)
        )
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 62.dp)
            .clip(shape)
            .background(fillBrush)
            .border(BorderStroke(1.dp, rimBrush), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
                .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badgeGlyph,
                color = ZenColors.Mint,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = ZenColors.TextPrimary,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = ZenColors.TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Text(
            text = "›",
            color = ZenColors.TextMuted,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Normal
        )
    }
}

/**
 * Compact frosted metric tile (inspired by 3-up translucent glass stat pills).
 */
@Composable
fun ZenGlassMetricPill(
    label: String,
    value: String,
    accent: Boolean = false,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(22.dp)
    val rimBrush = Brush.linearGradient(
        colors = listOf(
            if (accent) ZenColors.Mint.copy(alpha = 0.45f) else ZenColors.GlassBorderBright.copy(alpha = 0.34f),
            ZenColors.GlassBorderSoft
        )
    )
    Column(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF282E33).copy(alpha = 0.60f),
                        Color(0xFF171A1F).copy(alpha = 0.72f)
                    )
                )
            )
            .border(BorderStroke(1.dp, rimBrush), shape)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            color = ZenColors.TextMuted,
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = value,
            color = if (accent) ZenColors.Mint else ZenColors.TextPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
