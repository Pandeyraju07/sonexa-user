package com.sonexa.app.data.local

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sonexa.app.data.model.PodcastDto
import com.sonexa.app.data.model.PodcastEpisodeDto
import com.sonexa.app.data.model.TrackDto
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

data class DownloadedEpisode(
    val id: String,
    val podcastId: String,
    val title: String,
    val podcastTitle: String,
    val host: String,
    val coverUrl: String,
    val audioUrl: String,
    val localFilePath: String,
    val fileSizeBytes: Long = 0L,
    val durationLabel: String = "30 min",
    val durationMs: Long = 1800000L,
    val downloadedAt: Long = System.currentTimeMillis()
)

object PodcastDownloadManager {

    private val gson = com.sonexa.app.data.api.RetrofitClient.gson
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _downloadedEpisodes = MutableStateFlow<List<DownloadedEpisode>>(emptyList())
    val downloadedEpisodes: StateFlow<List<DownloadedEpisode>> = _downloadedEpisodes.asStateFlow()

    private val _downloadingIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadingIds: StateFlow<Set<String>> = _downloadingIds.asStateFlow()

    private val activeJobs = ConcurrentHashMap<String, Job>()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("sonexa_podcast_downloads", Context.MODE_PRIVATE)
        val json = prefs.getString("downloaded_episodes", null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<DownloadedEpisode>>() {}.type
                val list: List<DownloadedEpisode> = gson.fromJson(json, type)
                // Filter out any entries where file was removed from disk
                val validList = list.filter { File(it.localFilePath).exists() }
                _downloadedEpisodes.value = validList
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun isDownloaded(episodeId: String): Boolean {
        return _downloadedEpisodes.value.any { it.id == episodeId }
    }

    fun isDownloading(episodeId: String): Boolean {
        return _downloadingIds.value.contains(episodeId)
    }

    fun getLocalAudioUrl(episodeId: String): String? {
        val found = _downloadedEpisodes.value.find { it.id == episodeId } ?: return null
        val file = File(found.localFilePath)
        return if (file.exists()) "file://${file.absolutePath}" else null
    }

    fun downloadEpisode(
        context: Context,
        episode: PodcastEpisodeDto,
        podcast: PodcastDto?,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        if (episode.audioUrl.isBlank()) {
            withContextMain {
                Toast.makeText(context, "Cannot download: stream URL unavailable", Toast.LENGTH_SHORT).show()
            }
            onComplete?.invoke(false)
            return
        }

        if (isDownloaded(episode.id)) {
            withContextMain {
                Toast.makeText(context, "Episode is already downloaded", Toast.LENGTH_SHORT).show()
            }
            onComplete?.invoke(true)
            return
        }

        if (isDownloading(episode.id)) {
            withContextMain {
                Toast.makeText(context, "Episode is currently downloading...", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // Mark as downloading
        val currentDownloading = _downloadingIds.value.toMutableSet()
        currentDownloading.add(episode.id)
        _downloadingIds.value = currentDownloading

        withContextMain {
            Toast.makeText(context, "Starting download for ${episode.title}...", Toast.LENGTH_SHORT).show()
        }

        val job = scope.launch {
            try {
                val downloadDir = getPodcastDownloadDir(context)
                if (!downloadDir.exists()) {
                    downloadDir.mkdirs()
                }

                val safeFileName = "pod_ep_${episode.id.replace("[^a-zA-Z0-9]".toRegex(), "_")}.mp3"
                val destinationFile = File(downloadDir, safeFileName)

                val request = Request.Builder()
                    .url(episode.audioUrl)
                    .header("User-Agent", "Zynera-Downloader/1.0")
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw Exception("Server responded with code ${response.code}")
                }

                val body = response.body ?: throw Exception("Empty response body")
                val inputStream = body.byteStream()
                val outputStream = FileOutputStream(destinationFile)

                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                var totalBytes = 0L

                inputStream.use { input ->
                    outputStream.use { output ->
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalBytes += bytesRead
                        }
                        output.flush()
                    }
                }

                val downloadedItem = DownloadedEpisode(
                    id = episode.id,
                    podcastId = podcast?.id ?: episode.podcastId,
                    title = episode.title,
                    podcastTitle = podcast?.title ?: "Podcast",
                    host = podcast?.host ?: "Host",
                    coverUrl = episode.coverUrl.ifBlank { podcast?.coverUrl.orEmpty() },
                    audioUrl = episode.audioUrl,
                    localFilePath = destinationFile.absolutePath,
                    fileSizeBytes = totalBytes,
                    durationLabel = episode.durationLabel,
                    durationMs = episode.durationMs,
                    downloadedAt = System.currentTimeMillis()
                )

                // Add to downloaded list and persist
                val current = _downloadedEpisodes.value.toMutableList()
                current.removeAll { it.id == episode.id }
                current.add(0, downloadedItem)
                _downloadedEpisodes.value = current
                persist(context, current)

                withContextMain {
                    Toast.makeText(context, "Downloaded: ${episode.title} ✓", Toast.LENGTH_SHORT).show()
                }
                onComplete?.invoke(true)
            } catch (e: Exception) {
                e.printStackTrace()
                withContextMain {
                    Toast.makeText(context, "Download failed: ${e.message ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                }
                onComplete?.invoke(false)
            } finally {
                val updatedDownloading = _downloadingIds.value.toMutableSet()
                updatedDownloading.remove(episode.id)
                _downloadingIds.value = updatedDownloading
                activeJobs.remove(episode.id)
            }
        }

        activeJobs[episode.id] = job
    }

    fun deleteDownloadedEpisode(context: Context, episodeId: String): Boolean {
        val item = _downloadedEpisodes.value.find { it.id == episodeId } ?: return false
        try {
            val file = File(item.localFilePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val current = _downloadedEpisodes.value.toMutableList()
        current.removeAll { it.id == episodeId }
        _downloadedEpisodes.value = current
        persist(context, current)

        Toast.makeText(context, "Episode removed from downloads", Toast.LENGTH_SHORT).show()
        return true
    }

    private fun getPodcastDownloadDir(context: Context): File {
        val externalDir = context.getExternalFilesDir(Environment.DIRECTORY_PODCASTS)
        return externalDir ?: File(context.filesDir, "podcasts")
    }

    private fun persist(context: Context, list: List<DownloadedEpisode>) {
        val prefs = context.getSharedPreferences("sonexa_podcast_downloads", Context.MODE_PRIVATE)
        prefs.edit().putString("downloaded_episodes", gson.toJson(list)).apply()
    }

    private fun withContextMain(block: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            block()
        }
    }

    fun toTrack(downloaded: DownloadedEpisode): TrackDto =
        TrackDto(
            id = downloaded.id,
            title = downloaded.title,
            artist = downloaded.host,
            album = downloaded.podcastTitle,
            durationMs = downloaded.durationMs,
            audioUrl = "file://${downloaded.localFilePath}",
            coverUrl = downloaded.coverUrl,
            provider = "podcast",
            providerType = "local_audio"
        )
}
