package br.com.remediohoracerta.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import br.com.remediohoracerta.data.MedicationStorage

class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderNotifications.actionSnooze) return
        val medicationId = intent.getLongExtra(AlarmScheduler.extraMedicationId, -1L)
        if (medicationId < 0) return
        NotificationManagerCompat.from(context).cancel(medicationId.hashCode())
        val medication = MedicationStorage.find(context, medicationId)
        if (medication?.isActive != true) return
        AlarmScheduler.snooze(context, medicationId)
    }
}

