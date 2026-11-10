package br.com.remediohoracerta.data

import android.content.Context
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
                        ),
                    )
                }
            }.sortedBy(Medication::time)
        }.getOrDefault(emptyList())
    }

    fun add(context: Context, name: String, dosage: String, time: String): List<Medication> {
        val updated = load(context) + Medication(
            id = System.currentTimeMillis(),
            name = name.trim(),
            dosage = dosage.trim(),
            time = time.trim(),
        )
        persist(context, updated)
        return updated.sortedBy(Medication::time)
    }

    private fun persist(context: Context, medications: List<Medication>) {
        val array = JSONArray()
        medications.forEach { medication ->
            array.put(
                JSONObject()
                    .put("id", medication.id)
                    .put("name", medication.name)
                    .put("dosage", medication.dosage)
                    .put("time", medication.time),
            )
        }
        context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
            .edit()
            .putString(medicationsKey, array.toString())
            .apply()
    }
}

