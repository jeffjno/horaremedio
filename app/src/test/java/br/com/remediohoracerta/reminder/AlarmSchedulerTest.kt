package br.com.remediohoracerta.reminder

import br.com.remediohoracerta.domain.model.Medication
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class AlarmSchedulerTest {
    private val zone = ZoneId.of("America/Sao_Paulo")

    @Test
    fun `schedules the same day when its time is still ahead`() {
        val medication = Medication(1, "Teste", "1", "20:00", setOf(1, 2, 3, 4, 5, 6, 7))
        val after = ZonedDateTime.of(2026, 11, 10, 10, 0, 0, 0, zone)

        assertEquals(
            ZonedDateTime.of(2026, 11, 10, 20, 0, 0, 0, zone),
            AlarmScheduler.nextOccurrence(medication, after),
        )
    }

    @Test
    fun `skips days outside the selected periodicity`() {
        val medication = Medication(1, "Teste", "1", "08:00", setOf(1, 3, 5))
        val tuesday = ZonedDateTime.of(2026, 11, 10, 10, 0, 0, 0, zone)

        assertEquals(
            ZonedDateTime.of(2026, 11, 11, 8, 0, 0, 0, zone),
            AlarmScheduler.nextOccurrence(medication, tuesday),
        )
    }

    @Test
    fun `does not schedule a deactivated medication`() {
        val medication = Medication(
            id = 1,
            name = "Teste",
            dosage = "1",
            time = "20:00",
            isActive = false,
        )
        val after = ZonedDateTime.of(2026, 11, 11, 10, 0, 0, 0, zone)

        assertEquals(null, AlarmScheduler.nextOccurrence(medication, after))
    }
}
