package com.runanywhere.kotlin_starter_example.services

import android.content.Context
import android.util.Log
import com.runanywhere.kotlin_starter_example.data.SoundType
import com.runanywhere.sdk.public.RunAnywhere
import com.runanywhere.sdk.public.extensions.transcribe
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SoundClassifier(private val context: Context) {

    companion object {
        const val TAG = "SoundClassifier"
        const val SAMPLE_RATE = 16000
        const val REQUIRED_SAMPLES = 48000
        const val COOLDOWN_MS = 3000L
    }

    private val audioBuffer = mutableListOf<Float>()
    private var lastDetectionTime = 0L

    private val keywordMap = mapOf(
        "siren"      to SoundType.SIREN,
        "ambulance"  to SoundType.SIREN,
        "fire truck" to SoundType.SIREN,
        "police"     to SoundType.SIREN,
        "wee woo"    to SoundType.SIREN,
        "emergency"  to SoundType.SIREN,
        "horn"       to SoundType.HORN,
        "honk"       to SoundType.HORN,
        "beep"       to SoundType.HORN,
        "toot"       to SoundType.HORN,
        "alarm"      to SoundType.ALARM,
        "alert"      to SoundType.ALARM,
        "beeping"    to SoundType.ALARM,
        "ringing"    to SoundType.ALARM,
        "fire alarm" to SoundType.ALARM,
        "doorbell"   to SoundType.DOORBELL,
        "door bell"  to SoundType.DOORBELL,
        "ding dong"  to SoundType.DOORBELL,
        "ding"       to SoundType.DOORBELL,
        "dong"       to SoundType.DOORBELL,
        "hello"      to SoundType.VOICE,
        "hey"        to SoundType.VOICE,
        "help"       to SoundType.VOICE,
        "excuse me"  to SoundType.VOICE,
        "hi"         to SoundType.VOICE,
        "stop"       to SoundType.VOICE,
        "wait"       to SoundType.VOICE
    )

    suspend fun classify(audio: FloatArray): Map<SoundType, Float> {
        audioBuffer.addAll(audio.toList())

        if (audioBuffer.size < REQUIRED_SAMPLES) return emptyMap()

        val now = System.currentTimeMillis()
        if (now - lastDetectionTime < COOLDOWN_MS) {
            repeat(REQUIRED_SAMPLES / 2) {
                if (audioBuffer.isNotEmpty()) audioBuffer.removeAt(0)
            }
            return emptyMap()
        }

        val input = audioBuffer.take(REQUIRED_SAMPLES).toFloatArray()
        repeat(REQUIRED_SAMPLES / 2) {
            if (audioBuffer.isNotEmpty()) audioBuffer.removeAt(0)
        }

        // Skip silent chunks
        val rms = Math.sqrt(
            input.fold(0.0) { acc, s -> acc + s * s } / input.size
        ).toFloat()

        if (rms < 0.005f) {
            Log.d(TAG, "Silent chunk — skipping")
            return emptyMap()
        }

        return try {
            val wavBytes = floatArrayToWav(input, SAMPLE_RATE)

            // Attempt transcription — if model not loaded, exception is caught below
            val transcript = RunAnywhere.transcribe(wavBytes)
                .trim()
                .lowercase()

            Log.d(TAG, "Transcript: '$transcript'")

            if (transcript.isBlank()) return emptyMap()

            val detected = matchKeywords(transcript)

            if (detected != null) {
                lastDetectionTime = now
                Log.d(TAG, "Detected: ${detected.first} @ ${detected.second}")
                mapOf(detected.first to detected.second)
            } else if (transcript.length > 3) {
                lastDetectionTime = now
                Log.d(TAG, "Speech → VOICE fallback")
                mapOf(SoundType.VOICE to 0.6f)
            } else {
                emptyMap()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Transcription failed: ${e.message}")
            emptyMap()
        }
    }

    private fun matchKeywords(transcript: String): Pair<SoundType, Float>? {
        // Sort by length descending — match "fire alarm" before "alarm"
        val sorted = keywordMap.entries.sortedByDescending { it.key.length }
        for ((keyword, soundType) in sorted) {
            if (transcript.contains(keyword)) {
                val confidence = when {
                    transcript == keyword          -> 1.0f
                    transcript.startsWith(keyword) -> 0.9f
                    else                           -> 0.8f
                }
                return Pair(soundType, confidence)
            }
        }
        return null
    }

    private fun floatArrayToWav(samples: FloatArray, sampleRate: Int): ByteArray {
        val pcmBytes = ByteArray(samples.size * 2)
        val buffer = ByteBuffer.wrap(pcmBytes).order(ByteOrder.LITTLE_ENDIAN)
        samples.forEach { sample ->
            val clamped = sample.coerceIn(-1f, 1f)
            buffer.putShort((clamped * 32767).toInt().toShort())
        }

        val out = ByteArrayOutputStream()
        val totalDataLen = pcmBytes.size + 36
        val byteRate = sampleRate * 2

        out.write("RIFF".toByteArray())
        out.write(intToBytes(totalDataLen))
        out.write("WAVE".toByteArray())
        out.write("fmt ".toByteArray())
        out.write(intToBytes(16))
        out.write(shortToBytes(1))
        out.write(shortToBytes(1))
        out.write(intToBytes(sampleRate))
        out.write(intToBytes(byteRate))
        out.write(shortToBytes(2))
        out.write(shortToBytes(16))
        out.write("data".toByteArray())
        out.write(intToBytes(pcmBytes.size))
        out.write(pcmBytes)

        return out.toByteArray()
    }

    private fun intToBytes(value: Int): ByteArray =
        ByteBuffer.allocate(4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(value)
            .array()

    private fun shortToBytes(value: Int): ByteArray =
        ByteBuffer.allocate(2)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putShort(value.toShort())
            .array()

    fun close() {
        audioBuffer.clear()
    }
}