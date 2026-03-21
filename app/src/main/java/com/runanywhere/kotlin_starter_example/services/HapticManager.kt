package com.runanywhere.kotlin_starter_example.services

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.runanywhere.kotlin_starter_example.data.SoundProfiles
import com.runanywhere.kotlin_starter_example.data.SoundType

object HapticManager {

    fun trigger(context: Context, sound: SoundType) {
        val vibrator = getVibrator(context)
        val profile = SoundProfiles.map[sound] ?: return

        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // ✅ Use amplitudes so each sound has distinct intensity
            val amplitudes = getAmplitudes(sound, profile.pattern.size)
            val effect = VibrationEffect.createWaveform(
                profile.pattern,
                amplitudes,
                profile.repeat
            )
            vibrator.cancel()
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(profile.pattern, profile.repeat)
        }
    }

    // Amplitude array must match pattern array size
    // Even indices = OFF (always 0), odd indices = ON (intensity)
    private fun getAmplitudes(sound: SoundType, size: Int): IntArray {
        val onAmplitude = when (sound) {
            SoundType.SIREN    -> 255  // max — urgent
            SoundType.HORN     -> 255  // max — strong single hit
            SoundType.ALARM    -> 200  // strong but slightly softer
            SoundType.DOORBELL -> 160  // medium — calm knock
            SoundType.VOICE    -> 100  // soft — gentle nudge
        }

        // Fill: 0 for OFF slots, onAmplitude for ON slots
        return IntArray(size) { index ->
            if (index % 2 == 0) 0 else onAmplitude
        }
    }

    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
                    as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
}