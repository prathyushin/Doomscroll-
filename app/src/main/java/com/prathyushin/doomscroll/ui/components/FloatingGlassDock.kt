package com.prathyushin.doomscroll.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prathyushin.doomscroll.model.ZenDestination
import com.prathyushin.doomscroll.ui.theme.LocalReducedMotion
import com.prathyushin.doomscroll.ui.theme.ZenColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FloatingGlassDock(
    currentDestination: ZenDestination,
    visible: Boolean,
    sessionMinutesLabel: String?,
    focusActive: Boolean,
    onSelectDestination: (ZenDestination) -> Unit,
    onRevealDock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reducedMotion = LocalReducedMotion.current
    val animDuration = if (reducedMotion) 0 else 220

    val settingsSelected = currentDestination != ZenDestination.INSTAGRAM &&
        currentDestination != ZenDestination.THREADS

    Box(
        modifier = modifier.navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(tween(animDuration)) { it / 2 } + fadeIn(tween(animDuration)),
            exit = slideOutVertically(tween(animDuration)) { it / 2 } + fadeOut(tween(animDuration))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(ZenColors.GlassSurface)
                    .border(BorderStroke(1.dp, ZenColors.GlassBorder), RoundedCornerShape(30.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DockItem(
                    label = "Instagram",
                    selected = currentDestination == ZenDestination.INSTAGRAM,
                    onClick = { onSelectDestination(ZenDestination.INSTAGRAM) }
                )
                DockItem(
                    label = "Threads",
                    selected = currentDestination == ZenDestination.THREADS,
                    onClick = { onSelectDestination(ZenDestination.THREADS) }
                )
                SettingsGearDockButton(
                    selected = settingsSelected,
                    focusActive = focusActive,
                    onClick = { onSelectDestination(ZenDestination.DASHBOARD) }
                )
                if (!sessionMinutesLabel.isNullOrBlank()) {
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = sessionMinutesLabel,
                        color = ZenColors.TextMuted,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !visible,
            enter = fadeIn(tween(animDuration)),
            exit = fadeOut(tween(animDuration))
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .defaultMinSize(minWidth = 68.dp, minHeight = 24.dp)
                    .clip(CircleShape)
                    .background(ZenColors.GlassSurface.copy(alpha = 0.72f))
                    .border(1.dp, ZenColors.GlassBorder, CircleShape)
                    .clickable(role = Role.Button, onClick = onRevealDock)
                    .semantics { contentDescription = "Show navigation dock" }
                    .padding(horizontal = 16.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(ZenColors.Mint.copy(alpha = 0.65f))
                )
            }
        }
    }
}

@Composable
private fun DockItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (selected) ZenColors.Mint else Color.Transparent
    val textColor = if (selected) ZenColors.Charcoal else ZenColors.TextPrimary

    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = 46.dp)
            .clip(RoundedCornerShape(23.dp))
            .background(bgColor)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun SettingsGearDockButton(
    selected: Boolean,
    focusActive: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (selected) ZenColors.Mint else Color.Transparent
    val iconColor = if (selected) ZenColors.Charcoal else ZenColors.TextPrimary

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { contentDescription = "Settings" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerR = size.minDimension * 0.44f
            val innerR = size.minDimension * 0.28f
            val stroke = size.minDimension * 0.11f

            // Center ring
            drawCircle(
                color = iconColor,
                radius = innerR,
                center = center,
                style = Stroke(width = stroke)
            )
            // 8 clean gear teeth
            for (i in 0 until 8) {
                val angle = Math.toRadians(i * 45.0)
                val start = Offset(
                    center.x + (innerR * 1.15f * cos(angle)).toFloat(),
                    center.y + (innerR * 1.15f * sin(angle)).toFloat()
                )
                val end = Offset(
                    center.x + (outerR * cos(angle)).toFloat(),
                    center.y + (outerR * sin(angle)).toFloat()
                )
                drawLine(
                    color = iconColor,
                    start = start,
                    end = end,
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
        }
        if (focusActive && !selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(ZenColors.Mint)
            )
        }
    }
}
