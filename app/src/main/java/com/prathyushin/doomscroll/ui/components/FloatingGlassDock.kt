package com.prathyushin.doomscroll.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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

/**
 * Compact 2-slot liquid-glass floating dock (Instagram + Settings Gear):
 * - Starts hidden as a minimal bottom handle while browsing Instagram.
 * - Tapping the handle reveals the gliding refractive glass lens pill and auto-hides after 3s.
 */
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

    val activeIndex = if (currentDestination == ZenDestination.INSTAGRAM) 0 else 1

    val itemWidth = 108.dp
    val trackPadding = 6.dp
    val targetOffset = itemWidth * activeIndex

    val lensOffset by animateDpAsState(
        targetValue = targetOffset,
        animationSpec = if (reducedMotion) {
            tween(0)
        } else {
            spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        },
        label = "dockLensOffset"
    )

    Box(
        modifier = modifier.navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(tween(animDuration)) { it / 2 } + fadeIn(tween(animDuration)),
            exit = slideOutVertically(tween(animDuration)) { it / 2 } + fadeOut(tween(animDuration))
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Dark obsidian pill track
                Box(
                    modifier = Modifier
                        .width(itemWidth * 2 + trackPadding * 2)
                        .height(62.dp)
                        .clip(RoundedCornerShape(31.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xEB121418),
                                    Color(0xF508090C)
                                )
                            )
                        )
                        .border(
                            BorderStroke(
                                1.dp,
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.24f),
                                        Color.White.copy(alpha = 0.06f),
                                        Color.White.copy(alpha = 0.16f)
                                    )
                                )
                            ),
                            RoundedCornerShape(31.dp)
                        )
                )

                // Gliding refractive liquid-glass lens around the active item
                Box(
                    modifier = Modifier
                        .padding(start = trackPadding)
                        .offset(x = lensOffset)
                        .width(itemWidth)
                        .height(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.22f),
                                    Color.White.copy(alpha = 0.07f),
                                    ZenColors.Mint.copy(alpha = 0.14f)
                                )
                            )
                        )
                        .border(
                            BorderStroke(
                                1.25.dp,
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.65f),
                                        Color.White.copy(alpha = 0.10f),
                                        ZenColors.Mint.copy(alpha = 0.55f)
                                    )
                                )
                            ),
                            RoundedCornerShape(27.dp)
                        )
                )

                // Dock items row (Instagram + Settings Gear)
                Row(
                    modifier = Modifier
                        .padding(horizontal = trackPadding)
                        .height(62.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DockSlot(
                        width = itemWidth,
                        label = if (!sessionMinutesLabel.isNullOrBlank() && activeIndex == 0) {
                            "Instagram • $sessionMinutesLabel"
                        } else {
                            "Instagram"
                        },
                        selected = activeIndex == 0,
                        onClick = { onSelectDestination(ZenDestination.INSTAGRAM) }
                    ) { tint ->
                        InstagramGlyph(tint = tint, modifier = Modifier.size(20.dp))
                    }

                    DockSlot(
                        width = itemWidth,
                        label = "Settings",
                        selected = activeIndex == 1,
                        showFocusDot = focusActive,
                        onClick = { onSelectDestination(ZenDestination.DASHBOARD) }
                    ) { tint ->
                        SettingsGearGlyph(tint = tint, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // Minimal frosted bottom handle when tucked away
        AnimatedVisibility(
            visible = !visible,
            enter = fadeIn(tween(animDuration)),
            exit = fadeOut(tween(animDuration))
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .defaultMinSize(minWidth = 76.dp, minHeight = 26.dp)
                    .clip(CircleShape)
                    .background(Color(0xCC111318))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.26f),
                                ZenColors.Mint.copy(alpha = 0.38f),
                                Color.White.copy(alpha = 0.26f)
                            )
                        ),
                        CircleShape
                    )
                    .clickable(role = Role.Button, onClick = onRevealDock)
                    .semantics { contentDescription = "Show navigation dock" }
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(30.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(ZenColors.Mint.copy(alpha = 0.80f))
                )
            }
        }
    }
}

@Composable
private fun DockSlot(
    width: androidx.compose.ui.unit.Dp,
    label: String,
    selected: Boolean,
    showFocusDot: Boolean = false,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit
) {
    val tint = if (selected) Color.White else Color.White.copy(alpha = 0.45f)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .width(width)
            .height(56.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box {
                icon(tint)
                if (showFocusDot) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-2).dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(ZenColors.Mint)
                    )
                }
            }
            if (selected) {
                Text(
                    text = label,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun InstagramGlyph(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.11f
        val pad = stroke
        drawRoundRect(
            color = tint,
            topLeft = Offset(pad, pad),
            size = Size(size.width - pad * 2, size.height - pad * 2),
            cornerRadius = CornerRadius(size.minDimension * 0.28f),
            style = Stroke(width = stroke)
        )
        drawCircle(
            color = tint,
            radius = size.minDimension * 0.21f,
            center = Offset(size.width / 2f, size.height / 2f),
            style = Stroke(width = stroke)
        )
        drawCircle(
            color = tint,
            radius = stroke * 0.75f,
            center = Offset(size.width * 0.73f, size.height * 0.27f)
        )
    }
}

@Composable
private fun SettingsGearGlyph(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerR = size.minDimension * 0.44f
        val innerR = size.minDimension * 0.26f
        val stroke = size.minDimension * 0.11f

        drawCircle(
            color = tint,
            radius = innerR,
            center = center,
            style = Stroke(width = stroke)
        )
        for (i in 0 until 8) {
            val angle = Math.toRadians(i * 45.0)
            drawLine(
                color = tint,
                start = Offset(
                    center.x + (innerR * 1.15f * cos(angle)).toFloat(),
                    center.y + (innerR * 1.15f * sin(angle)).toFloat()
                ),
                end = Offset(
                    center.x + (outerR * cos(angle)).toFloat(),
                    center.y + (outerR * sin(angle)).toFloat()
                ),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}
