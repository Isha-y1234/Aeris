package com.runanywhere.kotlin_starter_example.data

data class HapticProfile(
    val pattern: LongArray,   // alternating off/on durations in ms
    val repeat: Int           // -1 = no repeat, 0 = repeat from index 0
)

object SoundProfiles {

    val map: Map<SoundType, HapticProfile> = mapOf(

        // ── SIREN: urgent triple pulse ───────────────────────────
        // ●●●  ●●●  ●●●
        SoundType.SIREN to HapticProfile(
            pattern = longArrayOf(0, 200, 100, 200, 100, 200),
            repeat = -1
        ),

        // ── HORN: one long strong burst ──────────────────────────
        // ━━━━━━━━━
        SoundType.HORN to HapticProfile(
            pattern = longArrayOf(0, 600),
            repeat = -1
        ),

        // ── ALARM: rapid double tap × 2 ──────────────────────────
        // ●● ●●  ●● ●●
        SoundType.ALARM to HapticProfile(
            pattern = longArrayOf(0, 100, 50, 100, 200, 100, 50, 100),
            repeat = -1
        ),

        // ── DOORBELL: two gentle knocks ──────────────────────────
        // ●  ●
        SoundType.DOORBELL to HapticProfile(
            pattern = longArrayOf(0, 150, 150, 150),
            repeat = -1
        ),

        // ── VOICE: single soft short tap ─────────────────────────
        // ●
        SoundType.VOICE to HapticProfile(
            pattern = longArrayOf(0, 80),
            repeat = -1
        )
    )
}