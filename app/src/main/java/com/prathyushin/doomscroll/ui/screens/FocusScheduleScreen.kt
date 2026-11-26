package com.prathyushin.doomscroll.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prathyushin.doomscroll.model.FocusEvaluation
import com.prathyushin.doomscroll.model.FocusSchedule
import com.prathyushin.doomscroll.ui.theme.ZenColors
import com.prathyushin.doomscroll.ui.theme.ZenGlassCard
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun FocusScheduleScreen(
    schedules: List<FocusSchedule>,
    evaluation: FocusEvaluation,
    onUpdateSchedules: (List<FocusSchedule>) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(
            title = "Smart Focus Schedule",
            subtitle = if (evaluation.isFocusActive) "Active now: ${evaluation.summaryLabel}" else "Schedule stricter Reels & session rules",
            onBack = onBack
        )

        schedules.forEach { schedule ->
            ZenGlassCard(highlighted = schedule.enabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = schedule.label,
                            color = ZenColors.TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val overnightTag = if (schedule.isOvernight) " (Overnight)" else ""
                        Text(
                            text = "${formatTime(schedule.startHour, schedule.startMinute)} – ${formatTime(schedule.endHour, schedule.endMinute)}$overnightTag",
                            color = ZenColors.Mint,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Switch(
                        checked = schedule.enabled,
                        onCheckedChange = { checked ->
                            onUpdateSchedules(schedules.map {
                                if (it.id == schedule.id) it.copy(enabled = checked) else it
                            })
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ZenColors.Charcoal,
                            checkedTrackColor = ZenColors.Mint
                        )
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Quick time adjusters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val nextStart = (schedule.startHour + 1) % 24
                            onUpdateSchedules(schedules.map {
                                if (it.id == schedule.id) it.copy(startHour = nextStart) else it
                            })
                        },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp),
                        border = BorderStroke(1.dp, ZenColors.GlassBorder),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Start +1h", color = ZenColors.TextPrimary)
                    }
                    OutlinedButton(
                        onClick = {
                            val nextEnd = (schedule.endHour + 1) % 24
                            onUpdateSchedules(schedules.map {
                                if (it.id == schedule.id) it.copy(endHour = nextEnd) else it
                            })
                        },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp),
                        border = BorderStroke(1.dp, ZenColors.GlassBorder),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("End +1h", color = ZenColors.TextPrimary)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Weekday chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DayOfWeek.entries.forEach { day ->
                        val active = day in schedule.daysOfWeek
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (active) ZenColors.Mint else ZenColors.PanelElevated)
                                .border(1.dp, ZenColors.GlassBorder, RoundedCornerShape(12.dp))
                                .clickable(role = Role.Checkbox) {
                                    val updatedDays = if (active) schedule.daysOfWeek - day else schedule.daysOfWeek + day
                                    onUpdateSchedules(schedules.map {
                                        if (it.id == schedule.id) it.copy(daysOfWeek = updatedDays) else it
                                    })
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                                color = if (active) ZenColors.Charcoal else ZenColors.TextMuted,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Block all Reels (even from DMs)",
                        color = ZenColors.TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = schedule.strictReelsBlock,
                        onCheckedChange = { strict ->
                            onUpdateSchedules(schedules.map {
                                if (it.id == schedule.id) it.copy(strictReelsBlock = strict) else it
                            })
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ZenColors.Charcoal,
                            checkedTrackColor = ZenColors.Mint
                        )
                    )
                }
            }
        }

        Button(
            onClick = {
                val nextId = "custom_${System.currentTimeMillis()}"
                val newSchedule = FocusSchedule(
                    id = nextId,
                    label = "Custom Focus ${schedules.size + 1}",
                    enabled = true,
                    startHour = 20,
                    startMinute = 0,
                    endHour = 23,
                    endMinute = 0,
                    daysOfWeek = DayOfWeek.entries.toSet(),
                    strictReelsBlock = true,
                    overrideSessionLimitMinutes = 10
                )
                onUpdateSchedules(schedules + newSchedule)
            },
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ZenColors.Mint,
                contentColor = ZenColors.Charcoal
            )
        ) {
            Text("Add focus schedule", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun ScreenHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        TextButton(
            onClick = onBack,
            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
        ) {
            Text("← Back to Zen", color = ZenColors.Mint, fontWeight = FontWeight.SemiBold)
        }
        Text(
            text = title,
            color = ZenColors.TextPrimary,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            color = ZenColors.TextMuted,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatTime(hour: Int, minute: Int): String =
    String.format(Locale.US, "%02d:%02d", hour, minute)
