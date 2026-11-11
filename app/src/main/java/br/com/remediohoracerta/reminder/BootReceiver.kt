package br.com.remediohoracerta.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.com.remediohoracerta.data.MedicationStorage

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        MedicationStorage.load(context).forEach { medication ->
            AlarmScheduler.scheduleNext(context, medication)
        }
    }
}

