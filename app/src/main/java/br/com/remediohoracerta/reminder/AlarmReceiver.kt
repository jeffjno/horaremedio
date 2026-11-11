package br.com.remediohoracerta.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.com.remediohoracerta.data.MedicationStorage
import java.time.ZonedDateTime

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getLongExtra(AlarmScheduler.extraMedicationId, -1L)
        val medication = MedicationStorage.load(context)
            .firstOrNull { it.id == medicationId && it.isActive }
            ?: return
        ReminderNotifications.createChannel(context)
        ReminderNotifications.show(context, medication)
        AlarmScheduler.scheduleNext(context, medication, ZonedDateTime.now().plusMinutes(1))
    }
}

