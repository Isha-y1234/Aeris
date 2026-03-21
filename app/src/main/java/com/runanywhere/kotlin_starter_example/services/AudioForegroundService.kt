package com.runanywhere.kotlin_starter_example.services

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.runanywhere.kotlin_starter_example.R
import com.runanywhere.kotlin_starter_example.data.SoundRepository
import kotlinx.coroutines.*

class AudioForegroundService : Service() {

    private lateinit var audioProcessor: AudioProcessor
    private lateinit var classifier: SoundClassifier
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onCreate() {
        super.onCreate()

        audioProcessor = AudioProcessor()
        classifier = SoundClassifier(applicationContext)

        startForeground(1, createNotification())

        startListening()
    }

    private fun startListening() {
        serviceScope.launch {
            audioProcessor.start { audioChunk ->
                val result = classifier.classify(audioChunk)
                SoundRepository.updateDetection(result)
            }
        }
    }

    private fun createNotification(): Notification {
        val channelId = "audio_service_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Audio Detection Service",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Aeris Running")
            .setContentText("Listening for sounds...")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // make sure this exists
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioProcessor.stop()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}