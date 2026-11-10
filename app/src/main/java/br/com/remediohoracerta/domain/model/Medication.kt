package br.com.remediohoracerta.domain.model

data class Medication(
    val id: Long,
    val name: String,
    val dosage: String,
    val time: String,
)

object MedicationInputValidator {
    private val timePattern = Regex("^(?:[01]\\d|2[0-3]):[0-5]\\d$")

    fun isNameValid(name: String): Boolean = name.isNotBlank()

    fun isTimeValid(time: String): Boolean = timePattern.matches(time.trim())
}

