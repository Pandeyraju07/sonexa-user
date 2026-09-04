package com.sonexa.app.audio

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import kotlin.math.roundToInt

/**
 * Hardware/software DSP Equalizer bound to ExoPlayer audio session with live realtime effects.
 */
class SonexaEqualizerEngine {

    data class Band(
        val index: Int,
        val centerHz: Int,
        val label: String,
        val level: Float // -1f..1f
    )

    data class Snapshot(
        val enabled: Boolean = true,
        val supported: Boolean = true,
        val bands: List<Band> = emptyList(),
        val bassBoost: Float = 0f, // 0..1
        val virtualizer: Float = 0f, // 0..1
        val presetName: String = "Flat",
        val minLevelMb: Short = -1500,
        val maxLevelMb: Short = 1500
    )

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var sessionId: Int = 0

    var enabled: Boolean = true
        private set
    var presetName: String = "Flat"
        private set
    private var bandLevels: FloatArray = FloatArray(5) { 0f } // -1..1
    private var bassStrength: Float = 0.25f
    private var virtualStrength: Float = 0.20f

    fun attach(audioSessionId: Int): Snapshot {
        if (audioSessionId == 0) return snapshot()
        if (audioSessionId == sessionId && equalizer != null) return snapshot()
        releaseEffects()
        sessionId = audioSessionId
        try {
            val eq = Equalizer(0, audioSessionId)
            equalizer = eq
            val count = eq.numberOfBands.toInt().coerceAtLeast(5)
            if (bandLevels.size != count) {
                val old = bandLevels
                bandLevels = FloatArray(count) { old.getOrElse(it) { 0f } }
            }
            applyAll()

            bassBoost = BassBoost(1, audioSessionId).apply {
                enabled = this@SonexaEqualizerEngine.enabled
                setStrength((bassStrength * 1000).roundToInt().coerceIn(0, 1000).toShort())
            }
            virtualizer = Virtualizer(1, audioSessionId).apply {
                enabled = this@SonexaEqualizerEngine.enabled
                setStrength((virtualStrength * 1000).roundToInt().coerceIn(0, 1000).toShort())
            }
        } catch (_: Throwable) {
            releaseEffects()
        }
        return snapshot()
    }

    fun setEnabled(value: Boolean): Snapshot {
        enabled = value
        try {
            equalizer?.enabled = value
            bassBoost?.enabled = value
            virtualizer?.enabled = value
        } catch (_: Throwable) { }
        return snapshot()
    }

    fun setBandLevel(index: Int, normalized: Float): Snapshot {
        if (index !in bandLevels.indices) return snapshot()
        bandLevels[index] = normalized.coerceIn(-1f, 1f)
        presetName = "Custom"
        applyBand(index)
        return snapshot()
    }

    fun setBassBoost(normalized: Float): Snapshot {
        bassStrength = normalized.coerceIn(0f, 1f)
        if (presetName != "Custom") presetName = "Custom"
        try {
            bassBoost?.setStrength((bassStrength * 1000).roundToInt().coerceIn(0, 1000).toShort())
        } catch (_: Throwable) { }
        return snapshot()
    }

    fun setVirtualizer(normalized: Float): Snapshot {
        virtualStrength = normalized.coerceIn(0f, 1f)
        if (presetName != "Custom") presetName = "Custom"
        try {
            virtualizer?.setStrength((virtualStrength * 1000).roundToInt().coerceIn(0, 1000).toShort())
        } catch (_: Throwable) { }
        return snapshot()
    }

    fun applyPreset(name: String): Snapshot {
        val preset = PRESETS[name] ?: return snapshot()
        presetName = name
        val eq = equalizer
        val count = eq?.numberOfBands?.toInt() ?: bandLevels.size
        if (count <= 0) return snapshot()
        if (bandLevels.size != count) bandLevels = FloatArray(count)
        for (i in 0 until count) {
            val src = preset.bands
            bandLevels[i] = when {
                src.isEmpty() -> 0f
                count == 1 -> src.first()
                else -> {
                    val t = i.toFloat() / (count - 1).coerceAtLeast(1)
                    val pos = t * (src.lastIndex)
                    val lo = pos.toInt().coerceIn(0, src.lastIndex)
                    val hi = (lo + 1).coerceAtMost(src.lastIndex)
                    val f = pos - lo
                    src[lo] * (1 - f) + src[hi] * f
                }
            }
        }
        bassStrength = preset.bass
        virtualStrength = preset.virtual
        applyAll()
        return snapshot()
    }

    fun reset(): Snapshot = applyPreset("Flat")

    fun snapshot(): Snapshot {
        val eq = equalizer
        if (eq == null) {
            val defaultBands = DEFAULT_HZ.mapIndexed { i, hz ->
                Band(
                    index = i,
                    centerHz = hz,
                    label = formatHz(hz),
                    level = bandLevels.getOrElse(i) { 0f }
                )
            }
            return Snapshot(
                enabled = enabled,
                supported = true,
                bands = defaultBands,
                bassBoost = bassStrength,
                virtualizer = virtualStrength,
                presetName = presetName
            )
        }
        val range = try {
            eq.bandLevelRange
        } catch (_: Throwable) {
            shortArrayOf(-1500, 1500)
        }
        val min = range[0]
        val max = range[1]
        val bands = (0 until eq.numberOfBands).map { i ->
            val hz = try {
                eq.getCenterFreq(i.toShort()) / 1000
            } catch (_: Throwable) {
                DEFAULT_HZ.getOrElse(i) { 1000 }
            }
            Band(
                index = i,
                centerHz = hz,
                label = formatHz(hz),
                level = bandLevels.getOrElse(i) { 0f }
            )
        }
        return Snapshot(
            enabled = enabled,
            supported = true,
            bands = bands,
            bassBoost = bassStrength,
            virtualizer = virtualStrength,
            presetName = presetName,
            minLevelMb = min,
            maxLevelMb = max
        )
    }

    fun release() {
        releaseEffects()
        sessionId = 0
    }

    private fun applyAll() {
        try {
            equalizer?.enabled = enabled
            for (i in bandLevels.indices) applyBand(i)
            bassBoost?.let {
                it.enabled = enabled
                it.setStrength((bassStrength * 1000).roundToInt().coerceIn(0, 1000).toShort())
            }
            virtualizer?.let {
                it.enabled = enabled
                it.setStrength((virtualStrength * 1000).roundToInt().coerceIn(0, 1000).toShort())
            }
        } catch (_: Throwable) { }
    }

    private fun applyBand(index: Int) {
        val eq = equalizer ?: return
        try {
            val range = eq.bandLevelRange
            val min = range[0].toInt()
            val max = range[1].toInt()
            val n = bandLevels.getOrElse(index) { 0f }.coerceIn(-1f, 1f)
            val mid = (min + max) / 2f
            val half = (max - min) / 2f
            val mb = (mid + n * half).roundToInt().coerceIn(min, max).toShort()
            eq.setBandLevel(index.toShort(), mb)
        } catch (_: Throwable) { }
    }

    private fun releaseEffects() {
        try { equalizer?.release() } catch (_: Throwable) { }
        try { bassBoost?.release() } catch (_: Throwable) { }
        try { virtualizer?.release() } catch (_: Throwable) { }
        equalizer = null
        bassBoost = null
        virtualizer = null
    }

    companion object {
        val PRESET_NAMES = listOf(
            "Flat", "Bass Boost", "Deep Bass", "Club & Dance", "Rock", "Pop", "Hip Hop", "Jazz", "Classical", "Acoustic", "Vocal Booster", "Lounge"
        )

        private data class Preset(val bands: FloatArray, val bass: Float, val virtual: Float)

        private val PRESETS = mapOf(
            "Flat" to Preset(floatArrayOf(0f, 0f, 0f, 0f, 0f), 0f, 0f),
            "Bass Boost" to Preset(floatArrayOf(0.85f, 0.55f, 0.1f, -0.05f, 0f), 0.75f, 0.20f),
            "Deep Bass" to Preset(floatArrayOf(0.95f, 0.70f, 0.25f, 0f, -0.1f), 0.90f, 0.25f),
            "Club & Dance" to Preset(floatArrayOf(0.70f, 0.40f, 0.15f, 0.35f, 0.65f), 0.60f, 0.45f),
            "Rock" to Preset(floatArrayOf(0.55f, 0.25f, -0.15f, 0.3f, 0.55f), 0.40f, 0.30f),
            "Pop" to Preset(floatArrayOf(0.20f, 0.40f, 0.55f, 0.30f, 0.15f), 0.30f, 0.25f),
            "Hip Hop" to Preset(floatArrayOf(0.80f, 0.50f, 0.10f, 0.25f, 0.45f), 0.70f, 0.35f),
            "Jazz" to Preset(floatArrayOf(0.30f, 0.15f, 0f, 0.20f, 0.40f), 0.20f, 0.35f),
            "Classical" to Preset(floatArrayOf(0.20f, 0.15f, 0.05f, 0.20f, 0.35f), 0.15f, 0.45f),
            "Acoustic" to Preset(floatArrayOf(0.25f, 0.35f, 0.45f, 0.50f, 0.30f), 0.20f, 0.25f),
            "Vocal Booster" to Preset(floatArrayOf(-0.25f, 0.20f, 0.70f, 0.50f, 0.15f), 0.10f, 0.20f),
            "Lounge" to Preset(floatArrayOf(0.40f, 0.20f, 0.10f, 0.25f, 0.10f), 0.35f, 0.40f)
        )

        private val DEFAULT_HZ = listOf(60, 230, 910, 3600, 14000)

        private fun formatHz(hz: Int): String = when {
            hz >= 1000 -> String.format("%.1fk", hz / 1000f).replace(".0k", "k")
            else -> "${hz}Hz"
        }
    }
}
