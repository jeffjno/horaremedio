package br.com.remediohoracerta.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import br.com.remediohoracerta.MainActivity
import br.com.remediohoracerta.R
import br.com.remediohoracerta.domain.model.Medication

object ReminderNotifications {
    private const val channelId = "medication_reminders_v1"
    const val actionSnooze = "br.com.remediohoracerta.action.SNOOZE"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            channelId,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            enableVibration(true)
            setShowBadge(true)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun show(context: Context, medication: Medication) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val openApp = PendingIntent.getActivity(
            context,
            medication.id.hashCode(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val snooze = PendingIntent.getBroadcast(
            context,
            medication.id.hashCode(),
            Intent(context, ReminderActionReceiver::class.java)
                .setAction(actionSnooze)
                .putExtra(AlarmScheduler.extraMedicationId, medication.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val dosage = medication.dosage.ifBlank {
            context.getString(R.string.dosage_not_informed)
        }
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(medication.name)
            .setContentText(
                context.getString(
                    R.string.reminder_notification_body,
                    dosage,
                    medication.time,
                ),
            )
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    context.getString(
                        R.string.reminder_notification_body,
                        dosage,
                        medication.time,
                    ),
                ),
            )
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .addAction(0, context.getString(R.string.open_app), openApp)
            .addAction(0, context.getString(R.string.snooze_ten_minutes), snooze)
            .build()

        NotificationManagerCompat.from(context).notify(medication.id.hashCode(), notification)
    }

    fun cancel(context: Context, medicationId: Long) {
        NotificationManagerCompat.from(context).cancel(medicationId.hashCode())
    }
}

