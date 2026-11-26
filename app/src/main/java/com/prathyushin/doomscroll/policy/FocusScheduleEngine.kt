package com.prathyushin.doomscroll.policy

import com.prathyushin.doomscroll.model.FocusEvaluation
import com.prathyushin.doomscroll.model.FocusSchedule
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Evaluates FocusSchedules accurately across same-day and overnight windows,
 * weekday boundaries, timezones, and system clock changes.
 */
class FocusScheduleEngine {

    fun evaluate(
        schedules: List<FocusSchedule>,
        nowInstant: Instant = Instant.now(),
        defaultZoneId: ZoneId = ZoneId.systemDefault()
    ): FocusEvaluation {
        val active = schedules.filter { schedule ->
            schedule.enabled && isScheduleActiveAt(schedule, nowInstant, defaultZoneId)
        }
        if (active.isEmpty()) {
            return FocusEvaluation(isFocusActive = false, summaryLabel = "Off-schedule")
        }

        val strictReels = active.any { it.strictReelsBlock }
        val minLimit = active.mapNotNull { it.overrideSessionLimitMinutes }.minOrNull()
        val maxCooldown = active.mapNotNull { it.overrideCooldownMinutes }.maxOrNull()
        val label = if (active.size == 1) active.first().label else "${active.size} focus windows active"

        return FocusEvaluation(
            isFocusActive = true,
            activeSchedules = active,
            strictReelsBlock = strictReels,
            effectiveSessionLimitMinutes = minLimit,
            effectiveCooldownMinutes = maxCooldown,
            summaryLabel = label
        )
    }

    fun isScheduleActiveAt(
        schedule: FocusSchedule,
        nowInstant: Instant,
        defaultZoneId: ZoneId = ZoneId.systemDefault()
    ): Boolean {
        if (!schedule.enabled || schedule.daysOfWeek.isEmpty()) return false

        val zone = schedule.timezoneId?.let { id ->
            runCatching { ZoneId.of(id) }.getOrNull()
        } ?: defaultZoneId

        val zonedNow: ZonedDateTime = nowInstant.atZone(zone)
        val currentTime = zonedNow.toLocalTime()
        val today = zonedNow.dayOfWeek
        val yesterday = today.minus(1)

        val start = LocalTime.of(schedule.startHour.coerceIn(0, 23), schedule.startMinute.coerceIn(0, 59))
        val end = LocalTime.of(schedule.endHour.coerceIn(0, 23), schedule.endMinute.coerceIn(0, 59))

        return if (start < end) {
            // Same-day window: [start, end)
            today in schedule.daysOfWeek && !currentTime.isBefore(start) && currentTime.isBefore(end)
        } else if (start > end) {
            // Overnight window: [start, 24:00) on scheduled day OR [00:00, end) if yesterday was scheduled
            val inEveningPart = today in schedule.daysOfWeek && !currentTime.isBefore(start)
            val inMorningPart = yesterday in schedule.daysOfWeek && currentTime.isBefore(end)
            inEveningPart || inMorningPart
        } else {
            // start == end means 24-hour active on scheduled days
            today in schedule.daysOfWeek
        }
    }
}
