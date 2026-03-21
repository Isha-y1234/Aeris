package com.runanywhere.kotlin_starter_example.services

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.runanywhere.kotlin_starter_example.data.SoundProfiles
import com.runanywhere.kotlin_starter_example.data.SoundType

object HapticManager {

    fun trigger(context: Context, sound: SoundType) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        val profile = SoundProfiles.map[sound] ?: return

        val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect.createWaveform(
                profile.pattern,
                profile.repeat
            )
        } else {
            @Suppress("DEPRECATION")
            VibrationEffect.createWaveform(
                profile.pattern,
                profile.repeat
            )
        }

        vibrator.cancel()
        vibrator.vibrate(effect)
    }
}