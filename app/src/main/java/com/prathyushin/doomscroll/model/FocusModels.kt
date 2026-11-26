package com.prathyushin.doomscroll.model

import java.time.DayOfWeek

data class FocusSchedule(
    val id: String,
    val label: String,
    val enabled: Boolean = true,
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val endHour: Int = 17,
    val endMinute: Int = 0,
    val daysOfWeek: Set<DayOfWeek> = setOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY
    ),
    val strictReelsBlock: Boolean = true,
    val overrideSessionLimitMinutes: Int? = 10,
    val overrideCooldownMinutes: Int? = 15,
    val timezoneId: String? = null
) {
    val isOvernight: Boolean
        get() = (startHour * 60 + startMinute) >= (endHour * 60 + endMinute)
}

data class FocusEvaluation(
    val isFocusActive: Boolean,
    val activeSchedules: List<FocusSchedule> = emptyList(),
    val strictReelsBlock: Boolean = false,
    val effectiveSessionLimitMinutes: Int? = null,
    val effectiveCooldownMinutes: Int? = null,
    val summaryLabel: String = "Off-schedule"
)
