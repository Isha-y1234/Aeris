package com.runanywhere.kotlin_starter_example.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.runanywhere.kotlin_starter_example.R
import com.runanywhere.kotlin_starter_example.data.SoundRepository
import com.runanywhere.kotlin_starter_example.data.SettingsRepository
import com.runanywhere.kotlin_starter_example.data.SoundType
import kotlinx.coroutines.*

class AudioForegroundService : Service() {

    companion object {
        const val TAG = "AudioForegroundService"
        var isRunning = false
            private set
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var audioProcessor: AudioProcessor
    private lateinit var classifier: SoundClassifier
    
    private val lastNotificationTimes = mutableMapOf<SoundType, Long>()
    private val NOTIFICATION_COOLDOWN = 8000L

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        isRunning = true
        audioProcessor = AudioProcessor(applicationContext)
        classifier = SoundClassifier(applicationContext)
        createNotificationChannels()
        
        val notification = buildForegroundNotification("Aeris Active", "Monitoring for sounds...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(1, notification)
        }
        
        startListening()
    }

    private fun startListening() {
        scope.launch {
            try {
                audioProcessor.start { audioChunk ->
                    scope.launch {
                        try {
                            val predictions = classifier.classify(audioChunk)
                            if (predictions.isNotEmpty()) {
                                val sensitivities = SettingsRepository.sensitivities.value
                                val filtered = predictions.filter { (type, conf) ->
                                    val threshold = sensitivities[type] ?: 0.5f
                                    conf >= threshold
                                }

                                if (filtered.isNotEmpty()) {
                                    SoundRepository.updateDetection(filtered)
                                    
                                    val top = filtered.maxByOrNull { it.value }!!
                                    
                                    // Trigger haptics
                                    HapticManager.trigger(applicationContext, top.key)
                                    
                                    // Trigger Visual Flash
                                    if (SettingsRepository.flashEnabled.value && Settings.canDrawOverlays(applicationContext)) {
                                        triggerFlashAlert(top.key)
                                    }
                                    
                                    // Notification
                                    checkAndSendNotification(filtered)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Detection error: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "startListening error: ${e.message}")
            }
        }
    }

    private fun triggerFlashAlert(type: SoundType) {
        val color = when (type) {
            SoundType.ALARM -> Color.RED
            SoundType.SIREN -> Color.parseColor("#FF9800")
            SoundType.HORN -> Color.YELLOW
            SoundType.DOORBELL -> Color.GREEN
            SoundType.VOICE -> Color.BLUE
        }
        FlashAlertService.start(applicationContext, color)
    }

    private fun checkAndSendNotification(predictions: Map<SoundType, Float>) {
        if (!SettingsRepository.notificationsEnabled.value) return
        val top = predictions.maxByOrNull { it.value } ?: return
        val now = System.currentTimeMillis()
        val lastTime = lastNotificationTimes[top.key] ?: 0L
        if (now - lastTime > NOTIFICATION_COOLDOWN) {
            sendSoundNotification(top.key, top.value)
            lastNotificationTimes[top.key] = now
        }
    }

    private fun sendSoundNotification(type: SoundType, confidence: Float) {
        val manager = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, "aeris_alerts")
            .setContentTitle("${type.name} Detected!")
            .setContentText("Confidence: ${(confidence * 100).toInt()}%")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify(type.ordinal + 100, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        scope.cancel()
        audioProcessor.stop()
        classifier.close()
        SoundRepository.updateDetection(emptyMap())
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel("aeris_channel") == null) {
            manager.createNotificationChannel(NotificationChannel("aeris_channel", "Aeris Monitoring", NotificationManager.IMPORTANCE_LOW))
        }
        if (manager.getNotificationChannel("aeris_alerts") == null) {
            val alertChannel = NotificationChannel("aeris_alerts", "Sound Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                enableLights(true); enableVibration(true)
            }
            manager.createNotificationChannel(alertChannel)
        }
    }

    private fun buildForegroundNotification(title: String, text: String): Notification {
        return NotificationCompat.Builder(this, "aeris_channel")
            .setContentTitle(title).setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true).build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
