package com.runanywhere.kotlin_starter_example.data

import java.text.SimpleDateFormat
import java.util.*

data class CaptionLine(
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val confidence: Float = 0.9f
)

data class CaptionsExport(
    val captions: List<CaptionLine>,
    val timestamp: Long = System.currentTimeMillis()
) {
    val filename: String get() = "Aeris_Captions_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date(timestamp))}.pdf"
    val formattedDate: String get() = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
}
