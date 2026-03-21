package com.runanywhere.kotlin_starter_example.services

import android.content.Context
import ai.onnxruntime.*
import com.runanywhere.kotlin_starter_example.data.SoundType
import java.nio.FloatBuffer

class SoundClassifier(context: Context) {

    private val env = OrtEnvironment.getEnvironment()
    private val session: OrtSession
    private val labels: List<String>

    init {
        // Load model
        val modelBytes = context.assets.open("yamnet_3s.onnx").readBytes()
        session = env.createSession(modelBytes)

        // Load labels
        labels = context.assets.open("yamnet_class_map.txt")
            .bufferedReader()
            .readLines()
    }

    fun classify(audio: FloatArray): Map<SoundType, Float> {

        // Ensure 48000 samples
        val input = if (audio.size >= 48000) {
            audio.take(48000).toFloatArray()
        } else {
            audio + FloatArray(48000 - audio.size)
        }

        val tensor = OnnxTensor.createTensor(
            env,
            FloatBuffer.wrap(input),
            longArrayOf(1, 48000)
        )

        // ⚠️ input name might need change → see below
        val result = session.run(mapOf("input" to tensor))

        val output = (result[0].value as Array<FloatArray>)[0]

        val predictions = output
            .mapIndexed { index, value -> index to value }
            .sortedByDescending { it.second }
            .take(5)

        val map = mutableMapOf<SoundType, Float>()

        for ((index, score) in predictions) {
            val label = labels[index].lowercase()

            when {
                "siren" in label -> map[SoundType.SIREN] = score
                "horn" in label -> map[SoundType.HORN] = score
                "alarm" in label -> map[SoundType.ALARM] = score
                "doorbell" in label -> map[SoundType.DOORBELL] = score
                "speech" in label || "voice" in label -> map[SoundType.VOICE] = score
            }
        }

        return map
    }
}