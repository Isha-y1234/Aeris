package com.runanywhere.kotlin_starter_example.services

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.runanywhere.kotlin_starter_example.R
import com.runanywhere.kotlin_starter_example.data.SoundRepository
import kotlinx.coroutines.*

class AudioForegroundService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var audioProcessor: AudioProcessor
    private lateinit var classifier: SoundClassifier

    override fun onCreate() {
        super.onCreate()
        audioProcessor = AudioProcessor(applicationContext)
        classifier = SoundClassifier(applicationContext)
        startForeground(1, createNotification())
        startListening()
    }

    private fun startListening() {
        scope.launch {
            try {
                audioProcessor.start { audioChunk ->
                    // ✅ launch a new coroutine per chunk so suspend classify() can be called
                    scope.launch {
                        try {
                            val predictions = classifier.classify(audioChunk)
                            if (predictions.isNotEmpty()) {
                                SoundRepository.updateDetection(predictions)
                            }
                        } catch (e: Exception) {
                            Log.e("AudioForegroundService", "classify error: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AudioForegroundService", "startListening error: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        audioProcessor.stop()
        classifier.close()
        SoundRepository.updateDetection(emptyMap())
    }

    private fun createNotification(): Notification {
        val channelId = "aeris_channel"
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            channelId, "Aeris Listening",
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Aeris Running")
            .setContentText("Listening for sounds")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}