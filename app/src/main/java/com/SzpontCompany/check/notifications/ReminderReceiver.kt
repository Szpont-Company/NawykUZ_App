package com.SzpontCompany.check.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.SzpontCompany.check.MainActivity
import com.SzpontCompany.check.R

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Jeśli urządzenie się zrestartowało, tu w przyszłości
        // dodamy odświeżanie alarmów, na razie przechwytujemy normalny alarm
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // TODO: Reschedule alarms
            return
        }

        showNotification(context)
    }

    private fun showNotification(context: Context) {
        val channelId = "habit_reminders"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Przypomnienia o nawykach",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Codzienne powiadomienia przypominające o twoich celach"
            }
            manager.createNotificationChannel(channel)
        }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: ikonka do zmiany
            .setContentTitle("Czas na nawyki!")
            .setContentText("Sprawdź co dziś przygotowałeś.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(1001, notification)
    }
}