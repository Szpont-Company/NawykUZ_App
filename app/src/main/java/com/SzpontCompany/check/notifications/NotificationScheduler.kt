package com.SzpontCompany.check.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

/**
 * NotificationScheduler - zarządza planowaniem powiadomień za pomocą AlarmManager.
 *
 * Odpowiada za:
 * - Planowanie codziennych przypomnień
 * - Planowanie przypomnień dla konkretnych nawyków
 * - Anulowanie zaplanowanych powiadomień
 * - Obsługa różnych wersji Android (uprawnienia, API)
 *
 * Obsługuje:
 * - Przypomnienia generalne (ReminderReceiver)
 * - Przypomnienia nawyków (HabitReminderReceiver)
 * - Budżet do dokładnego planowania (SCHEDULE_EXACT_ALARM na Android 12+)
 * - Operacje w stanie uśpienia urządzenia (doze mode)
 *
 * @since 1.0
 * @author Szpont Company
 */
class NotificationScheduler(private val context: Context) {

    /** Manager alarmów systemowych */
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Planuje codzienne powiadomienie na określoną godzinę.
     *
     * Jeśli godzina już minęła w bieżącym dniu, alarm zostanie ustawiony na następny dzień.
     *
     * @param hour Godzina (0-23)
     * @param minute Minuta (0-59)
     */
    fun scheduleDailyReminder(hour: Int, minute: Int) {
        cancelReminder()

        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    /**
     * Anuluje zaplanowane codzienne powiadomienie.
     */
    fun cancelReminder() {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        /** Request code dla ogólnych przypomnień */
        private const val REMINDER_REQUEST_CODE = 2001
    }

    /**
     * Planuje powiadomienie dla konkretnego nawyku na określoną godzinę.
     *
     * Każdy nawyk może mieć dwa oddzielne alarmy: poranny i wieczorny.
     * Request code jest unikalny dla każdej kombinacji habitId + isEvening.
     *
     * @param habitId Unikalny identyfikator nawyku
     * @param name Nazwa nawyku do wyświetlenia w powiadomieniu
     * @param icon Emoji nawyku
     * @param hour Godzina (0-23)
     * @param minute Minuta (0-59)
     * @param isEvening true dla wieczornych przypomnień
     */
    fun scheduleHabitReminder(habitId: String, name: String, icon: String, hour: Int, minute: Int, isEvening: Boolean) {
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra("HABIT_ID", habitId)
            putExtra("HABIT_NAME", name)
            putExtra("HABIT_ICON", icon)
            putExtra("IS_EVENING", isEvening)
        }

        val requestCode = habitId.hashCode() + if (isEvening) 1 else 0

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    /**
     * Anuluje zaplanowane powiadomienie dla konkretnego nawyku.
     *
     * @param habitId Identyfikator nawyku
     * @param isEvening true aby anulować wieczorne powiadomienie
     */
    fun cancelHabitReminder(habitId: String, isEvening: Boolean) {
        val intent = Intent(context, HabitReminderReceiver::class.java)
        val requestCode = habitId.hashCode() + if (isEvening) 1 else 0
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}