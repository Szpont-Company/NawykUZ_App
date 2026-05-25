package com.SzpontCompany.check.notifications

import android.app.Notification
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
import com.SzpontCompany.check.ui.settings.NotificationFrequency
import java.util.Calendar

/**
 * ReminderReceiver - odbiornik powiadomień dla głównych przypomnień o nawyków.
 *
 * Obsługuje:
 * - Powiadomienia zaplanowane przez AlarmManager
 * - Reaktywacja przypomnień po restarcie urządzenia (BOOT_COMPLETED)
 * - Częstość powiadomień (codziennie, dni robocze, własny harmonogram)
 * - Wyświetlanie powiadomień z uwzględnieniem preferencji użytkownika
 *
 * Logika:
 * 1. Sprawdza czy powiadomienia są włączone
 * 2. Sprawdza czy dzisiaj powinno być powiadomienie (na podstawie częstości)
 * 3. Wyświetla powiadomienie
 * 4. Planuje następne powiadomienie
 *
 * @since 1.0
 * @author Szpont Company
 */
class ReminderReceiver : BroadcastReceiver() {

    /**
     * Wywoływana gdy odbiornik otrzyma zaplanowane powiadomienie.
     *
     * Obsługuje również specjalny intent ACTION_BOOT_COMPLETED po restarcie urządzenia
     * w celu przywrócenia zaplanowanych przypomnień.
     *
     * @param context Kontekst aplikacji
     * @param intent Intent zawierający dane (ACTION_BOOT_COMPLETED lub zaplanowane powiadomienie)
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("check_notifications", Context.MODE_PRIVATE)
            if (prefs.getBoolean("mainReminders", false)) {
                val hour = prefs.getInt("savedHour", 20)
                val minute = prefs.getInt("savedMinute", 0)
                NotificationScheduler(context).scheduleDailyReminder(hour, minute)
            }
            return
        }

        val prefs = context.getSharedPreferences("check_notifications", Context.MODE_PRIVATE)
        val mainReminders = prefs.getBoolean("mainReminders", false)

        if (!mainReminders) return

        val savedFrequencyStr = prefs.getString("selectedFrequency", NotificationFrequency.EVERYDAY.name) ?: NotificationFrequency.EVERYDAY.name
        val frequency = NotificationFrequency.valueOf(savedFrequencyStr)

        val savedDaysStrSet = prefs.getStringSet("selectedDays", setOf("0", "1", "2", "3", "4")) ?: setOf("0", "1", "2", "3", "4")
        val selectedDays = savedDaysStrSet.map { it.toInt() }.toSet()

        val calendar = Calendar.getInstance()
        val currentDay = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7

        val shouldNotifyToday = when (frequency) {
            NotificationFrequency.EVERYDAY -> true
            NotificationFrequency.WORKDAYS -> currentDay in 0..4 // 0 to PN, 4 to PT
            NotificationFrequency.CUSTOM -> selectedDays.contains(currentDay)
        }

        if (shouldNotifyToday) {
            val sound = prefs.getBoolean("notificationSound", true)
            val vibrate = prefs.getBoolean("vibrations", true)
            showNotification(context, sound, vibrate)
        }

        val savedHour = prefs.getInt("savedHour", 20)
        val savedMinute = prefs.getInt("savedMinute", 0)
        NotificationScheduler(context).scheduleDailyReminder(savedHour, savedMinute)
    }

    /**
     * Wyświetla powiadomienie push dla głównego przypomnienia.
     *
     * Tworzy kanał notyfikacji (Android 8+), ustawia dźwięk i wibracje.
     * Powiadomienie otwiera MainActivity po kliknięciu.
     *
     * @param context Kontekst aplikacji
     * @param playSound Czy grać dźwięk powiadomienia
     * @param shouldVibrate Czy wywoływać wibracje
     */
    private fun showNotification(context: Context, playSound: Boolean, shouldVibrate: Boolean) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val dynamicChannelId = "habit_reminders_s${playSound}_v${shouldVibrate}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = context.getString(R.string.notification_channel_name)
            val channelDesc = context.getString(R.string.notification_channel_desc)

            val importance = if (playSound || shouldVibrate) NotificationManager.IMPORTANCE_DEFAULT else NotificationManager.IMPORTANCE_LOW

            val channel = NotificationChannel(
                dynamicChannelId,
                channelName,
                importance
            ).apply {
                description = channelDesc
                if (!playSound) {
                    setSound(null, null)
                }
                enableVibration(shouldVibrate)
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

        val notificationTitle = context.getString(R.string.notification_title)
        val notificationContent = context.getString(R.string.notification_content)

        val builder = NotificationCompat.Builder(context, dynamicChannelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notificationTitle)
            .setContentText(notificationContent)
            .setPriority(if (playSound || shouldVibrate) NotificationCompat.PRIORITY_DEFAULT else NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        var defaults = 0
        if (playSound) defaults = defaults or Notification.DEFAULT_SOUND
        if (shouldVibrate) defaults = defaults or Notification.DEFAULT_VIBRATE
        builder.setDefaults(defaults)

        manager.notify(1001, builder.build())
    }
}