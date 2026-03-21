package com.runanywhere.kotlin_starter_example.services

import android.media.*
import kotlinx.coroutines.*
import kotlin.math.min

class AudioProcessor {

    private var isRecording = false

    fun start(onAudio: (FloatArray) -> Unit) {

        val sampleRate = 16000
        val chunkSize = 48000 // 🔥 REQUIRED for your model (3 sec)

        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize
        )

        val shortBuffer = ShortArray(minBufferSize)
        val floatBuffer = mutableListOf<Float>()

        audioRecord.startRecording()
        isRecording = true

        CoroutineScope(Dispatchers.IO).launch {

            while (isRecording) {

                val read = audioRecord.read(shortBuffer, 0, shortBuffer.size)

                for (i in 0 until read) {
                    floatBuffer.add(shortBuffer[i] / 32768f)
                }

                // 🔥 Once we have 48000 samples → run model
                if (floatBuffer.size >= chunkSize) {

                    val chunk = FloatArray(chunkSize)
                    for (i in 0 until chunkSize) {
                        chunk[i] = floatBuffer[i]
                    }

                    // remove used samples
                    repeat(chunkSize) { floatBuffer.removeAt(0) }

                    onAudio(chunk)
                }
            }
        }
    }

    fun stop() {
        isRecording = false
    }
}