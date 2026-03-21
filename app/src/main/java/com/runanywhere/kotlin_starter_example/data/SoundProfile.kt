package com.runanywhere.kotlin_starter_example.data

data class SoundProfile(
    val priority: Int,
    val pattern: LongArray,
    val repeat: Int
)

object SoundProfiles {
    val map = mapOf(
        SoundType.SIREN to SoundProfile(5, longArrayOf(0, 300, 100, 300), 0),
        SoundType.HORN to SoundProfile(3, longArrayOf(0, 100, 50, 100), -1),
        SoundType.VOICE to SoundProfile(1, longArrayOf(0, 300, 300, 300), -1)
    )
}