package br.com.remediohoracerta.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationInputValidatorTest {
    @Test
    fun `requires a medication name`() {
        assertFalse(MedicationInputValidator.isNameValid("  "))
        assertTrue(MedicationInputValidator.isNameValid("Losartana"))
    }

    @Test
    fun `accepts only valid 24 hour times`() {
        assertTrue(MedicationInputValidator.isTimeValid("08:30"))
        assertTrue(MedicationInputValidator.isTimeValid("23:59"))
        assertFalse(MedicationInputValidator.isTimeValid("24:00"))
        assertFalse(MedicationInputValidator.isTimeValid("8:30"))
    }
}
