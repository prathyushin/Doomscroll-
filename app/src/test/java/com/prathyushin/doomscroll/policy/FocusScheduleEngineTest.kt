package com.prathyushin.doomscroll.policy

import com.prathyushin.doomscroll.model.FocusSchedule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

class FocusScheduleEngineTest {

    private val engine = FocusScheduleEngine()
    private val zone = ZoneId.of("Asia/Kolkata")

    @Test
    fun sameDayScheduleMatchesInsideWindowOnly() {
        val schedule = FocusSchedule(
            id = "work",
            label = "Work",
            startHour = 9,
            startMinute = 0,
            endHour = 17,
            endMinute = 0,
            daysOfWeek = setOf(DayOfWeek.MONDAY),
            timezoneId = zone.id
        )

        val inside = ZonedDateTime.of(2026, 11, 23, 12, 30, 0, 0, zone).toInstant() // Monday
        val outside = ZonedDateTime.of(2026, 11, 23, 18, 0, 0, 0, zone).toInstant()
        val wrongDay = ZonedDateTime.of(2026, 11, 24, 12, 30, 0, 0, zone).toInstant() // Tuesday

        assertTrue(engine.isScheduleActiveAt(schedule, inside, zone))
        assertFalse(engine.isScheduleActiveAt(schedule, outside, zone))
        assertFalse(engine.isScheduleActiveAt(schedule, wrongDay, zone))
    }

    @Test
    fun overnightScheduleHandlesMidnightWrapAround() {
        val overnight = FocusSchedule(
            id = "night",
            label = "Wind Down",
            startHour = 22,
            startMinute = 0,
            endHour = 7,
            endMinute = 0,
            daysOfWeek = setOf(DayOfWeek.FRIDAY),
            timezoneId = zone.id
        )

        val fridayLate = ZonedDateTime.of(2026, 11, 27, 23, 15, 0, 0, zone).toInstant()
        val saturdayEarly = ZonedDateTime.of(2026, 11, 28, 6, 30, 0, 0, zone).toInstant()
        val saturdayMorningAfterEnd = ZonedDateTime.of(2026, 11, 28, 7, 15, 0, 0, zone).toInstant()

        assertTrue(engine.isScheduleActiveAt(overnight, fridayLate, zone))
        assertTrue(engine.isScheduleActiveAt(overnight, saturdayEarly, zone))
        assertFalse(engine.isScheduleActiveAt(overnight, saturdayMorningAfterEnd, zone))
    }
}
