package com.sonexa.app.data.local

import android.content.Context
import java.io.File

/**
 * Measures and clears Sonexa on-device audio / image / download caches.
 * Cache size is device-local (not a server API).
 */
object AudioCacheManager {

    fun cacheRoots(context: Context): List<File> {
        val app = context.applicationContext
        return listOfNotNull(
            app.cacheDir,
            app.externalCacheDir,
            File(app.filesDir, "audio_cache"),
            File(app.filesDir, "downloads"),
            File(app.filesDir, "exo_cache"),
            app.getExternalFilesDir("audio_cache"),
            app.getExternalFilesDir("downloads")
        ).distinctBy { it.absolutePath }
    }

    fun cacheSizeBytes(context: Context): Long =
        cacheRoots(context).sumOf { dirSize(it) }

    fun clearCache(context: Context): Long {
        var cleared = 0L
        cacheRoots(context).forEach { root ->
            if (!root.exists()) return@forEach
            cleared += dirSize(root)
            root.listFiles()?.forEach { child ->
                if (child.isDirectory) child.deleteRecursively() else child.delete()
            }
            // Keep the root folders; only wipe contents
        }
        return cleared
    }

    fun formatMb(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb < 0.1) "0 MB" else String.format("%.0f MB", mb)
    }

    private fun dirSize(file: File): Long {
        if (!file.exists()) return 0L
        if (file.isFile) return file.length()
        return file.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    }
}
