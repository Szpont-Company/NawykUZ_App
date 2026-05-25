package com.SzpontCompany.check.data.steps

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.SzpontCompany.check.R
import com.SzpontCompany.check.widgets.WidgetDataUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * StepCounterService - usługa systemu zliczająca kroki użytkownika w tle.
 *
 * Ta usługa działająca na pierwszym planie (foreground service) nasłuchuje
 * zmiany z czujnika krokomierza urządzenia (STEP_COUNTER) i aktualizuje
 * liczbę kroków w bazie danych oraz widżetach aplikacji.
 *
 * Funkcje:
 * - Rejestracja słuchacza czujnika krokomierza
 * - Przetwarzanie danych sensorów w tle
 * - Aktualizacja danych w bazie Firebase
 * - Notyfikacja dla widżetów o zmianach
 *
 * Wymaga pozwoleń:
 * - android.permission.ACTIVITY_RECOGNITION (Android 10+)
 *
 * @since 1.0
 * @author Szpont Company
 */
class StepCounterService : Service(), SensorEventListener {

    /** Manager sensorów urządzenia */
    private lateinit var sensorManager: SensorManager

    /** Sensor krokomierza (TYPE_STEP_COUNTER) */
    private var stepSensor: Sensor? = null

    /** Repozytorium do zarządzania danymi kroków */
    private lateinit var stepRepository: StepRepository

    /** Scope do operacji asynchronicznych */
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    /**
     * Wywoływana przy tworzeniu serwisu.
     *
     * Inicjalizuje menadżera sensorów, pobiera sensor krokomierza,
     * wyświetla notyfikację dla serwisu pierwszoplanowego,
     * i rejestruje słuchacza sensorowego.
     */
    override fun onCreate() {
        super.onCreate()
        stepRepository = StepRepository(applicationContext)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        startForegroundServiceWithNotification()

        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    /**
     * Wywoływana gdy serwis jest startowany.
     *
     * Zwraca START_STICKY aby serwis był restartowany przez system
     * jeśli został zabity z powodu braku zasobów.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    /**
     * Wywoływana gdy sensor emituje nowe zdarzenie.
     *
     * Przechwytuje wartość całkowitej liczby kroków od ostatniego restartu
     * urządzenia i przetwarza je asynchronicznie.
     *
     * @param event Zdarzenie sensora zawierające wartości kroków
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalStepsSinceReboot = event.values[0].toInt()

            android.util.Log.d("StepCounter", "Czujnik złapał kroki! Łącznie od włączenia telefonu: $totalStepsSinceReboot")

            serviceScope.launch {
                stepRepository.processSensorSteps(totalStepsSinceReboot)
                WidgetDataUpdater().updateStepsWidgetData(applicationContext)
            }
        }
    }

    /**
     * Wywoływana gdy zmienia się dokładność sensora.
     *
     * Nie wymaga działań dla krokomierza.
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    /**
     * Wywoływana przy niszczeniu serwisu.
     *
     * Wyrejestrowuje słuchacza sensorowego aby uniknąć wycieków zasobów.
     */
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    /**
     * Zwraca null ponieważ ten serwis nie wspiera bindowania.
     */
    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Tworzy i uruchamia notyfikację dla serwisu pierwszoplanowego.
     *
     * Wymagane dla serwisów działających w tle na Android 8+.
     * Notyfikacja informuje użytkownika że aplikacja zlicza kroki.
     */
    private fun startForegroundServiceWithNotification() {
        val channelId = "step_counter_channel"
        val channelName = "Liczenie kroków w tle"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("NawykUZ - Liczenie Kroków")
            .setContentText("Aplikacja zlicza Twoje kroki w tle...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }
}