package br.com.remediohoracerta.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import br.com.remediohoracerta.domain.model.Medication
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime

object AlarmScheduler {
    const val extraMedicationId = "medication_id"

    fun scheduleNext(
        context: Context,
        medication: Medication,
        after: ZonedDateTime = ZonedDateTime.now(),
    ) {
        if (!medication.isActive) {
            cancel(context, medication.id)
            return
        }
        val triggerAt = nextOccurrence(medication, after) ?: return
        scheduleAt(context, medication.id, triggerAt.toInstant().toEpochMilli())
    }

    fun cancel(context: Context, medicationId: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = alarmPendingIntent(context, medicationId)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun snooze(context: Context, medicationId: Long, minutes: Long = 10) {
        val triggerAtMillis = System.currentTimeMillis() + Duration.ofMinutes(minutes).toMillis()
        scheduleAt(context, medicationId, triggerAtMillis)
    }

    internal fun nextOccurrence(
        medication: Medication,
        after: ZonedDateTime,
    ): ZonedDateTime? {
        if (!medication.isActive) return null
        val time = runCatching { LocalTime.parse(medication.time) }.getOrNull() ?: return null
        for (offset in 0..7L) {
            val date = after.toLocalDate().plusDays(offset)
            val candidate = date.atTime(time).atZone(after.zone)
            val dayNumber = DayOfWeek.from(date).value
            if (dayNumber in medication.daysOfWeek && candidate.isAfter(after)) {
                return candidate
            }
        }
        return null
    }

    private fun scheduleAt(context: Context, medicationId: Long, triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = alarmPendingIntent(context, medicationId)

        val canScheduleExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()
        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        }
    }

    private fun alarmPendingIntent(context: Context, medicationId: Long): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            medicationId.hashCode(),
            Intent(context, AlarmReceiver::class.java)
                .putExtra(extraMedicationId, medicationId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}

