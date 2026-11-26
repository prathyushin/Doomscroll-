package com.prathyushin.doomscroll.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prathyushin.doomscroll.ui.theme.ZenColors
import com.prathyushin.doomscroll.ui.theme.ZenGlassCard

@Composable
fun GentlePauseOverlay(
    reason: String,
    sessionMinutes: Long,
    onContinueIntentionally: () -> Unit,
    onReturnToZen: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ZenColors.Charcoal.copy(alpha = 0.86f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        ZenGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 28.dp,
            padding = 24.dp,
            highlighted = true
        ) {
            DoomEmblem(modifier = Modifier.size(52.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                text = "GENTLE PAUSE",
                color = ZenColors.Mint,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Take a quiet breath",
                color = ZenColors.TextPrimary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "$reason (${sessionMinutes}m this session). You can step back to Zen or continue with intention.",
                color = ZenColors.TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(22.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onReturnToZen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ZenColors.Mint,
                        contentColor = ZenColors.Charcoal
                    )
                ) {
                    Text("Return to Zen", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onContinueIntentionally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Continue intentionally")
                }
            }
        }
    }
}
