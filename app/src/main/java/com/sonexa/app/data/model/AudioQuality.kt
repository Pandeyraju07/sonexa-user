package com.sonexa.app.data.model

import java.util.Locale

enum class AudioQuality(
    val key: String,
    val displayName: String,
    val bitrateKbps: Int,
    val description: String,
    val badgeLabel: String,
    val saavnSuffix: String
) {
    LOW(
        key = "Low",
        displayName = "Data Saver (48 kbps)",
        bitrateKbps = 48,
        description = "Saves mobile data on slower networks",
        badgeLabel = "DATA SAVER • 48K",
        saavnSuffix = "_48.mp4"
    ),
    NORMAL(
        key = "Normal",
        displayName = "Standard (96 kbps)",
        bitrateKbps = 96,
        description = "Smooth streaming on standard cellular data",
        badgeLabel = "STANDARD • 96K",
        saavnSuffix = "_96.mp4"
    ),
    HIGH(
        key = "High",
        displayName = "High Quality (160 kbps)",
        bitrateKbps = 160,
        description = "Crisp, balanced sound for everyday listening",
        badgeLabel = "HQ • 160K",
        saavnSuffix = "_160.mp4"
    ),
    VERY_HIGH(
        key = "Very High",
        displayName = "Studio HD (320 kbps)",
        bitrateKbps = 320,
        description = "Maximum fidelity for premium audio gear",
        badgeLabel = "STUDIO • 320K",
        saavnSuffix = "_320.mp4"
    ),
    LOSSLESS(
        key = "Lossless",
        displayName = "Hi-Fi Lossless (320kbps Master)",
        bitrateKbps = 320,
        description = "Studio master uncompressed lossless stream",
        badgeLabel = "LOSSLESS • 24-BIT MASTER",
        saavnSuffix = "_320.mp4"
    );

    companion object {
        fun fromKey(key: String?): AudioQuality {
            val norm = key?.trim()?.lowercase(Locale.ROOT) ?: "lossless"
            return when {
                norm.contains("lossless") || norm.contains("master") || norm.contains("hi-fi") -> LOSSLESS
                norm.contains("very") || norm.contains("320") -> VERY_HIGH
                norm.contains("high") || norm.contains("160") -> HIGH
                norm.contains("normal") || norm.contains("medium") || norm.contains("96") -> NORMAL
                norm.contains("low") || norm.contains("saver") || norm.contains("48") -> LOW
                else -> LOSSLESS
            }
        }
    }
}
