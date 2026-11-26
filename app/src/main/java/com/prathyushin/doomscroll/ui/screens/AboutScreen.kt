package com.prathyushin.doomscroll.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prathyushin.doomscroll.ui.components.DoomEmblem
import com.prathyushin.doomscroll.ui.theme.ZenColors
import com.prathyushin.doomscroll.ui.theme.ZenGlassCard

/**
 * Liquid-glass-inspired About page (inspired by modern translucent glass interfaces such as Glisse)
 * with app identity, version, privacy philosophy, and discreet developer credits.
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
                .padding(24.dp)
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
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Version 2.0.0 Zen",
                    color = ZenColors.Mint,
                    style = MaterialTheme.typography.labelLarge,
                    letterSpacing = 0.8.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Social connection without the algorithmic undertow.",
                    color = ZenColors.TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Privacy Philosophy
        ZenGlassCard(cornerRadius = 26.dp) {
            Text(
                text = "PRIVACY PHILOSOPHY",
                color = ZenColors.Mint,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp
            )
            Spacer(Modifier.height(8.dp))
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

        // Discreet Credits
        ZenGlassCard(cornerRadius = 22.dp, padding = 16.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Crafted independently",
                    color = ZenColors.TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Built by Pranav and Prathyush",
                    color = ZenColors.TextPrimary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
