package com.prathyushin.doomscroll.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prathyushin.doomscroll.ui.components.DoomEmblem
import com.prathyushin.doomscroll.ui.theme.ZenColors
import com.prathyushin.doomscroll.ui.theme.ZenGlassCard

/**
 * Liquid-glass-inspired About page with app identity, version tag,
 * privacy philosophy, and refined, spacious developer credits.
 */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ScreenHeader(
            title = "About",
            subtitle = "Identity, philosophy, and craft.",
            onBack = onBack
        )

        // Hero Liquid-Glass Identity Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF22252A),
                            ZenColors.Panel,
                            Color(0xFF15191A)
                        )
                    )
                )
                .border(
                    BorderStroke(
                        1.dp,
                        Brush.verticalGradient(
                            colors = listOf(
                                ZenColors.Mint.copy(alpha = 0.35f),
                                ZenColors.GlassBorder
                            )
                        )
                    ),
                    RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DoomEmblem(modifier = Modifier.size(76.dp))
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Doom Scroll",
                    color = ZenColors.TextPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(ZenColors.Mint.copy(alpha = 0.12f))
                        .border(1.dp, ZenColors.Mint.copy(alpha = 0.28f), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Version 2.1.3 Zen",
                        color = ZenColors.Mint,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.6.sp
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Social connection without the algorithmic undertow.",
                    color = ZenColors.TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Privacy Philosophy
        ZenGlassCard(cornerRadius = 26.dp, padding = 20.dp) {
            Text(
                text = "PRIVACY PHILOSOPHY",
                color = ZenColors.Mint,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Doom Scroll runs entirely on your device. There are no accounts, no analytics pipelines, no ad profiles, and no remote servers reading your messages or feeds.",
                color = ZenColors.TextPrimary,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Calm technology should respect your attention and your privacy equally—never feeling like punishment.",
                color = ZenColors.TextMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Refined, spacious developer credits card (no overlapping or cramped text)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ZenColors.PanelElevated.copy(alpha = 0.85f),
                            ZenColors.Panel.copy(alpha = 0.95f)
                        )
                    )
                )
                .border(1.dp, ZenColors.GlassBorder, RoundedCornerShape(26.dp))
                .padding(horizontal = 24.dp, vertical = 22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(2.dp)
                        .clip(CircleShape)
                        .background(ZenColors.Mint.copy(alpha = 0.6f))
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "CRAFTED INDEPENDENTLY",
                    color = ZenColors.TextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.4.sp
                )
                Text(
                    text = "Built by Pranav and Prathyush",
                    color = ZenColors.TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
