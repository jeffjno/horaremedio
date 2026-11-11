package br.com.remediohoracerta.data

import android.content.Context
import androidx.core.content.edit
import br.com.remediohoracerta.domain.model.Medication
import org.json.JSONArray
import org.json.JSONObject

object MedicationStorage {
    private const val preferencesName = "medication_preferences"
    private const val medicationsKey = "medications"

    fun load(context: Context): List<Medication> {
        val serialized = context
            .getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
            .getString(medicationsKey, null)
            ?: return emptyList()

        return runCatching {
            val array = JSONArray(serialized)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        Medication(
                            id = item.getLong("id"),
                            name = item.getString("name"),
                            dosage = item.optString("dosage"),
                            time = item.getString("time"),
                            daysOfWeek = item.optJSONArray("daysOfWeek")
                                ?.let(::readDaysOfWeek)
                                ?.takeIf { it.isNotEmpty() }
                                ?: (1..7).toSet(),
                            medicationPhotoPath = item.optString("medicationPhotoPath")
                                .takeIf(String::isNotBlank),
                            prescriptionPhotoPath = item.optString("prescriptionPhotoPath")
                                .takeIf(String::isNotBlank),
                            isActive = item.optBoolean("isActive", true),
                        ),
                    )
                }
            }.sortedForDisplay()
        }.getOrDefault(emptyList())
    }

    fun add(
        context: Context,
        name: String,
        dosage: String,
        time: String,
        daysOfWeek: Set<Int>,
        medicationPhotoPath: String?,
        prescriptionPhotoPath: String?,
    ): Pair<Medication, List<Medication>> {
        val medication = Medication(
            id = System.currentTimeMillis(),
            name = name.trim(),
            dosage = dosage.trim(),
            time = time.trim(),
            daysOfWeek = daysOfWeek,
            medicationPhotoPath = medicationPhotoPath,
            prescriptionPhotoPath = prescriptionPhotoPath,
        )
        val updated = load(context) + medication
        persist(context, updated)
        return medication to updated.sortedForDisplay()
    }

    fun setActive(context: Context, id: Long, isActive: Boolean): List<Medication> {
        val updated = load(context).map { medication ->
            if (medication.id == id) medication.copy(isActive = isActive) else medication
        }
        persist(context, updated)
        return updated.sortedForDisplay()
    }

    fun delete(context: Context, id: Long): Pair<Medication?, List<Medication>> {
        val current = load(context)
        val removed = current.firstOrNull { it.id == id }
        val updated = current.filterNot { it.id == id }
        persist(context, updated)
        return removed to updated.sortedForDisplay()
    }

    private fun persist(context: Context, medications: List<Medication>) {
        val array = JSONArray()
        medications.forEach { medication ->
            array.put(
                JSONObject()
                    .put("id", medication.id)
                    .put("name", medication.name)
                    .put("dosage", medication.dosage)
                    .put("time", medication.time)
                    .put("daysOfWeek", JSONArray(medication.daysOfWeek.sorted()))
                    .put("medicationPhotoPath", medication.medicationPhotoPath.orEmpty())
                    .put("prescriptionPhotoPath", medication.prescriptionPhotoPath.orEmpty())
                    .put("isActive", medication.isActive),
            )
        }
        context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).edit {
            putString(medicationsKey, array.toString())
        }
    }

    fun find(context: Context, id: Long): Medication? = load(context).firstOrNull { it.id == id }

    private fun readDaysOfWeek(array: JSONArray): Set<Int> = buildSet {
        for (index in 0 until array.length()) {
            array.optInt(index).takeIf { it in 1..7 }?.let(::add)
        }
    }

    private fun List<Medication>.sortedForDisplay(): List<Medication> =
        sortedWith(compareByDescending<Medication> { it.isActive }.thenBy { it.time })
}

