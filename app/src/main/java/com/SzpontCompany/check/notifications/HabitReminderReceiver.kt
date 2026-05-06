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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HabitReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getStringExtra("HABIT_ID") ?: return
        val habitName = intent.getStringExtra("HABIT_NAME") ?: "Nawyk"
        val habitIcon = intent.getStringExtra("HABIT_ICON") ?: "🎯"
        val isEvening = intent.getBooleanExtra("IS_EVENING", false)

        val prefs = context.getSharedPreferences("check_notifications", Context.MODE_PRIVATE)

        if (!isEvening && !prefs.getBoolean("mainReminders", false)) return
        if (isEvening && !prefs.getBoolean("eveningReminders", true)) return

        val playSound = prefs.getBoolean("notificationSound", true)
        val shouldVibrate = prefs.getBoolean("vibrations", true)

        if (isEvening) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        val doc = FirebaseFirestore.getInstance()
                            .collection("users").document(uid)
                            .collection("habits").document(habitId)
                            .get().await()

                        val completedDates = doc.get("completedDates") as? List<*>
                        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                        if (completedDates?.contains(todayStr) != true) {
                            showNotification(context, habitId, habitName, habitIcon, isEvening, playSound, shouldVibrate)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    showNotification(context, habitId, habitName, habitIcon, isEvening, playSound, shouldVibrate)
                } finally {
                    pendingResult.finish()
                    reschedule(context, intent)
                }
            }
        } else {
            showNotification(context, habitId, habitName, habitIcon, isEvening, playSound, shouldVibrate)
            reschedule(context, intent)
        }
    }

    private fun showNotification(context: Context, habitId: String, name: String, icon: String, isEvening: Boolean, playSound: Boolean, shouldVibrate: Boolean) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val dynamicChannelId = "specific_habit_reminders_s${playSound}_v${shouldVibrate}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Habit Reminders"
            val channelDesc = "Przypomnienia dla konkretnych nawyków"
            val importance = if (playSound || shouldVibrate) NotificationManager.IMPORTANCE_DEFAULT else NotificationManager.IMPORTANCE_LOW

            val channel = NotificationChannel(dynamicChannelId, channelName, importance).apply {
                description = channelDesc
                if (!playSound) setSound(null, null)
                enableVibration(shouldVibrate)
            }
            manager.createNotificationChannel(channel)
        }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, habitId.hashCode(), contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val engLang = Locale.getDefault().language == "en"

        val title = if (engLang) {
            if (isEvening) "Did you forget about $name? $icon" else "Time for $name! $icon"
        } else {
            if (isEvening) "Czyżbyś zapomniał o $name? $icon" else "Czas na $name! $icon"
        }

        val content = if (engLang) "Check it off today to keep your streak!" else "Odznacz go dzisiaj, aby utrzymać swoją serię!"

        val builder = NotificationCompat.Builder(context, dynamicChannelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(if (playSound || shouldVibrate) NotificationCompat.PRIORITY_DEFAULT else NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        var defaults = 0
        if (playSound) defaults = defaults or Notification.DEFAULT_SOUND
        if (shouldVibrate) defaults = defaults or Notification.DEFAULT_VIBRATE
        builder.setDefaults(defaults)

        manager.notify(habitId.hashCode() + if (isEvening) 1 else 0, builder.build())
    }

    private fun reschedule(context: Context, intent: Intent) {
        val habitId = intent.getStringExtra("HABIT_ID") ?: return
        val habitName = intent.getStringExtra("HABIT_NAME") ?: "Nawyk"
        val habitIcon = intent.getStringExtra("HABIT_ICON") ?: "🎯"
        val isEvening = intent.getBooleanExtra("IS_EVENING", false)

        CoroutineScope(Dispatchers.IO).launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .collection("habits").document(habitId)
                    .get().await()

                val isActive = doc.getBoolean("isActive") ?: true
                if (!isActive) return@launch // Nie planujemy ponownego dla nieaktywnych

                val dailyReminder = doc.getBoolean("dailyReminder") ?: false
                val eveningReminder = doc.getBoolean("eveningReminder") ?: false

                if (!isEvening && dailyReminder) {
                    val time = doc.getString("reminderTime") ?: "08:00"
                    scheduleNext(context, habitId, habitName, habitIcon, time, false)
                } else if (isEvening && eveningReminder) {
                    val time = doc.getString("eveningTime") ?: "20:00"
                    scheduleNext(context, habitId, habitName, habitIcon, time, true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun scheduleNext(context: Context, habitId: String, name: String, icon: String, time: String, isEvening: Boolean) {
        val parts = time.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: if (isEvening) 20 else 8
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0

        NotificationScheduler(context).scheduleHabitReminder(habitId, name, icon, h, m, isEvening)
    }
}
