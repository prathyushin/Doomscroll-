package com.prathyushin.doomscroll.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.prathyushin.doomscroll.ui.theme.ZenColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * Recognizable two-color vector emblem (#101114 charcoal & #B8F2D0 mint):
 * an open Zen Enso ring breaking the infinite scroll loop, accented by a
 * focal dot and sculpted dual pause pillars.
 */
@Composable
fun DoomEmblem(
    modifier: Modifier = Modifier,
    contentDescriptionText: String = "Doom Scroll Zen emblem"
) {
    Canvas(
        modifier = modifier
            .semantics { contentDescription = contentDescriptionText }
            .clip(CircleShape)
            .background(ZenColors.Charcoal)
            .border(1.dp, ZenColors.Mint.copy(alpha = 0.25f), CircleShape)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.30f
        val stroke = size.minDimension * 0.056f

        // Open Enso arc (gap centered at -45 deg / top-right)
        drawArc(
            color = ZenColors.Mint,
            startAngle = -14f,
            sweepAngle = 298f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )

        // Intentional focal dot inside the top-right break
        val dotAngleRad = Math.toRadians(-45.0)
        val dotCenter = Offset(
            x = center.x + (radius * cos(dotAngleRad)).toFloat(),
            y = center.y + (radius * sin(dotAngleRad)).toFloat()
        )
        drawCircle(
            color = ZenColors.Mint,
            radius = stroke * 0.65f,
            center = dotCenter
        )

        // Sculpted dual Zen pause pillars
        val pillarHalfH = radius * 0.38f
        val pillarOffsetX = radius * 0.28f
        val pillarStroke = size.minDimension * 0.068f

        drawLine(
            color = ZenColors.Mint,
            start = Offset(center.x - pillarOffsetX, center.y - pillarHalfH),
            end = Offset(center.x - pillarOffsetX, center.y + pillarHalfH),
            strokeWidth = pillarStroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = ZenColors.Mint,
            start = Offset(center.x + pillarOffsetX, center.y - pillarHalfH),
            end = Offset(center.x + pillarOffsetX, center.y + pillarHalfH),
            strokeWidth = pillarStroke,
            cap = StrokeCap.Round
        )
    }
}
