package com.sonexa.app

import android.app.Application
import com.sonexa.app.audio.playback.PlaybackManager
import com.sonexa.app.data.api.RetrofitClient
import com.sonexa.app.data.local.LikedSongsStore
import com.sonexa.app.data.local.PodcastDownloadManager
import com.sonexa.app.data.local.SessionManager
import com.sonexa.app.data.local.UserPlaylistStore

class SonexaApp : Application() {
    lateinit var playbackManager: PlaybackManager
        private set

    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(SessionManager.getInstance(this))
        LikedSongsStore.init(this)
        PodcastDownloadManager.init(this)
        UserPlaylistStore.init(this)
        com.sonexa.app.data.provider.MusicMemoryService.init(this)
        com.sonexa.app.data.local.UserTastePreferencesStore.init(this)
        playbackManager = PlaybackManager(this)
    }
}
